package ch.elexis.core.mail.internal.connection;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.angus.mail.imap.IMAPStore;

import ch.elexis.core.mail.model.AccountConnectionState;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;

final class MailLane {
	private final MailboxConnectionManager manager;
	private final MailAccountWork owner;
	final ThreadPoolExecutor executor;
	final OpenFolder.Cache folders;
	volatile IMAPStore store;
	volatile Thread thread;
	MailboxAccount connectedAccount;
	private volatile long lastUsed;
	private ScheduledFuture<?> expiry;

	MailLane(MailboxConnectionManager manager, MailAccountWork owner, String name) {
		this.manager = manager;
		folders = new OpenFolder.Cache(() -> manager.clock.millis());
		this.owner = owner;
		ThreadFactory factory = Thread.ofPlatform().daemon(true)
				.name("mail-account-" + owner.id + "-" + name).factory();
		executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(), runnable -> {
			Thread thread = factory.newThread(runnable);
			this.thread = thread;
			thread.setContextClassLoader(IMAPStore.class.getClassLoader());
			return thread;
		});
	}

	<T> T execute(MailboxAccount account, MailboxConnectionManager.StoreCallback<T> work, boolean read) throws MessagingException {
		var lock = read ? owner.access.readLock() : owner.access.writeLock();
		lock.lock();
		try {
			if (owner.ended) {
				throw new MessagingException("Mail account has been removed");
			}
			folders.retainReads = read;
			OpenFolder.use(folders);
			for (int attempt = 0;; attempt++) {
				try {
					if (owner.state == AccountConnectionState.AUTH_REQUIRED) {
						throw new AuthenticationFailedException("Mail sign-in is required");
					}
					IMAPStore current = store;
					if (current == null || !account.equals(connectedAccount)
							|| (manager.clock.millis() - lastUsed >= MailboxConnectionManager.IDLE_MILLIS && !current.isConnected())) {
						closeStore();
						connect(account);
					}
					current = store;
					if (current == null) {
						throw new MessagingException("Mail connection was not created");
					}
					if (owner.state == AccountConnectionState.AUTH_REQUIRED) {
						throw new AuthenticationFailedException("Mail sign-in is required");
					}
					if (this == owner.main) {
						owner.quota.refresh(current);
					}
					return work.run(current);
				} catch (IllegalStateException e) {
					if (!"This operation is not allowed on a closed folder".equals(e.getMessage())) {
						throw e;
					}
					closeStore();
					owner.setState(AccountConnectionState.OFFLINE, "connect");
					if (!read || attempt == 1) {
						throw new MessagingException("Mail folder closed during operation", e);
					}
				} catch (AuthenticationFailedException e) {
					closeStore();
					owner.setState(AccountConnectionState.AUTH_REQUIRED, "auth");
					throw e;
				} catch (MessagingException e) {
					boolean disconnected = manager.connectionFailure(e);
					if (owner.state == AccountConnectionState.CONNECTING || disconnected) {
						closeStore();
						if (owner.state != AccountConnectionState.AUTH_REQUIRED) {
							owner.setState(AccountConnectionState.OFFLINE, "connect");
						}
					}
					if (!read || !disconnected || attempt == 1) {
						throw e;
					}
				}
			}
		} finally {
			OpenFolder.use(null);
			lastUsed = manager.clock.millis();
			scheduleExpiry();
			lock.unlock();
		}
	}

	private void scheduleExpiry() {
		if (expiry != null) {
			expiry.cancel(false);
		}
		long delay = folders.delay();
		if (!manager.stopped && !owner.ended && delay != Long.MAX_VALUE) {
			try {
				expiry = manager.idle.schedule(this::expireIdle, Math.max(1, delay), TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException e) {
				manager.log.debug("Mail idle cleanup rejected after shutdown");
			}
		}
	}

	private void connect(MailboxAccount account) throws MessagingException {
		owner.setState(AccountConnectionState.CONNECTING, "connect");
		manager.credentials.connect(account, false, null, (username, password) -> {
			IMAPStore opened = manager.sessions.createLaneStore(account);
			store = opened;
			try {
				opened.connect(account.imap().host(), account.imap().port(), username, new String(password));
			} catch (MessagingException e) {
				closeStore();
				throw e;
			}
		});
		connectedAccount = account;
		if (owner.state != AccountConnectionState.AUTH_REQUIRED) {
			owner.setState(AccountConnectionState.CONNECTED, "connect");
		}
	}

	void expireIdle() {
		try {
			executor.execute(new MailWorkTask<>(manager, () -> {
				owner.access.readLock().lock();
				try {
					folders.expire();
					if (this == owner.reader && folders.delay() == Long.MAX_VALUE) {
						closeStore();
					}
					scheduleExpiry();
				} catch (MessagingException e) {
					closeStore();
					manager.log.debug("Mail idle folder cleanup failed");
				} finally {
					owner.access.readLock().unlock();
				}
				return null;
			}, null, owner));
		} catch (RejectedExecutionException e) {
			manager.log.debug("Mail idle cleanup rejected after shutdown");
		}
	}

	void discardPrefetch() {
		executor.getQueue().removeIf(queued -> {
			if (queued instanceof MailWorkTask<?> task && task.session != null) {
				task.cancel(false);
				return true;
			}
			return false;
		});
	}

	void closeStore() {
		if (this == owner.main) {
			manager.dates.removeAccount(owner.id);
		}
		IMAPStore closing = store;
		store = null;
		connectedAccount = null;
		try {
			folders.close();
		} catch (MessagingException e) {
			manager.log.debug("Could not close mail read folder");
		}
		if (closing != null) {
			try {
				closing.close();
			} catch (MessagingException e) {
				manager.log.debug("Could not close mail work connection");
			}
		}
	}

	void shutdown() {
		discardPrefetch();
		executor.execute(new MailWorkTask<>(manager, () -> {
			closeStore();
			return null;
		}, null, owner) {
			/** {@inheritDoc} */
			@Override
			public void run() {
				super.run();
				closeStore();
			}
		});
		executor.shutdown();
	}

	void awaitShutdown() {
		try {
			if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
				return;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			manager.log.debug("Mail shutdown interrupted");
		}
		for (Runnable queued : executor.shutdownNow()) {
			if (queued instanceof Future<?> future) {
				future.cancel(false);
			}
		}
		closeStore();
	}
}
