package ch.elexis.core.mail.internal.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.mail.FetchProfile;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.Message;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.BODY;
import org.eclipse.angus.mail.imap.protocol.FetchResponse;
import org.eclipse.angus.mail.imap.protocol.UID;

import ch.elexis.core.model.IUser;

import ch.elexis.core.mail.internal.connection.MailboxConnectionManager;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.model.MailMessageView;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailNotFoundException;

class MailMessageReader {
	private final MailboxService mailbox;

	MailMessageReader(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	/** {@inheritDoc} */
	MailMessageView openMessage(IUser user, MailRef ref) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, ref.accountId());
		return mailbox.connections.executeReader(account, store -> loadMessage(store, ref));
	}

	/** {@inheritDoc} */
	void prefetch(IUser user, Object session, List<MailRef> refs, Predicate<MailRef> needed,
			Consumer<MailMessageView> loaded) {
		if (session == null || refs == null || refs.isEmpty() || refs.size() > 5 || refs.stream().anyMatch(ref -> ref == null)
				|| needed == null || loaded == null) {
			throw new IllegalArgumentException("Invalid mail prefetch parameters");
		}
		Map<String, List<MailRef>> groups = refs.stream().collect(Collectors.groupingBy(MailRef::accountId));
		for (var entry : groups.entrySet()) {
			MailboxAccount account = mailbox.visibleAccount(user, entry.getKey());
			List<MailboxConnectionManager.StoreCallback<Void>> work = entry.getValue().stream().filter(needed)
					.<MailboxConnectionManager.StoreCallback<Void>>map(ref -> store -> {
						mailbox.visibleAccount(user, ref.accountId());
						if (needed.test(ref)) {
							loaded.accept(loadMessage(store, ref));
						}
						return null;
					}).toList();
			mailbox.connections.prefetch(account, session, work);
		}
	}

	private MailMessageView loadMessage(IMAPStore store, MailRef ref) throws MessagingException {
		try (OpenFolder opened = new OpenFolder(store, ref.folder())) {
			IMAPFolder folder = opened.folder();
			MailboxConnectionManager.checkReadRequest();
			if (folder.getUIDValidity() != ref.uidValidity()) {
				throw new MailNotFoundException("Mail folder UID validity has changed");
			}
			Message remote = fetchMessage(folder, ref);
			Message content = remote;
			if (remote.getSize() >= 0 && remote.getSize() <= 2 * 1024 * 1024) {
				MailboxConnectionManager.checkReadRequest();
				BODY body = (BODY) folder.doCommand(protocol -> protocol.peekBody(remote.getMessageNumber(), ""));
				if (body == null || body.getByteArrayInputStream() == null) {
					throw new MailNotFoundException("Mail message no longer exists");
				}
				content = new MimeMessage(Session.getInstance(new Properties()), body.getByteArrayInputStream());
			}
			MailMimeReader.Display display = new MailMimeReader().read(MailHeaderMapper.readable(content));
			return new MailMessageView(MailHeaderMapper.header(remote, ref, true), MailHeaderMapper.addresses(content.getRecipients(Message.RecipientType.TO)),
					MailHeaderMapper.addresses(content.getRecipients(Message.RecipientType.CC)), display.html(), display.plain(),
					display.attachments());
		} catch (MessageRemovedException | FolderNotFoundException e) {
			throw new MailNotFoundException("Mail message no longer exists");
		} catch (IOException e) {
			throw new MessagingException("Could not decode message for display", e);
		}
	}

	private Message fetchMessage(IMAPFolder folder, MailRef ref) throws MessagingException {
		FetchProfile profile = MailHeaderMapper.fetchProfile();
		Response[] responses = (Response[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeNumber(ref.uid());
			Argument items = new Argument();
			items.writeAtom("UID ENVELOPE INTERNALDATE RFC822.SIZE FLAGS BODYSTRUCTURE BODY.PEEK[HEADER.FIELDS ("
					+ String.join(" ", profile.getHeaderNames()) + ")]");
			arguments.writeArgument(items);
			Response[] fetched = protocol.command("UID FETCH", arguments);
			protocol.notifyResponseHandlers(fetched);
			protocol.handleResult(fetched[fetched.length - 1]);
			return fetched;
		});
		for (Response response : responses) {
			if (response instanceof FetchResponse fetch) {
				UID uid = fetch.getItem(UID.class);
				if (uid != null && uid.uid == ref.uid()) {
					return new MailHeaderList.FetchedMessage(folder, fetch, profile);
				}
			}
		}
		throw new MailNotFoundException("Mail message no longer exists");
	}

	/** {@inheritDoc} */
	MailAttachment writeAttachment(IUser user, MailRef ref, String partPath, OutputStream target)
			throws MessagingException, IOException {
		MailboxAccount account = mailbox.visibleAccount(user, ref.accountId());
		Path staging = Files.createTempFile("mail-attachment-", ".tmp");
		try {
			MailAttachment metadata = mailbox.connections.executeReader(account, store -> {
				try (OpenFolder opened = new OpenFolder(store, ref.folder());
						OutputStream stagingOutput = Files.newOutputStream(staging)) {
					Message message = MailHeaderMapper.message(opened.folder(), ref);
					MailMimeReader reader = new MailMimeReader();
					Part part = reader.attachment(MailHeaderMapper.readable(message), partPath);
					MailAttachment result = reader.metadata(part, partPath);
					try (InputStream input = part.getInputStream()) {
						byte[] buffer = new byte[8192];
						int read;
						while ((read = input.read(buffer)) != -1) {
							try {
								stagingOutput.write(buffer, 0, read);
							} catch (IOException e) {
								throw new TargetWriteFailure(e);
							}
						}
					}
					return result;
				} catch (MessageRemovedException | FolderNotFoundException e) {
					throw new MailNotFoundException("Mail message no longer exists");
				} catch (IOException e) {
					throw new MessagingException("Could not read mail attachment", e);
				}
			});
			try (InputStream input = Files.newInputStream(staging)) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = input.read(buffer)) != -1) {
					target.write(buffer, 0, read);
				}
			}
			return metadata;
		} catch (TargetWriteFailure e) {
			throw e.failure;
		} finally {
			Files.deleteIfExists(staging);
		}
	}

	void writeMessage(IUser user, MailRef ref, OutputStream target) throws MessagingException, IOException {
		MailboxAccount account = mailbox.visibleAccount(user, ref.accountId());
		Path staging = Files.createTempFile("mail-message-", ".eml");
		try {
			mailbox.connections.executeReader(account, store -> {
				try (OpenFolder opened = new OpenFolder(store, ref.folder());
						OutputStream output = Files.newOutputStream(staging)) {
					Message message = MailHeaderMapper.message(opened.folder(), ref);
					message.writeTo(output);
					return null;
				} catch (MessageRemovedException | FolderNotFoundException e) {
					throw new MailNotFoundException("Mail message no longer exists");
				} catch (IOException e) {
					throw new TargetWriteFailure(e);
				}
			});
			try (InputStream input = Files.newInputStream(staging)) {
				input.transferTo(target);
			}
		} catch (TargetWriteFailure e) {
			throw e.failure;
		} finally {
			Files.deleteIfExists(staging);
		}
	}

	private static final class TargetWriteFailure extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final IOException failure;

		private TargetWriteFailure(IOException failure) {
			super("Could not write attachment to target", failure);
			this.failure = failure;
		}
	}
}
