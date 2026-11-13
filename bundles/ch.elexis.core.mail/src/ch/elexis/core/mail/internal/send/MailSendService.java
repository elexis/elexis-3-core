package ch.elexis.core.mail.internal.send;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Strings;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.smtp.SMTPAddressFailedException;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailClientRights;
import ch.elexis.core.mail.html.OutgoingHtmlFilter;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.internal.account.MailCredentialProvider;
import ch.elexis.core.mail.internal.account.PasswordCredentialProvider;
import ch.elexis.core.mail.internal.connection.MailSessionFactory;
import ch.elexis.core.mail.internal.connection.MailboxConnectionManager;
import ch.elexis.core.mail.internal.connection.MailcapRegistration;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.internal.send.RecentRecipients;
import ch.elexis.core.mail.internal.mailbox.MailFiledMimeReader;
import ch.elexis.core.mail.internal.oauth.OAuth2TokenCache;
import ch.elexis.core.mail.model.ComposeMode;
import ch.elexis.core.mail.model.MailAttachmentSource;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailFiledItem;
import ch.elexis.core.mail.model.MailFilingResult;
import ch.elexis.core.mail.model.MailFilingStatus;
import ch.elexis.core.mail.model.MailOutgoingAttachment;
import ch.elexis.core.mail.model.MailRecipientSuggestion;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailSendError;
import ch.elexis.core.mail.model.MailSendFiling;
import ch.elexis.core.mail.model.MailSendResult;
import ch.elexis.core.mail.model.MailSendState;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.IMailSentFiling;
import ch.elexis.core.mail.service.IMailSendService;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.IMailboxService;
import ch.elexis.core.mail.service.MailAccessException;
import ch.elexis.core.mail.service.MailNotFoundException;
import jakarta.mail.Address;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.URLName;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/** Prepares, stores and sends visible-account drafts with isolated SMTP queues. */
@Component(service = IMailSendService.class)
public class MailSendService implements IMailSendService {

