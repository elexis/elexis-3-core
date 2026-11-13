package ch.elexis.core.mail.internal.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.protocol.Status;
import org.eclipse.angus.mail.util.DecodingException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.internal.account.MailCredentialProvider;
import ch.elexis.core.mail.internal.account.PasswordCredentialProvider;
import ch.elexis.core.mail.internal.oauth.OAuth2TokenCache;
import ch.elexis.core.mail.model.AccountConnectionState;
import ch.elexis.core.mail.model.MailQuota;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.FolderClosedException;
import jakarta.mail.MessagingException;
import jakarta.mail.StoreClosedException;

/** Coordinates independent main, reader and date index IMAP lanes per account, with shared authentication state. */
@Singleton
@Component(service = { MailboxConnectionManager.class, EventHandler.class },
		property = "event.topics=" + MailEventTopics.ACCOUNTS)
public class MailboxConnectionManager implements EventHandler {

	@Inject
	@Reference
	IConfigService configService;
	@Inject
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	volatile EventAdmin eventAdmin;

	final Logger log = LoggerFactory.getLogger(getClass());
	final MailSessionFactory sessions = new MailSessionFactory(
			id -> Boolean.parseBoolean(configService.get(MailClientConfig.legacy(id), "false")));
	public final MailDateIndex dates = new MailDateIndex();
	final Map<String, MailAccountWork> accounts = new HashMap<>();
	@Inject
	@Reference
	private OAuth2TokenCache tokens;
	MailCredentialProvider credentials;
	boolean stopped;
	Clock clock = Clock.systemUTC();
	static final long IDLE_MILLIS = 60_000;
	private static final int INDEX_CHUNK = 5000;
	private static final ThreadLocal<BooleanSupplier> CURRENT_READ = new ThreadLocal<>();
	final AtomicLong sequence = new AtomicLong();
	final ScheduledExecutorService idle = Executors.newSingleThreadScheduledExecutor(
			Thread.ofPlatform().daemon(true).name("mail-idle-cleanup").factory());

	private final GmailLabelCounts gmailCounts = new GmailLabelCounts(this);

	@PostConstruct
	@Activate
	void activate() {
		MailcapRegistration.register();
		CredentialCipher cipher = new CredentialCipher(() -> configService.get(MailClientConfig.SECRET_SALT, null),
				salt -> configService.set(MailClientConfig.SECRET_SALT, salt, false));
		credentials = new MailCredentialProvider(new PasswordCredentialProvider(configService, cipher,
				id -> modelService.load(id, IUser.class), id -> modelService.load(id, IContact.class)), tokens);
	}

	/** A unit of work that uses the connected store exclusively until it returns. */
	@FunctionalInterface
	public interface StoreCallback<T> {
		/**
		 * Performs one operation on the account's work thread.
		 * @param store the connected store
		 * @return the operation result
		 * @throws MessagingException if the operation fails
		 */
		T run(IMAPStore store) throws MessagingException;
	}

	/**
	 * Runs blocking work on the lazily created account executor, reusing its store.
	 * @param <T> the result type
	 * @param account the visible account to access
	 * @param work the non-retryable operation; scoped folders must be closed before returning
	 * @return the operation result
	 * @throws MessagingException if connection, execution or waiting fails
	 */
	public <T> T execute(MailboxAccount account, StoreCallback<T> work) throws MessagingException {
		MailLane current = currentLane(account.id(), false);
		return current == null ? await(submit(account, work, false, false))
				: current.execute(account, work, false);
	}

	/**
	 * Executes a retryable read on the main lane.
	 * @param <T> result type
	 * @param account visible account
	 * @param work read-only operation
	 * @return operation result
	 * @throws MessagingException if both connection attempts fail
	 */
	public <T> T executeRead(MailboxAccount account, StoreCallback<T> work) throws MessagingException {
		MailLane current = currentLane(account.id(), false);
		return current == null ? await(submit(account, work, false, true))
				: current.execute(account, work, true);
	}

