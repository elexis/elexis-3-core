package ch.elexis.core.ui.text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.locks.LockResponseHelper;

/**
 * This class handles transparent locking request and release on modification of
 * the consultation text. After the initial modification, the lock will be held
 * for 1 minute, and if by the end the text is still locked, release the lock
 */
class TransparentTextModificationLockHandler implements VerifyKeyListener {

	private static final int LOCK_WINDOW_SECS = 60;

	private Logger logger;
	private ScheduledExecutorService executor;
	private EnhancedTextField enhancedTextField;

	private UnlockRunnable currentUnlockRunnable;

	public TransparentTextModificationLockHandler(EnhancedTextField enhancedTextField) {
		this.enhancedTextField = enhancedTextField;
		executor = Executors.newSingleThreadScheduledExecutor();
		logger = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void verifyKey(VerifyEvent event) {
		if (SWT.ARROW_LEFT == event.keyCode || SWT.ARROW_RIGHT == event.keyCode || SWT.ARROW_UP == event.keyCode
				|| SWT.ARROW_DOWN == event.keyCode) {
			event.doit = true;
			return;
		}

		final IEncounter encounter = enhancedTextField.getEncounter();
		if (encounter != null) {
			if (LocalLockServiceHolder.get().isLockedLocal(encounter)) {
				if (currentUnlockRunnable != null) {
					currentUnlockRunnable.setSeconds(LOCK_WINDOW_SECS);
				}
			} else {
				BusyIndicator.showWhile(enhancedTextField.getDisplay(), () -> {
					final LockResponse lockResponse = LocalLockServiceHolder.get().acquireLockBlocking(encounter, 1,
							new NullProgressMonitor());
					if (!lockResponse.isOk()) {
						event.doit = false;
						// TODO reload kons! -> needs resp events
						LockResponseHelper.showInfo(lockResponse, encounter, logger);
						return;
					}
				});
				currentUnlockRunnable = new UnlockRunnable(encounter, LOCK_WINDOW_SECS);
				executor.schedule(currentUnlockRunnable, 1, TimeUnit.SECONDS);
			}
		}
		event.doit = true;
	}

	private class UnlockRunnable implements Runnable {

		private IEncounter encounter;

		private int seconds;

		public UnlockRunnable(IEncounter encounter, int seconds) {
			this.encounter = encounter;
			this.seconds = seconds;
			logger.info("LOCK [" + encounter.getId() + "]");
		}
		
		public void setSeconds(int seconds) {
			this.seconds = seconds;
		}

		@Override
		public void run() {
			if (LocalLockServiceHolder.get().isLockedLocal(encounter)) {
				if (seconds > 0) {
					seconds--;
					executor.schedule(this, 1, TimeUnit.SECONDS);
				} else {
					// maybe it was already released
					if (LocalLockServiceHolder.get().isLockedLocal(encounter)) {
						logger.info("UNLOCK [" + encounter.getId() + "]");
						LockResponse releaseLock = LocalLockServiceHolder.get().releaseLock(encounter);
						if (!releaseLock.isOk()) {
							LockResponseHelper.showInfo(releaseLock, encounter, logger);
						}
					} else {
						logger.info("NO LOCK [" + encounter.getId() + "]");
					}
				}
			} else {
				logger.info("UNLOCKED [" + encounter.getId() + "]");
			}
		}
	}
}