	static final Pattern SUBJECT_PREFIX = Pattern.compile("^(?:re|aw|r|wg|fwd|fw|tr|i)\\s*:",
			Pattern.CASE_INSENSITIVE);
	private static final DateTimeFormatter BIRTH_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	final Logger log = LoggerFactory.getLogger(getClass());
	@Reference
	private IMailboxAccountService accountService;
	@Reference
	private IContextService contextService;
	@Reference
	private IAccessControlService accessControlService;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, target = "(storeid=ch.elexis.data.store.omnivore)")
	private volatile IDocumentStore documentStore;
	@Reference
	IMailboxService mailboxService;
	@Reference
	MailboxConnectionManager connections;
	@Reference
	IConfigService configService;
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;
	@Reference
	private OAuth2TokenCache tokens;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	private volatile IMailSentFiling archiveService;
	private MailCredentialProvider credentials;
	RecentRecipients recent;
	private final Map<String, AccountSender> senders = new HashMap<>();
	private final Duration throttleDelay;
	final Duration detectionWait;
	private Consumer<Session> smtpProperties = session -> { };
	private boolean stopped;

	private final MailReplyPreparer replies = new MailReplyPreparer(this);
	final MailDraftStore drafts = new MailDraftStore(this);
	final MailSentCopy sentCopies = new MailSentCopy(this);

	/** Creates the lazy DS service with a thirty-second throttle retry delay. */
	public MailSendService() {
		this(Duration.ofSeconds(30));
	}

	MailSendService(Duration throttleDelay) {
		this(throttleDelay, Duration.ofSeconds(30));
	}

	MailSendService(Duration throttleDelay, Duration detectionWait) {
		this.throttleDelay = throttleDelay;
		this.detectionWait = detectionWait;
	}

	@Activate
	void activate() {
		MailcapRegistration.register();
		CredentialCipher cipher = new CredentialCipher(() -> configService.get(MailClientConfig.SECRET_SALT, null),
				salt -> configService.set(MailClientConfig.SECRET_SALT, salt, false));
		credentials = new MailCredentialProvider(new PasswordCredentialProvider(configService, cipher,
				id -> modelService.load(id, IUser.class), id -> modelService.load(id, IContact.class)), tokens);
		recent = new RecentRecipients(configService, () -> contextService.getActiveUser());
	}

	/** {@inheritDoc} */
	@Override
	public List<MailboxAccount> getSendAccounts(IUser user) {
		if (user == null) {
			throw new MailAccessException("Mail account access denied");
		}
		return accountService.getVisibleAccounts(user).stream().filter(account -> account.smtp() != null).toList();
	}

	/** {@inheritDoc} */
	@Override
	public MailDraft prepare(IUser user, ComposeMode mode, String accountId, MailRef original)
			throws MessagingException {
		return replies.prepare(user, mode, accountId, original);
	}

	/** {@inheritDoc} */
	@Override
	public MailDraft prepareFiled(IUser user, ComposeMode mode, String documentId) throws MessagingException {
		MailboxAccount account = visibleAccount(user, getSendAccounts(user).stream().findFirst()
				.orElseThrow(() -> new MailAccessException("Mail account access denied")).id());
		IDocument document = filedDocument(user, documentId);
		Future<MailDraft> future;
		synchronized (senders) {
			if (stopped) {
				throw new MessagingException("Mail send service is disabled");
			}
			future = senders.computeIfAbsent(account.id(), AccountSender::new).executor.submit(() ->
					replies.prepareFiled(user, mode, documentId, MailFiledMimeReader.parse(document)));
		}
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MessagingException("Filed preparation interrupted", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof MailAccessException denied) {
				throw denied;
			}
			throw new MessagingException("Filed message cannot be prepared");
		}
	}

	private IDocument filedDocument(IUser user, String documentId) throws MessagingException {
		if (user == null || !user.getId().equals(contextService.getActiveUserId())
				|| !accessControlService.evaluate(EvACE.of(MailClientRights.MAIL_CLIENT_USE))) {
			throw new MailAccessException("Active document user required");
		}
		IDocumentStore store = documentStore;
		if (store == null) {
			throw new MessagingException("Filed document is unavailable");
		}
		return store.loadDocument(documentId).filter(document -> !document.isDeleted())
				.orElseThrow(() -> new MessagingException("Filed document is unavailable"));
	}

	/** {@inheritDoc} */
	@Override
	public MailDraft saveDraft(IUser user, MailDraft draft) throws MessagingException {
		return drafts.saveDraft(user, draft);
	}

	/** {@inheritDoc} */
	@Override
	public void discardDraft(IUser user, MailDraft draft) throws MessagingException {
		drafts.discardDraft(user, draft);
	}

	/** {@inheritDoc} */
	@Override
	public MailSendResult send(IUser user, MailDraft draft) {
		return send(user, draft, null);
	}

	/** {@inheritDoc} */
	@Override
	public MailSendResult send(IUser user, MailDraft draft, MailSendFiling filing) {
		MailboxAccount account = checkDraft(user, draft);
		Future<MailSendResult> future;
		try {
			synchronized (senders) {
				if (stopped) {
					throw new RejectedExecutionException("Mail send service is disabled");
				}
				AccountSender sender = senders.computeIfAbsent(account.id(), AccountSender::new);
				future = sender.executor.submit(() -> sendNow(user, draft, sender, filing));
			}
		} catch (RejectedExecutionException e) {
			log.debug("Mail send rejected after service shutdown");
			return recover(user, draft, MailSendState.FAILED, MailSendError.FAILED, null);
		}
		boolean interrupted = false;
		try {
			while (true) {
				try {
					return future.get();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} catch (CancellationException e) {
			return recover(user, draft, MailSendState.FAILED, MailSendError.FAILED, null);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof MailAccessException denied) {
				throw denied;
			}
			log.error("Mail send task failed", e.getCause());
			return recover(user, draft, MailSendState.FAILED, MailSendError.FAILED, null);
		} finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private MailSendResult fileSent(IUser user, MailboxAccount account, MimeMessage message, MailSendFiling filing,
			MailSendResult sent) {
		if (filing == null || sent.state() != MailSendState.SENT) {
			return sent;
		}
		IMailSentFiling filer = archiveService;
		MailFilingResult result;
		try {
			if (filer == null) {
				throw new IllegalStateException("Sent mail filing is unavailable");
			}
			result = filer.fileSent(user, account.id(), message, filing).get(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Sent mail filing interrupted for account {}", account.id(), e);
			result = failedFiling(filing);
		} catch (ExecutionException | TimeoutException | RuntimeException e) {
			log.error("Sent mail filing failed for account {}", account.id(), e);
			result = failedFiling(filing);
		}
		return new MailSendResult(sent.state(), sent.error(), sent.detail(), sent.draft(), result);
	}

	private MailFilingResult failedFiling(MailSendFiling filing) {
		return new MailFilingResult(null, filing.patientId(),
				List.of(new MailFiledItem(null, MailFilingStatus.FAILED, null)));
	}

	private MailSendResult sendNow(IUser user, MailDraft draft, AccountSender sender, MailSendFiling filing) {
		MailboxAccount account = checkDraft(user, draft);
		MimeMessage message;
		Session session = new MailSessionFactory(id -> Boolean.parseBoolean(configService.get(MailClientConfig.legacy(id), "false"))).createSmtpSession(account);
		smtpProperties.accept(session);
		try {
			message = build(user, account, draft);
		} catch (MessagingException | IOException | IllegalArgumentException e) {
			return recover(user, draft, MailSendState.FAILED,
					hasCause(e, TooLargeException.class) ? MailSendError.TOO_LARGE : MailSendError.FAILED, null);
		}
		for (int attempt = 0; attempt < 2; attempt++) {
			checkDraft(user, draft);
			SendSmtpTransport transport = new SendSmtpTransport(session,
					new URLName("smtp", account.smtp().host(), account.smtp().port(), null, account.username(), null),
					account.smtp().security() == MailSecurity.SSL);
			sender.transport = transport;
			Failure failure = null;
			try {
				credentials.connect(account, true, null, (username, password) ->
						transport.connect(account.smtp().host(), account.smtp().port(), username, new String(password)));
				transport.sendMessage(message, message.getAllRecipients());
			} catch (MessagingException e) {
				failure = classify(e, transport);
			} finally {
				try {
					transport.close();
				} catch (MessagingException e) {
					log.warn("Mail SMTP transport cleanup failed: {}", e.getClass().getSimpleName());
				}
				sender.transport = null;
			}
			if (failure == null) {
				return fileSent(user, account, message, filing, sentCopies.sent(user, draft, account, message, sender));
			}
			if (failure.state() == MailSendState.UNCERTAIN && !sentCopies.clientCopy(account)) {
				try {
					if (sentCopies.sentCopyExists(user, account, draft.messageId())) {
						return fileSent(user, account, message, filing,
								sentCopies.sent(user, draft, account, message, sender));
					}
				} catch (MessagingException | RuntimeException e) {
					log.warn("Mail sent-copy lookup failed after uncertain delivery: {}", e.getClass().getSimpleName());
				}
			}
			if (failure.error() == MailSendError.THROTTLED && attempt == 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(throttleDelay.toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return recover(user, draft, MailSendState.FAILED, MailSendError.THROTTLED, null);
				}
				continue;
			}
			return recover(user, draft, failure.state(), failure.error(), failure.detail());
		}
		throw new IllegalStateException("SMTP retry limit exceeded");
	}

	private Failure classify(MessagingException error, SendSmtpTransport transport) {
		if (transport.dataStarted() && (hasCause(error, SocketTimeoutException.class)
				|| hasCause(error, IOException.class))) {
			return new Failure(MailSendState.UNCERTAIN, MailSendError.TIMEOUT_AFTER_DATA, null);
		}
		if (hasCause(error, AuthenticationFailedException.class)) {
			return new Failure(MailSendState.FAILED, MailSendError.AUTH_FAILED, null);
		}
		int code = transport.getLastReturnCode();
		String response = transport.getLastServerResponse();
		LinkedHashSet<String> rejected = new LinkedHashSet<>();
		for (Throwable current = error; current != null; current = current.getCause()) {
			if (current instanceof SMTPAddressFailedException address) {
				code = address.getReturnCode();
				rejected.add(address.getAddress().getAddress());
			} else if (current instanceof SMTPSendFailedException send) {
				code = send.getReturnCode();
			}
			if (current instanceof SendFailedException send && send.getInvalidAddresses() != null) {
				for (Address address : send.getInvalidAddresses()) {
					String value = address instanceof InternetAddress internet ? internet.getAddress() : address.toString();
					if (!rejected.contains(value)) {
						rejected.add(value);
					}
				}
			}
		}
		if (code == 552 || response != null && response.contains("5.3.4")) {
			return new Failure(MailSendState.FAILED, MailSendError.TOO_LARGE, null);
		}
		if (code == 432 || response != null && response.contains("4.3.2")) {
			return new Failure(MailSendState.FAILED, MailSendError.THROTTLED, null);
		}
		if (!rejected.isEmpty()) {
			return new Failure(MailSendState.FAILED, MailSendError.RECIPIENT_REJECTED, String.join(", ", rejected));
		}
		if (hasCause(error, MailConnectException.class) || hasCause(error, ConnectException.class)
				|| hasCause(error, UnknownHostException.class) || hasCause(error, SocketTimeoutException.class)) {
			return new Failure(MailSendState.FAILED, MailSendError.UNREACHABLE, null);
		}
		return new Failure(MailSendState.FAILED, MailSendError.FAILED, null);
	}

	private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
		for (Throwable current = error; current != null; current = current.getCause()) {
			if (type.isInstance(current)) {
				return true;
			}
		}
		return false;
	}

	private MailSendResult recover(IUser user, MailDraft draft, MailSendState state, MailSendError error, String detail) {
		MailDraft saved = draft;
		try {
			saved = saveDraft(user, draft);
		} catch (MessagingException | RuntimeException e) {
			log.warn("Failed mail could not be saved as draft: {}", e.getClass().getSimpleName());
		}
		return new MailSendResult(state, error, detail, saved);
	}

	/** {@inheritDoc} */
	@Override
	public List<MailRecipientSuggestion> searchRecipients(IUser user, String query, int limit) {
		if (getSendAccounts(user).isEmpty()) {
			throw new MailAccessException("Mail recipient search access denied");
		}
		recent.checkActive(user);
		if (query == null || query.strip().length() < 2 || limit <= 0) {
			return List.of();
		}
		String term = query.strip();
		Map<String, MailRecipientSuggestion> result = new LinkedHashMap<>();
		for (String address : recent.read(user)) {
			if (Strings.CI.contains(address, term)) {
				result.put(address, new MailRecipientSuggestion("", address, null, true));
				if (result.size() == limit) {
					return List.copyOf(result.values());
				}
			}
		}
		IQuery<IContact> contacts = modelService.getQuery(IContact.class);
		String pattern = "%" + term + "%";
		contacts.and("description1", COMPARATOR.LIKE, pattern, true)
				.or("description2", COMPARATOR.LIKE, pattern, true).or("description3", COMPARATOR.LIKE, pattern, true)
				.or("email", COMPARATOR.LIKE, pattern, true).or("email2", COMPARATOR.LIKE, pattern, true);
		for (IContact contact : contacts.execute()) {
			String name = String.join(" ", value(contact.getDescription1()), value(contact.getDescription2()),
					value(contact.getDescription3())).strip();
			String hint = null;
			if (contact.isPatient()) {
				IPatient patient = contact.asIPatient();
				if (patient != null && patient.getDateOfBirth() != null) {
					hint = BIRTH_DATE.format(patient.getDateOfBirth());
				}
			}
			for (String field : List.of(value(contact.getEmail()), value(contact.getEmail2()))) {
				try {
					for (InternetAddress address : InternetAddress.parse(field, true)) {
						address.validate();
						String email = address.getAddress();
						if (Strings.CI.contains(name, term) || Strings.CI.contains(email, term)) {
							result.putIfAbsent(email.toLowerCase(Locale.ROOT),
									new MailRecipientSuggestion(name, email, hint, false));
							if (result.size() == limit) {
								return List.copyOf(result.values());
							}
						}
					}
				} catch (AddressException e) {
					log.debug("Contact with malformed recipient address skipped");
				}
			}
		}
		return List.copyOf(result.values());
	}

	String value(String text) {
		return text == null ? "" : text;
	}

	MimeMessage buildWithMailClassLoader(IUser user, MailboxAccount account, MailDraft draft)
			throws MessagingException {
		if (Thread.currentThread().getContextClassLoader() == IMAPStore.class.getClassLoader()) {
			try {
				return build(user, account, draft);
			} catch (IOException e) {
				throw new MessagingException("Could not build the draft", e);
			}
		}
		Future<MimeMessage> future;
		synchronized (senders) {
			if (stopped) {
				throw new MessagingException("Mail send service is disabled");
			}
			future = senders.computeIfAbsent(account.id(), AccountSender::new).executor
					.submit(() -> build(user, account, draft));
		}
		try {
			return future.get();
		} catch (CancellationException e) {
			throw new MessagingException("Draft build cancelled during shutdown", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MessagingException("Interrupted while building the draft", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof MessagingException messaging) {
				throw messaging;
			}
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new MessagingException("Could not build the draft", (Exception) cause);
		}
	}

	private MimeMessage build(IUser user, MailboxAccount account, MailDraft draft)
			throws MessagingException, IOException {
		return new MailMessageBuilder(attachment -> originalContent(user, attachment))
				.build(new MailSessionFactory(id -> Boolean.parseBoolean(configService.get(MailClientConfig.legacy(id), "false"))).createSmtpSession(account), account, draft);
	}

	private byte[] originalContent(IUser user, MailOutgoingAttachment attachment) throws MessagingException, IOException {
		if (attachment.source() == MailAttachmentSource.FILED_PART) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			MailFiledMimeReader.write(MailFiledMimeReader.parse(filedDocument(user, attachment.documentId())),
					attachment.partPath(), new SizeLimitOutputStream(bytes));
			return bytes.toByteArray();
		}
		MailRef ref = attachment.originalRef();
		MailboxAccount account = visibleAccount(user, ref.accountId());
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		SizeLimitOutputStream bounded = new SizeLimitOutputStream(bytes);
		if (attachment.source() == MailAttachmentSource.ORIGINAL_PART) {
			mailboxService.writeAttachment(user, ref, attachment.partPath(), bounded);
		} else {
			connections.execute(account, store -> {
				visibleAccount(user, account.id());
				try (OpenFolder opened = new OpenFolder(store, ref.folder())) {
					message(opened.folder(), ref).writeTo(bounded);
				} catch (IOException e) {
					throw new MessagingException("Could not read original message", e);
				}
				return null;
			});
		}
		return bytes.toByteArray();
	}

	MailboxAccount checkDraft(IUser user, MailDraft draft) {
		MailboxAccount account = visibleAccount(user, draft.accountId());
		if (account.smtp() == null) {
			throw new MailAccessException("SMTP account required");
		}
		for (MailRef ref : new MailRef[] { draft.savedDraftRef(), draft.answeredRef() }) {
			if (ref != null) {
				visibleAccount(user, ref.accountId());
			}
		}
		for (MailOutgoingAttachment attachment : draft.attachments()) {
			if (attachment.originalRef() != null) {
				visibleAccount(user, attachment.originalRef().accountId());
			}
		}
		return account;
	}

	MailboxAccount visibleAccount(IUser user, String id) {
		if (user == null) {
			throw new MailAccessException("Mail account access denied");
		}
		return accountService.getVisibleAccounts(user).stream().filter(account -> account.id().equals(id)).findFirst()
				.orElseThrow(() -> new MailAccessException("Mail account access denied"));
	}

	Message message(IMAPFolder folder, MailRef ref) throws MessagingException {
		if (folder.getUIDValidity() != ref.uidValidity()) {
			throw new MailNotFoundException("Mail folder UID validity has changed");
		}
		Message message = folder.getMessageByUID(ref.uid());
		if (message == null || message.isExpunged()) {
			throw new MailNotFoundException("Mail message no longer exists");
		}
		return message;
	}

	MailDraft stored(MailDraft draft, MailRef ref) {
		return new MailDraft(draft.sessionId(), draft.accountId(), draft.to(), draft.cc(), draft.bcc(), draft.subject(),
				new OutgoingHtmlFilter().filter(draft.html()), storedAttachments(draft, ref), draft.messageId(), draft.inReplyTo(),
				draft.references(), draft.answeredRef(), ref);
	}

	private List<MailOutgoingAttachment> storedAttachments(MailDraft draft, MailRef ref) {
		if (ref == null) {
			return draft.attachments();
		}
		List<MailOutgoingAttachment> attachments = new ArrayList<>();
		for (int i = 0; i < draft.attachments().size(); i++) {
			MailOutgoingAttachment attachment = draft.attachments().get(i);
			attachments.add(new MailOutgoingAttachment(attachment.id(), attachment.fileName(), attachment.contentType(),
					attachment.size(), MailAttachmentSource.ORIGINAL_PART, null, ref, Integer.toString(i + 2)));
		}
		return List.copyOf(attachments);
	}

	@Deactivate
	void deactivate() {
		List<AccountSender> workers;
		synchronized (senders) {
			stopped = true;
			workers = new ArrayList<>(senders.values());
			workers.forEach(worker -> worker.executor.shutdown());
		}
		for (AccountSender worker : workers) {
			try {
				if (!worker.executor.awaitTermination(5, TimeUnit.SECONDS)) {
					worker.stopNow();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				worker.stopNow();
			}
		}
	}

	record PendingCopy(IUser user, MailboxAccount account, MimeMessage message, String messageId) {
	}

	private record Failure(MailSendState state, MailSendError error, String detail) {
	}

	final class AccountSender {
		final ExecutorService executor;
		final List<PendingCopy> pendingCopies = new ArrayList<>();
		private volatile SendSmtpTransport transport;

		private AccountSender(String id) {
			ThreadFactory factory = Thread.ofPlatform().daemon(true).name("mail-account-" + id + "-send").factory();
			executor = Executors.newSingleThreadExecutor(runnable -> {
				Thread thread = factory.newThread(runnable);
				thread.setContextClassLoader(IMAPStore.class.getClassLoader());
				return thread;
			});
		}

		private void stopNow() {
			for (Runnable task : executor.shutdownNow()) {
				if (task instanceof Future<?> future) {
					future.cancel(false);
				}
			}
			SendSmtpTransport closing = transport;
			if (closing != null) {
				try {
					closing.close();
				} catch (MessagingException e) {
					log.debug("Mail SMTP transport stopped during shutdown");
				}
			}
		}
	}
}