	/**
	 * Executes a retryable user read on the independent reader lane.
	 * @param <T> result type
	 * @param account visible account
	 * @param work read-only operation
	 * @return operation result
	 * @throws MessagingException if both connection attempts fail
	 */
	public <T> T executeReader(MailboxAccount account, StoreCallback<T> work) throws MessagingException {
		MailLane current = currentLane(account.id(), true);
		return current == null ? await(submit(account, work, true, true))
				: current.execute(account, work, true);
	}

	/**
	 * Replaces queued speculative reads for this session and account, without waiting for I/O.
	 * @param account visible account
	 * @param session opaque session identity used only for replacement
	 * @param work individual speculative reads, each using PEEK
	 */
	public void prefetch(MailboxAccount account, Object session, List<StoreCallback<Void>> work) {
		synchronized (accounts) {
			if (stopped) {
				return;
			}
			MailAccountWork worker = accounts.computeIfAbsent(account.id(), id -> new MailAccountWork(this, id));
			worker.reader.executor.getQueue().removeIf(queued -> {
				if (queued instanceof MailWorkTask<?> task && task.session == session) {
					task.cancel(false);
					return true;
				}
				return false;
			});
			worker.deferred.removeIf(task -> task.session == session);
			for (StoreCallback<Void> callback : work) {
				MailWorkTask<Void> task = new MailWorkTask<>(this, () -> {
					try {
						worker.reader.execute(account, callback, true);
					} catch (MessagingException | RuntimeException e) {
						log.debug("Mail prefetch failed");
					}
					return null;
				}, session, worker);
				worker.reader.executor.execute(task);
			}
		}
	}

	/**
	 * Queues a write before returning, preserving order with later explicit user writes.
	 * @param account the visible account
	 * @param work the same write callback used by synchronous operations
	 * @param failed reconciliation callback after any asynchronous failure
	 * @throws MessagingException if queueing is unavailable
	 */
	public void enqueueWrite(MailboxAccount account, StoreCallback<Void> work, Runnable failed)
			throws MessagingException {
		submit(account, work, false, false, failed);
	}

	/**
	 * Enqueues a request envelope without opening a store or occupying a shared waiting thread.
	 * @param <T> the response type
	 * @param account the visible account
	 * @param reader whether to use the reader lane
	 * @param request the account-local callback
	 * @return a future completed by the lane, including cancellation and rejection
	 */
	public <T> CompletableFuture<T> submitRequest(MailboxAccount account, boolean reader, Supplier<T> request) {
		return submitRequest(account.id(), reader, request);
	}

	/**
	 * Queues an envelope without reading account configuration on the calling thread.
	 * @param <T> the result type
	 * @param accountId the routing key, not an authorization grant
	 * @param reader whether to use the reader lane
	 * @param request callback that must check visibility before accessing account data
	 * @return the queued result
	 */
	public <T> CompletableFuture<T> submitRequest(String accountId, boolean reader, Supplier<T> request) {
		CompletableFuture<T> result = new CompletableFuture<>();
		synchronized (accounts) {
			if (stopped) {
				return CompletableFuture.failedFuture(new RejectedExecutionException("Mail manager is disabled"));
			}
			MailAccountWork worker = accounts.computeIfAbsent(accountId, id -> new MailAccountWork(this, id));
			MailLane lane = reader ? worker.reader : worker.main;
			if (!reader) {
				worker.mainPending++;
			}
			MailWorkTask<T> task = new MailWorkTask<>(this, request::get, null, worker) {
				@Override
				protected void done() {
					if (!reader) {
						synchronized (accounts) {
							worker.mainPending--;
							worker.resumePrefetch();
						}
					}
					try {
						result.complete(await(this));
					} catch (MessagingException | RuntimeException | Error e) {
						result.completeExceptionally(e);
					}
				}
			};
			try {
				lane.executor.execute(task);
			} catch (RejectedExecutionException e) {
				log.debug("Mail request rejected after shutdown");
				task.cancel(false);
			}
		}
		return result;
	}

