package ch.elexis.core.common;

import org.slf4j.LoggerFactory;

/**
 * Exceptions occurring within a Thread are not visible. This runnable asserts
 * that such events are logged appropriately, allowing to find programmatic
 * errors.
 *
 * @since 3.10
 */
public class CatchingRunnable implements Runnable {

	private final Runnable delegate;

	public CatchingRunnable(Runnable delegate) {
		if (delegate == null) {
			throw new NullPointerException();
		}
		this.delegate = delegate;
	}

	@Override
	public void run() {
		try {
			delegate.run();
		} catch (Exception | Error e) {
			LoggerFactory.getLogger(delegate.getClass()).error("", e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

}
