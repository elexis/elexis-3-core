package ch.elexis.core.mail.internal.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.Status;

import ch.elexis.core.mail.internal.mailbox.MailboxService;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.mail.MessagingException;

final class GmailLabelCounts {
	private final MailboxConnectionManager manager;

	GmailLabelCounts(MailboxConnectionManager manager) {
		this.manager = manager;
	}

	void requestGmailCounts(String accountId) {
		MailLane lane = manager.currentLane(accountId, false);
		if (lane == null) {
			return;
		}
		IMAPStore store = lane.store;
		MailboxAccount account = lane.connectedAccount;
		try {
			if (store != null && account != null && store.hasCapability("X-GM-EXT-1")) {
				manager.refreshGmailCounts(account);
			}
		} catch (MessagingException e) {
			manager.log.debug("Mail label count capabilities unavailable");
		}
	}

	void refreshGmailCounts(MailboxAccount account) {
		synchronized (manager.accounts) {
			if (manager.stopped || manager.eventAdmin == null) {
				return;
			}
			MailAccountWork worker = manager.accounts.computeIfAbsent(account.id(), id -> new MailAccountWork(manager, id));
			if (worker.ended || worker.countRefreshPending) {
				return;
			}
			worker.countRefreshPending = true;
			long generation = worker.generation;
			long delay = Math.max(TimeUnit.MILLISECONDS.toNanos(100), worker.nextCountRefresh - System.nanoTime());
			try {
				manager.idle.schedule(() -> queueGmailCounts(worker, account, generation), delay, TimeUnit.NANOSECONDS);
			} catch (RejectedExecutionException e) {
				worker.countRefreshPending = false;
				manager.log.debug("Mail label count timer rejected after shutdown");
			}
		}
	}

	private void queueGmailCounts(MailAccountWork worker, MailboxAccount account, long generation) {
		synchronized (manager.accounts) {
			if (manager.stopped || worker.ended || generation != worker.generation) {
				return;
			}
			MailWorkTask<Void> task = new MailWorkTask<>(manager, () -> {
				synchronized (manager.accounts) {
					worker.countRefreshPending = false;
					worker.nextCountRefresh = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
				}
				try {
					worker.main.execute(account, store -> {
						if (store.hasCapability("X-GM-EXT-1") && store.hasCapability("LIST-STATUS")) {
							Map<String, Status> counts = new HashMap<>();
							MailboxService.listedFolders(store, counts);
							for (Map.Entry<String, Status> entry : counts.entrySet()) {
								Status status = entry.getValue();
								manager.folderCounts(account.id(), entry.getKey(), status.total, status.unseen);
							}
						}
						return null;
					}, true);
				} catch (MessagingException | RuntimeException e) {
					manager.log.debug("Mail label count refresh failed");
				}
				return null;
			}, null, worker, true);
			try {
				worker.main.executor.execute(task);
			} catch (RejectedExecutionException e) {
				worker.countRefreshPending = false;
				manager.log.debug("Mail label count refresh rejected after shutdown");
			}
		}
	}
}