	/**
	 * Runs bounded write steps, yielding to user work between steps and stopping after a reset or failure.
	 * @param <T> the non-null final result type
	 * @param accountId the routing key
	 * @param account visibility-checked configuration, resolved on every step
	 * @param step one non-retryable step; null requests another step
	 * @return the final result or the original operation failure
	 */
	public <T> CompletableFuture<T> submitSteps(String accountId, Supplier<MailboxAccount> account,
			StoreCallback<T> step) {
		CompletableFuture<T> result = new CompletableFuture<>();
		synchronized (accounts) {
			if (stopped) {
				return CompletableFuture.failedFuture(new RejectedExecutionException("Mail manager is disabled"));
			}
			MailAccountWork worker = accounts.computeIfAbsent(accountId, id -> new MailAccountWork(this, id));
			writeStep(worker, worker.generation, account, step, result, false);
		}
		return result;
	}

	private <T> void writeStep(MailAccountWork worker, long generation, Supplier<MailboxAccount> account,
			StoreCallback<T> step, CompletableFuture<T> result, boolean background) {
		if (stopped || worker.ended || generation != worker.generation || result.isDone()) {
			result.completeExceptionally(new CancellationException("Mail operation was cancelled"));
			return;
		}
		MailWorkTask<T> task = new MailWorkTask<>(this, () -> execute(account.get(), step), null, worker, background) {
			@Override
			protected void done() {
				try {
					T value = await(this);
					if (value != null) {
						result.complete(value);
					} else {
						synchronized (accounts) {
							writeStep(worker, generation, account, step, result, true);
						}
					}
				} catch (MessagingException | RuntimeException | Error e) {
					result.completeExceptionally(e);
				}
			}
		};
		try {
			worker.main.executor.execute(task);
		} catch (RejectedExecutionException e) {
			log.debug("Mail write step rejected after shutdown");
			task.cancel(false);
		}
	}

	/**
	 * Scopes a cancellable read to its account-lane callback, restoring any outer scope.
	 * @param <T> the response type
	 * @param current whether the requesting view is still current
	 * @param request the read callback
	 * @return the callback result
	 * @throws CancellationException if the view has been superseded
	 */
	public static <T> T readRequest(BooleanSupplier current, Supplier<T> request) {
		BooleanSupplier previous = CURRENT_READ.get();
		CURRENT_READ.set(current);
		try {
			checkReadRequest();
			return request.get();
		} finally {
			if (previous == null) {
				CURRENT_READ.remove();
			} else {
				CURRENT_READ.set(previous);
			}
		}
	}

	/**
	 * Stops an obsolete read between IMAP commands without interrupting writes or closing shared stores.
	 * @throws CancellationException if the scoped view has been superseded
	 */
	public static void checkReadRequest() {
		BooleanSupplier current = CURRENT_READ.get();
		if (current != null && !current.getAsBoolean()) {
			throw new CancellationException("Mail read was superseded");
		}
	}

	MailLane currentLane(String accountId, boolean reader) {
		synchronized (accounts) {
			MailAccountWork worker = accounts.get(accountId);
			MailLane lane = worker == null ? null : reader ? worker.reader : worker.main;
			return lane != null && lane.thread == Thread.currentThread() ? lane : null;
		}
	}

	/**
	 * Starts background date indexing in bounded steps unless the folder is already being indexed or the manager is stopped.
	 * @param account the account to access
	 * @param folderName the full folder name
	 */
	public void buildDateIndex(MailboxAccount account, String folderName) {
		synchronized (accounts) {
			if (stopped) {
				return;
			}
			MailAccountWork worker = accounts.computeIfAbsent(account.id(), id -> new MailAccountWork(this, id));
			if (worker.indexing.add(folderName)) {
				indexStep(worker, account, folderName);
			}
		}
	}

