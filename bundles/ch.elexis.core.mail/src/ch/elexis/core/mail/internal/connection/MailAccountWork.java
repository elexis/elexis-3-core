package ch.elexis.core.mail.internal.connection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.model.AccountConnectionState;

final class MailAccountWork {
	private final MailboxConnectionManager manager;
	final String id;
	final MailQuotaCache quota;
	final ReentrantReadWriteLock access = new ReentrantReadWriteLock(true);
	final MailLane main;
	final MailLane reader;
	final MailLane index;
	final List<MailWorkTask<?>> deferred = new ArrayList<>();
	volatile AccountConnectionState state = AccountConnectionState.OFFLINE;
	volatile boolean ended;
	long generation;
	int mainPending;
	boolean countRefreshPending;
	long nextCountRefresh;
	final Set<String> indexing = new HashSet<>();

	MailAccountWork(MailboxConnectionManager manager, String id) {
		this.manager = manager;
		this.id = id;
		quota = new MailQuotaCache(manager.configService, manager.clock, id);
		main = new MailLane(manager, this, "work");
		reader = new MailLane(manager, this, "reader");
		index = new MailLane(manager, this, "index");
		main.folders.peer = reader.folders;
		reader.folders.peer = main.folders;
	}

	void resumePrefetch() {
		if (mainPending == 0 && !ended && !manager.stopped) {
			for (MailWorkTask<?> task : deferred) {
				reader.executor.execute(task);
			}
			deferred.clear();
		}
	}

	void setState(AccountConnectionState next, String reason) {
		synchronized (manager.accounts) {
			if (state == AccountConnectionState.AUTH_REQUIRED && next != AccountConnectionState.DISABLED
					&& !"accounts".equals(reason)) {
				return;
			}
			if (state == next) {
				return;
			}
			state = next;
			EventAdmin events = manager.eventAdmin;
			if (events != null) {
				try {
					events.postEvent(new Event(MailEventTopics.ACCOUNT_STATE, Map.of(MailEventTopics.ACCOUNT_ID,
							id, MailEventTopics.STATE, next.name(), MailEventTopics.REASON, reason)));
				} catch (IllegalStateException e) {
					manager.log.warn("Mail state event service is unavailable");
				}
			}
		}
	}

}
