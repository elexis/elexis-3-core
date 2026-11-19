package ch.elexis.core.mail.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.mail.IMAPMailMessage;
import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.internal.account.MailCredentialProvider;
import ch.elexis.core.mail.internal.account.PasswordCredentialProvider;
import ch.elexis.core.mail.internal.connection.MailSessionFactory;
import ch.elexis.core.mail.internal.connection.MailcapRegistration;
import ch.elexis.core.mail.internal.legacy.MailLegacyAccountConverter;
import ch.elexis.core.mail.internal.legacy.MailLegacyAccountStore;
import ch.elexis.core.mail.internal.oauth.OAuth2TokenCache;
import ch.elexis.core.mail.internal.send.MailSystemSender;
import ch.elexis.core.mail.internal.send.SendSmtpTransport;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Service;
import jakarta.mail.URLName;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.SearchTerm;

@ApplicationScoped
@Component
public class MailClient implements IMailClient {

	private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

	private static final String CONFIG_ACCOUNTS = "ch.elexis.core.mail/accounts";
	private static final String CONFIG_ACCOUNT = "ch.elexis.core.mail/account";

	private static final String ACCOUNTS_SEPARATOR = ",";

	@Inject
	@Reference
	IConfigService configService;

	@Inject
	@Reference
	private IMailboxAccountService accountService;
	@Inject
	@Reference
	private MailLegacyAccountStore legacyAccounts;
	@Inject
	@Reference
	private OAuth2TokenCache tokens;
	@Inject
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;
	private MailCredentialProvider credentials;
	private MailSystemSender sender;
	private final ThreadFactory threads = Thread.ofPlatform().daemon(true).name("mail-legacy-", 0).factory();
	private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
		Thread thread = threads.newThread(runnable);
		thread.setContextClassLoader(IMAPStore.class.getClassLoader());
		return thread;
	});

	private volatile ErrorTyp lastError;

	@Activate
	@PostConstruct
	void activate() {
		MailcapRegistration.register();
		CredentialCipher cipher = new CredentialCipher();
		credentials = new MailCredentialProvider(new PasswordCredentialProvider(configService, cipher,
				id -> modelService.load(id, IUser.class), id -> modelService.load(id, IContact.class)), tokens);
		sender = new MailSystemSender(configService, credentials);
	}

	@Override
	public Optional<ErrorTyp> getLastError() {
		ErrorTyp ret = lastError;
		lastError = null;
		return Optional.ofNullable(ret);
	}

	@Override
	public Optional<MailAccount> getAccount(String id) {
		String local = configService.getLocal(CONFIG_ACCOUNT + "/" + id, null);
		if (local != null) {
			logger.warn("Using local legacy mail account {}", id);
			return Optional.ofNullable(MailAccount.from(local));
		}
		return accountService.getAccount(id)
				.map(account -> MailLegacyAccountConverter.toLegacy(account, accountService.getMandatorIds(id)));
	}

	@Override
	public List<String> getAccounts() {
		return Arrays.stream(configService.get(MailClientConfig.ACCOUNTS, "").split(ACCOUNTS_SEPARATOR))
				.filter(id -> !id.isBlank()).filter(id -> accountService.getAccount(id).isPresent()).toList();
	}

	@Override
	public List<String> getAccountsLocal() {
		List<String> ret = new ArrayList<>();
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			ret.addAll(Arrays.asList(currentIds));
		}
		return ret;
	}

	@Override
	public void saveAccount(MailAccount account) {
		if (account != null && account.getId() != null) {
			legacyAccounts.save(account);
		}
	}

	@Override
	public void saveAccountLocal(MailAccount account) {
		if (account != null && account.getId() != null) {
			addAccountIdLocal(account.getId());
			configService.setLocal(CONFIG_ACCOUNT + "/" + account.getId(), account.toString());
		}
	}

	private void addAccountIdLocal(String id) {
		if (id.contains(ACCOUNTS_SEPARATOR)) {
			throw new IllegalStateException("Id can not contain separator [" + ACCOUNTS_SEPARATOR + "]");
		}
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
		if (accountIds == null) {
			configService.setLocal(CONFIG_ACCOUNTS, id);
		} else {
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (string.equals(id)) {
					return;
				}
			}
			// not already in list
			configService.setLocal(CONFIG_ACCOUNTS, accountIds + ACCOUNTS_SEPARATOR + id);
		}
	}

	@Override
	public void removeAccount(MailAccount account) {
		if (account != null && account.getId() != null) {
			legacyAccounts.remove(account.getId());
		}
	}

	@Override
	public void removeAccountLocal(MailAccount account) {
		if (account != null && account.getId() != null) {
			removeAccountIdLocal(account.getId());
			configService.setLocal(CONFIG_ACCOUNT + "/" + account.getId(), null);
		}
	}

	private void removeAccountIdLocal(String id) {
		String accountIds = configService.getLocal(CONFIG_ACCOUNTS, null);
		if (accountIds != null) {
			StringBuilder sb = new StringBuilder();
			String[] currentIds = accountIds.split(ACCOUNTS_SEPARATOR);
			for (String string : currentIds) {
				if (!string.equals(id)) {
					if (sb.length() > 0) {
						sb.append(ACCOUNTS_SEPARATOR);
					}
					sb.append(string);
				}
			}
			// write new list
			if (StringUtils.isNotBlank(sb.toString())) {
				configService.setLocal(CONFIG_ACCOUNTS, sb.toString());
			} else {
				configService.setLocal(CONFIG_ACCOUNTS, null);
			}
		}
	}

	@Override
	public boolean testAccount(MailAccount account) {
		if (account == null || account.getType() == null) {
			lastError = ErrorTyp.CONFIGTYP;
			return false;
		}
		try {
			mailWork(() -> {
				Resolved resolved = resolve(account);
				MailboxAccount settings = resolved.account();
				MailSessionFactory sessions = sessions(resolved);
				boolean smtp = settings.smtp() != null;
				try (Service connection = smtp
						? new SendSmtpTransport(sessions.createSmtpSession(settings), new URLName("smtp",
								settings.smtp().host(), settings.smtp().port(), null, null, null),
								settings.smtp().security() == MailSecurity.SSL)
						: sessions.createStore(settings)) {
					credentials.connect(settings, smtp, resolved.password(), (username, password) -> connection.connect(
							smtp ? settings.smtp().host() : settings.imap().host(),
							smtp ? settings.smtp().port() : settings.imap().port(), username, new String(password)));
				} finally {
					resolved.clear();
				}
				return null;
			});
			return true;
		} catch (MessagingException e) {
			handleException(e);
			return false;
		} catch (IllegalArgumentException e) {
			lastError = ErrorTyp.CONFIGTYP;
			return false;
		}
	}

	@Override
	public boolean sendMail(MailAccount account, MailMessage message) {
		if (account == null || account.getType() != TYPE.SMTP) {
			lastError = ErrorTyp.CONFIGTYP;
			return false;
		}
		try {
			mailWork(() -> {
				Resolved resolved = resolve(account);
				try {
					if (resolved.account().smtp() == null) {
						throw new IllegalArgumentException("SMTP account required");
					}
					sender.send(resolved.account(), message, resolved.password(), sessions(resolved));
				} finally {
					resolved.clear();
				}
				return null;
			});
			return true;
		} catch (MessagingException e) {
			handleException(e);
			return false;
		} catch (IllegalArgumentException e) {
			lastError = ErrorTyp.CONFIGTYP;
			return false;
		}
	}

	@Override
	public List<IMAPMailMessage> getMessages(MailAccount account, String sourceFolder, boolean flag,
			boolean onlyFetchUnflagged) throws MessagingException {
		return mailWork(() -> readMessages(account, sourceFolder, flag, onlyFetchUnflagged));
	}

	private List<IMAPMailMessage> readMessages(MailAccount account, String sourceFolder, boolean flag,
			boolean onlyFetchUnflagged) throws MessagingException {
		Resolved resolved = resolve(account);
		try {
			if (resolved.account().imap() == null) {
				lastError = ErrorTyp.CONFIGTYP;
				return Collections.emptyList();
			}
		} finally {
			resolved.clear();
		}

		if (sourceFolder == null) {
			sourceFolder = "INBOX";
		}

		IMAPStore imapStore = null;
		try {
			imapStore = connectedStore(account);
			List<IMAPMailMessage> listMessages = new ArrayList<>();
			Folder folder = imapStore.getFolder(sourceFolder);

			int openMode = (flag) ? Folder.READ_WRITE : Folder.READ_ONLY;
			folder.open(openMode);

			Message[] messages = folder.getMessages();
			for (Message _message : messages) {
				if (onlyFetchUnflagged && _message.getFlags().contains(Flag.FLAGGED)) {
					continue;
				}
				IMAPMailMessage selfSustainableCopy = IMAPMailMessage.of((IMAPMessage) _message);
				listMessages.add(selfSustainableCopy);
				if (flag) {
					_message.setFlag(Flag.FLAGGED, true);
				}
			}
			folder.close(false);
			return listMessages;
		} finally {
			if (imapStore != null && imapStore.isConnected()) {
				imapStore.close();
			}
		}
	}

	@Override
	public void moveMessage(MailAccount account, IMAPMailMessage message, @Nullable String sourceFolder,
			String targetFolder, boolean removeFlag) throws MessagingException {

		mailWork(() -> {
			move(account, message, sourceFolder, targetFolder, removeFlag);
			return null;
		});
	}

	private void move(MailAccount account, IMAPMailMessage message, String sourceFolder,
			String targetFolder, boolean removeFlag) throws MessagingException {

		if (sourceFolder == null) {
			sourceFolder = "INBOX";
		}

		if (targetFolder == null) {
			throw new MessagingException("targetFolder must not be null");
		}

		IMAPStore imapStore = null;
		try {
			imapStore = connectedStore(account);

			IMAPFolder _sourceFolder = (IMAPFolder) imapStore.getFolder(sourceFolder);
			IMAPFolder _targetFolder = (IMAPFolder) imapStore.getFolder(targetFolder);

			// find message in sourcefolder
			MimeMessage referrer = (MimeMessage) message.toIMAPMessage();
			SearchTerm searchTerm = new MySearchTerm(referrer.getSubject(), referrer.getSize());

			_sourceFolder.open(Folder.READ_WRITE);
			Message[] matches = Arrays.stream(_sourceFolder.getMessages()).filter(searchTerm::match).toArray(Message[]::new);

			if (matches != null && matches.length == 1) {
				// found the message, lets move it

				_targetFolder.open(Folder.READ_WRITE);

				Message[] _message = new Message[] { matches[0] };

				if (removeFlag) {
					_message[0].setFlag(Flag.FLAGGED, false);
				}

				_sourceFolder.setFlags(_message, new Flags(Flags.Flag.SEEN), true);
				// TODO support direct move operation,
				// requires newer javax.mail https://github.com/javaee/javamail/releases
				// and imapStore.hasCapability("MOVE")
				_sourceFolder.copyMessages(_message, _targetFolder);
				_sourceFolder.setFlags(_message, new Flags(Flags.Flag.DELETED), true);

				_sourceFolder.close(true);
				_targetFolder.close(false);

			} else {
				throw new MessagingException("Could not find message in sourcefolder");
			}

		} finally {
			if (imapStore != null && imapStore.isConnected()) {
				imapStore.close();
			}
		}
	}

	private Resolved resolve(MailAccount account) {
		String local = configService.getLocal(CONFIG_ACCOUNT + "/" + account.getId(), null);
		if (local != null) {
			MailAccount legacy = MailAccount.from(local);
			return new Resolved(MailLegacyAccountConverter.toMailbox(legacy),
					legacy.getPassword() == null ? new char[0] : legacy.getPassword().toCharArray(), true);
		}
		Optional<MailboxAccount> saved = accountService.getAccount(account.getId());
		if (saved.isPresent()) {
			return new Resolved(saved.get(), null, accountService.isLegacy(account.getId()));
		}
		return new Resolved(MailLegacyAccountConverter.toMailbox(account),
				account.getPassword() == null ? new char[0] : account.getPassword().toCharArray(), true);
	}

	private MailSessionFactory sessions(Resolved account) {
		return new MailSessionFactory(id -> account.legacy());
	}

	private IMAPStore connectedStore(MailAccount account) throws MessagingException {
		Resolved resolved = resolve(account);
		IMAPStore store = null;
		try {
			MailboxAccount settings = resolved.account();
			if (settings.imap() == null) {
				throw new MessagingException("IMAP account required");
			}
			store = sessions(resolved).createStore(settings);
			IMAPStore opened = store;
			credentials.connect(settings, false, resolved.password(), (username, password) -> opened.connect(
					settings.imap().host(), settings.imap().port(), username, new String(password)));
			return store;
		} catch (MessagingException | RuntimeException e) {
			if (store != null) {
				store.close();
			}
			throw e;
		} finally {
			resolved.clear();
		}
	}

	private <T> T mailWork(Callable<T> work) throws MessagingException {
		try {
			return executor.submit(work).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MessagingException("Legacy mail operation interrupted", e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof MessagingException mail) {
				handleException(mail);
				throw mail;
			}
			if (e.getCause() instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new MessagingException("Legacy mail operation failed", e);
		}
	}

	private void handleException(MessagingException e) {
		lastError = e instanceof AuthenticationFailedException ? ErrorTyp.AUTHENTICATION
				: e instanceof AddressException ? ErrorTyp.ADDRESS : ErrorTyp.CONNECTION;
		logger.warn("Legacy mail operation failed: {}", e.getClass().getSimpleName());
	}

	@Deactivate
	@PreDestroy
	void deactivate() {
		executor.shutdown();
	}

	private record Resolved(MailboxAccount account, char[] password, boolean legacy) {
		void clear() {
			if (password != null) {
				Arrays.fill(password, '\0');
			}
		}
	}

	private class MySearchTerm extends SearchTerm {

		private static final long serialVersionUID = -5686587458681566618L;

		private final String subject;
		private final int size;

		public MySearchTerm(String subject, int size) {
			this.subject = subject;
			this.size = size;
		}

		@Override
		public boolean match(Message msg) {
			try {
				return (Objects.equals(msg.getSubject(), subject) && msg.getSize() == size);
			} catch (MessagingException e) {
				logger.warn("Error matching message", e);
				return false;
			}
		}

	}
}