	private void indexStep(MailAccountWork worker, MailboxAccount account, String folderName) {
		MailWorkTask<Boolean> task = new MailWorkTask<>(this, () -> worker.index.execute(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, folderName, false, false)) {
				boolean done = dates.ready(account.id(), opened.folder())
						|| dates.buildStep(account.id(), opened.folder(),
								store.hasCapability("X-GM-EXT-1") ? 50 : INDEX_CHUNK);
				if (done) {
					folderChanged(account.id(), folderName);
				}
				return done;
			}
		}, true), null, worker, true) {
			@Override
			protected void done() {
				boolean finished = true;
				try {
					finished = !Boolean.FALSE.equals(await(this));
				} catch (MessagingException | RuntimeException e) {
					log.debug("Mail background date index failed");
				}
				boolean idle = false;
				synchronized (accounts) {
					if (finished || stopped || worker.ended) {
						worker.indexing.remove(folderName);
						idle = worker.indexing.isEmpty();
					} else {
						indexStep(worker, account, folderName);
					}
				}
				if (idle) {
					worker.index.closeStore();
				}
			}
		};
		try {
			worker.index.executor.execute(task);
		} catch (RejectedExecutionException e) {
			log.debug("Mail date index rejected after shutdown");
			task.cancel(false);
		}
	}

	private <T> Future<T> submit(MailboxAccount account, StoreCallback<T> work, boolean reader, boolean read)
			throws MessagingException {
		return submit(account, work, reader, read, null);
	}

	private <T> Future<T> submit(MailboxAccount account, StoreCallback<T> work, boolean reader, boolean read,
			Runnable failed) throws MessagingException {
		synchronized (accounts) {
			if (stopped) {
				throw new MessagingException("Mail connection manager is disabled");
			}
			MailAccountWork worker = accounts.computeIfAbsent(account.id(), id -> new MailAccountWork(this, id));
			MailLane lane = reader ? worker.reader : worker.main;
			if (!reader) {
				worker.mainPending++;
			}
			MailWorkTask<T> task = new MailWorkTask<>(this, () -> lane.execute(account, work, read), null, worker) {
				@Override
				protected void done() {
					if (failed != null) {
						try {
							await(this);
						} catch (MessagingException | RuntimeException e) {
							log.debug("Mail open seen update failed");
							failed.run();
						}
					}
					if (!reader) {
						synchronized (accounts) {
							worker.mainPending--;
							worker.resumePrefetch();
						}
					}
				}
			};
			try {
				lane.executor.execute(task);
			} catch (RejectedExecutionException e) {
				task.cancel(false);
				log.debug("Mail operation rejected after shutdown");
				throw new MessagingException("Mail account executor has stopped", e);
			}
			return task;
		}
	}

	/**
	 * Waits for an operation result, preserving runtime failures and wrapping cancellation, interruption and other failures.
	 * @param <T> the result type
	 * @param future the pending operation
	 * @return the operation result
	 * @throws MessagingException if waiting fails or the operation reports a checked failure
	 */
	public <T> T await(Future<T> future) throws MessagingException {
		try {
			return future.get();
		} catch (CancellationException e) {
			throw new MessagingException("Mail operation cancelled during shutdown", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new MessagingException("Interrupted while waiting for mail operation", e);
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
			throw new MessagingException("Mail operation failed", e);
		}
	}

	/**
	 * Returns the last known state without creating a thread or opening a connection.
	 * @param accountId the account id
	 * @return OFFLINE before first use, DISABLED after deactivation, or the last connection state
	 */
	public AccountConnectionState getState(String accountId) {
		synchronized (accounts) {
			MailAccountWork worker = accounts.get(accountId);
			return stopped ? AccountConnectionState.DISABLED
					: worker == null ? AccountConnectionState.OFFLINE : worker.state;
		}
	}

	/**
	 * Returns the session quota or persisted observation without opening a connection.
	 * @param accountId the account already checked for visibility by the caller
	 * @return the last quota or UNKNOWN
	 */
	public MailQuota getQuota(String accountId) {
		synchronized (accounts) {
			MailAccountWork worker = accounts.get(accountId);
			return worker == null ? MailQuotaCache.stored(configService, MailClientConfig.quota(accountId))
					: worker.quota.get();
		}
	}

	/**
	 * Announces a folder whose content this client changed itself, for example by storing a draft.
	 * @param accountId the account id
	 * @param folder the folder full name
	 */
	public void folderChanged(String accountId, String folder) {
		requestGmailCounts(accountId);
		EventAdmin events = eventAdmin;
		if (events != null) {
			try {
				events.postEvent(new Event(MailEventTopics.CHANGED,
						Map.of(MailEventTopics.ACCOUNT_ID, accountId, MailEventTopics.FOLDER, folder)));
			} catch (IllegalStateException e) {
				log.warn("Mail change event service is unavailable");
			}
		}
	}

	/**
	 * Announces a batch and reads counts using STATUS on the account worker.
	 * @param accountId the visible account
	 * @param folder the source or destination folder
	 * @param uids the affected source UIDs, even for the destination event
	 * @param changeId optional page correlation identifier
	 * @throws MessagingException if counts cannot be read
	 */
	public void folderChanged(String accountId, IMAPFolder folder, long[] uids, String changeId)
			throws MessagingException {
		requestGmailCounts(accountId);
		EventAdmin events = eventAdmin;
		if (events == null) {
			return;
		}
		Map<String, Object> changed = new HashMap<>();
		changed.put(MailEventTopics.ACCOUNT_ID, accountId);
		changed.put(MailEventTopics.FOLDER, folder.getFullName());
		changed.put(MailEventTopics.UIDS, uids.clone());
		if (changeId != null) {
			changed.put(MailEventTopics.CHANGE_ID, changeId);
		}
		Status counts = (Status) folder.doCommand(protocol ->
				protocol.status(folder.getFullName(), new String[] { "MESSAGES", "UNSEEN" }));
		int unread = counts.unseen;
		int total = counts.total;
		try {
			events.postEvent(new Event(MailEventTopics.CHANGED, changed));
			events.postEvent(new Event(MailEventTopics.COUNTS, Map.of(MailEventTopics.ACCOUNT_ID, accountId,
					MailEventTopics.FOLDER, folder.getFullName(), MailEventTopics.UNREAD, unread,
					MailEventTopics.TOTAL, total)));
		} catch (IllegalStateException e) {
			log.warn("Mail change event service is unavailable");
		}
	}

	/**
	 * Requests a count refresh when called on the account main lane with a connected Gmail-capable store.
	 * @param accountId the account id
	 */
	public void requestGmailCounts(String accountId) {
		gmailCounts.requestGmailCounts(accountId);
	}

	/**
	 * Schedules a coalesced folder count refresh on the main lane when the manager and event service are available.
	 * @param account the account whose folder counts should be refreshed
	 */
	public void refreshGmailCounts(MailboxAccount account) {
		gmailCounts.refreshGmailCounts(account);
	}

	/**
	 * Publishes confirmed intermediate counts without another server round trip or content-change event.
	 * @param accountId the visible account
	 * @param folder the folder full name
	 * @param total the message count captured at batch start
	 * @param unread the remaining unread snapshot count
	 */
	public void folderCounts(String accountId, String folder, int total, int unread) {
		EventAdmin events = eventAdmin;
		if (events != null) {
			try {
				events.postEvent(new Event(MailEventTopics.COUNTS, Map.of(MailEventTopics.ACCOUNT_ID, accountId,
						MailEventTopics.FOLDER, folder, MailEventTopics.UNREAD, unread, MailEventTopics.TOTAL, total)));
			} catch (IllegalStateException e) {
				log.warn("Mail count event service is unavailable");
			}
		}
	}

	/**
	 * Queues cleanup on both lanes, rejecting queued work from before the configuration change.
	 * @param event the account configuration or credential event
	 */
	@Override
	public void handleEvent(Event event) {
		Object value = event.getProperty(MailEventTopics.ACCOUNT_ID);
		if (!MailEventTopics.ACCOUNTS.equals(event.getTopic()) || !(value instanceof String id)) {
			return;
		}
		if (tokens != null) {
			tokens.clear(id);
		}
		synchronized (accounts) {
			MailAccountWork worker = accounts.get(id);
			if (worker == null || stopped) {
				return;
			}
			worker.generation++;
			worker.countRefreshPending = false;
			worker.nextCountRefresh = 0;
			worker.reader.discardPrefetch();
			worker.deferred.clear();
			AtomicBoolean reset = new AtomicBoolean();
			Callable<Void> cleanup = () -> {
				worker.access.writeLock().lock();
				try {
					if (!reset.compareAndSet(false, true)) {
						return null;
					}
					worker.main.closeStore();
					worker.reader.closeStore();
					worker.index.closeStore();
					worker.setState(AccountConnectionState.OFFLINE, "accounts");
					if (configService.get(MailClientConfig.account(id), null) == null) {
						synchronized (accounts) {
							worker.ended = true;
							accounts.remove(id, worker);
							worker.main.executor.shutdown();
							worker.reader.executor.shutdown();
							worker.index.executor.shutdown();
						}
					}
				} finally {
					worker.access.writeLock().unlock();
				}
				return null;
			};
			worker.main.executor.execute(new MailWorkTask<>(this, cleanup, null, worker));
			worker.reader.executor.execute(new MailWorkTask<>(this, cleanup, null, worker));
		}
	}

	@PreDestroy
	@Deactivate
	void deactivate() {
		ArrayList<MailAccountWork> workers;
		synchronized (accounts) {
			stopped = true;
			idle.shutdownNow();
			workers = new ArrayList<>(accounts.values());
			for (MailAccountWork worker : workers) {
				worker.ended = true;
				worker.deferred.clear();
				worker.main.shutdown();
				worker.reader.shutdown();
				worker.index.shutdown();
			}
		}
		for (MailAccountWork worker : workers) {
			worker.main.awaitShutdown();
			worker.reader.awaitShutdown();
			worker.index.awaitShutdown();
			worker.setState(AccountConnectionState.DISABLED, "disabled");
		}
	}

	/**
	 * Creates and connects a separate notification store, passing each new store to the callback before connecting.
	 * @param account the account to connect
	 * @param created the callback receiving each newly created store
	 * @return the connected notification store
	 * @throws MessagingException if the manager is stopped, authentication fails or no store is connected
	 */
	public IMAPStore openNotificationStore(MailboxAccount account, Consumer<IMAPStore> created) throws MessagingException {
		synchronized (accounts) {
			if (stopped) {
				throw new MessagingException("Mail connection manager is disabled");
			}
		}
		IMAPStore[] opened = new IMAPStore[1];
		credentials.connect(account, false, null, (username, password) -> {
			IMAPStore store = sessions.createStore(account);
			opened[0] = store;
			created.accept(store);
			try {
				store.connect(account.imap().host(), account.imap().port(), username, new String(password));
			} catch (MessagingException e) {
				try {
					store.close();
				} catch (MessagingException closing) {
					log.debug("Mail notification connection cleanup failed");
				}
				throw e;
			}
		});
		IMAPStore result = opened[0];
		if (result == null) {
			throw new MessagingException("Mail notification connection was not created");
		}
		return result;
	}

	/**
	 * Marks the account as requiring authentication unless the manager is stopped.
	 * @param account the account requiring authentication
	 */
	public void notificationAuthRequired(MailboxAccount account) {
		synchronized (accounts) {
			if (!stopped) {
				accounts.computeIfAbsent(account.id(), id -> new MailAccountWork(this, id))
						.setState(AccountConnectionState.AUTH_REQUIRED, "auth");
			}
		}
	}

	boolean connectionFailure(Throwable failure) {
		for (Throwable current = failure; current != null; current = current.getCause()) {
			if (current instanceof DecodingException || current instanceof CharacterCodingException
					|| current instanceof UnsupportedEncodingException) {
				return false;
			}
			if (current instanceof StoreClosedException || current instanceof FolderClosedException
					|| current instanceof MailConnectException || current instanceof IOException) {
				return true;
			}
		}
		return false;
	}


}
