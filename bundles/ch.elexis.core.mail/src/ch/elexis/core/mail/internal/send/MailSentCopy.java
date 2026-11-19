package ch.elexis.core.mail.internal.send;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.internal.send.MailSendService.AccountSender;
import ch.elexis.core.mail.internal.send.MailSendService.PendingCopy;
import ch.elexis.core.mail.model.MailAddress;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailSendResult;
import ch.elexis.core.mail.model.MailSendState;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.SentCopyDetected;
import ch.elexis.core.mail.model.SentCopyMode;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

final class MailSentCopy {
	private final MailSendService service;

	MailSentCopy(MailSendService service) {
		this.service = service;
	}

	MailSendResult sent(IUser user, MailDraft draft, MailboxAccount account, MimeMessage message,
			AccountSender sender) {
		if (account.imap() != null && clientCopy(account)) {
			appendSentSafely(user, account, message);
		} else if (account.imap() != null && account.sentCopyMode() == SentCopyMode.AUTO && account.provider() != MailProvider.M365
				&& detected(account) == SentCopyDetected.UNKNOWN) {
			sender.pendingCopies.add(new PendingCopy(user, account, message, draft.messageId()));
			if (sender.pendingCopies.size() == 1) {
				String settings = service.configService.get(MailClientConfig.account(account.id()), null);
				try {
					sender.executor.execute(() -> detectSentCopies(sender, settings));
				} catch (RejectedExecutionException e) {
					service.log.debug("Mail sent-copy detection rejected after service shutdown");
					appendSentSafely(user, account, message);
					sender.pendingCopies.clear();
				}
			}
		}
		if (draft.answeredRef() != null) {
			try {
				MailRef ref = draft.answeredRef();
				MailboxAccount original = service.visibleAccount(user, ref.accountId());
				service.connections.execute(original, store -> {
					service.visibleAccount(user, original.id());
					try (OpenFolder opened = new OpenFolder(store, ref.folder(), true)) {
						service.message(opened.folder(), ref).setFlag(Flags.Flag.ANSWERED, true);
					}
					return null;
				});
			} catch (MessagingException | RuntimeException e) {
				service.log.warn("Mail answered flag update failed: {}", e.getClass().getSimpleName());
			}
		}
		MailRef remaining = draft.savedDraftRef();
		try {
			if (remaining != null) {
				service.drafts.remove(user, remaining);
				remaining = null;
			}
		} catch (MessagingException | RuntimeException e) {
			service.log.warn("Sent mail draft cleanup failed: {}", e.getClass().getSimpleName());
		}
		try {
			List<MailAddress> recipients = new ArrayList<>(draft.to());
			recipients.addAll(draft.cc());
			recipients.addAll(draft.bcc());
			service.recent.remember(user, recipients);
		} catch (RuntimeException e) {
			service.log.warn("Mail recent recipients update failed: {}", e.getClass().getSimpleName());
		}
		return new MailSendResult(MailSendState.SENT, null, null, service.stored(draft, remaining));
	}

	private SentCopyDetected detected(MailboxAccount account) {
		String value = service.configService.get(MailClientConfig.sentCopyDetected(account.id()),
				SentCopyDetected.UNKNOWN.name());
		try {
			return SentCopyDetected.valueOf(value);
		} catch (IllegalArgumentException e) {
			return SentCopyDetected.UNKNOWN;
		}
	}

	boolean clientCopy(MailboxAccount account) {
		return account.sentCopyMode() == SentCopyMode.CLIENT
				|| account.sentCopyMode() == SentCopyMode.AUTO && account.provider() != MailProvider.M365
						&& detected(account) == SentCopyDetected.NO_SERVER_COPY;
	}

	private void detectSentCopies(AccountSender sender, String settings) {
		PendingCopy first = sender.pendingCopies.getFirst();
		SentCopyDetected result = SentCopyDetected.UNKNOWN;
		try {
			long deadline = System.nanoTime() + service.detectionWait.toNanos();
			do {
				if (sentCopyExists(first.user(), first.account(), first.messageId())) {
					result = SentCopyDetected.SERVER_COPIES;
					break;
				}
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					result = SentCopyDetected.NO_SERVER_COPY;
					break;
				}
				TimeUnit.NANOSECONDS.sleep(Math.min(remaining, TimeUnit.SECONDS.toNanos(3)));
			} while (true);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			service.log.debug("Mail sent-copy detection interrupted: {}", e.getClass().getSimpleName());
		} catch (MessagingException | RuntimeException e) {
			service.log.warn("Mail sent-copy detection failed: {}", e.getClass().getSimpleName());
		}
		try {
			boolean copied = true;
			if (result != SentCopyDetected.SERVER_COPIES) {
				for (PendingCopy pending : sender.pendingCopies) {
					copied &= appendSentSafely(pending.user(), pending.account(), pending.message());
				}
			}
			if (copied && result != SentCopyDetected.UNKNOWN
					&& Objects.equals(settings, service.configService.get(MailClientConfig.account(first.account().id()), null))
					&& service.visibleAccount(first.user(), first.account().id()).sentCopyMode() == SentCopyMode.AUTO) {
				service.configService.set(MailClientConfig.sentCopyDetected(first.account().id()), result.name());
			}
		} catch (RuntimeException e) {
			service.log.warn("Mail sent-copy detection persistence failed: {}", e.getClass().getSimpleName());
		} finally {
			sender.pendingCopies.clear();
		}
	}

	private boolean appendSentSafely(IUser user, MailboxAccount account, MimeMessage message) {
		try {
			String folder = roleFolder(user, account, MailFolderRole.SENT);
			service.connections.execute(account, store -> {
				service.visibleAccount(user, account.id());
				message.setFlag(Flags.Flag.SEEN, true);
				store.getFolder(folder).appendMessages(new Message[] { message });
				return null;
			});
			service.connections.folderChanged(account.id(), folder);
			return true;
		} catch (MessagingException | RuntimeException e) {
			service.log.warn("Mail sent-copy append failed: {}", e.getClass().getSimpleName());
			return false;
		}
	}

	boolean sentCopyExists(IUser user, MailboxAccount account, String id) throws MessagingException {
		String folder = roleFolder(user, account, MailFolderRole.SENT);
		return service.connections.execute(account, store -> {
			service.visibleAccount(user, account.id());
			try (OpenFolder opened = new OpenFolder(store, folder)) {
				return service.drafts.findMessageId(opened.folder(), id) != null;
			}
		});
	}

	String roleFolder(IUser user, MailboxAccount account, MailFolderRole role) throws MessagingException {
		return service.mailboxService.listFolders(user, account.id()).stream()
				.filter(folder -> folder.selectable() && folder.role() == role).map(folder -> folder.fullName()).findFirst()
				.orElseThrow(() -> new MessagingException("Required mail folder is not configured"));
	}
}
