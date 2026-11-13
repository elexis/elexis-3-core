package ch.elexis.core.mail.internal.connection;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

class MailWorkTask<T> extends FutureTask<T> implements Comparable<MailWorkTask<?>> {
	private final MailboxConnectionManager manager;
	private final long order;
	final Object session;
	private final boolean background;
	private final MailAccountWork owner;
	private final long generation;

	MailWorkTask(MailboxConnectionManager manager, Callable<T> callback, Object session, MailAccountWork owner) {
		this(manager, callback, session, owner, false);
	}

	MailWorkTask(MailboxConnectionManager manager, Callable<T> callback, Object session, MailAccountWork owner, boolean background) {
		super(callback);
		this.manager = manager;
		order = manager.sequence.getAndIncrement();
		this.background = background;
		this.session = session;
		this.owner = owner;
		generation = owner.generation;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(MailWorkTask<?> other) {
		int priority = Boolean.compare(background || session != null, other.background || other.session != null);
		return priority == 0 ? Long.compare(order, other.order) : priority;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		synchronized (manager.accounts) {
			if (generation != owner.generation || owner.ended) {
				cancel(false);
				return;
			}
			if (session != null && owner.mainPending > 0) {
				owner.deferred.add(this);
				return;
			}
		}
		super.run();
	}
}
