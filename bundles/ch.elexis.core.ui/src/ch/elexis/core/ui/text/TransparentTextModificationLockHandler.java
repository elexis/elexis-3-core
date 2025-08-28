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

	private static final long LOCK_WINDOW_SECS = 60;

	private Logger logger;
	private ScheduledExecutorService executor;
	private EnhancedTextField enhancedTextField;

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
		if (!enhancedTextField.isUnlocked() && encounter != null && !LocalLockServiceHolder.get().isLocked(encounter)) {
			BusyIndicator.showWhile(enhancedTextField.getDisplay(), () -> {
				final LockResponse lockResponse = LocalLockServiceHolder.get().acquireLockBlocking(encounter, 1,
						new NullProgressMonitor());
				if (!lockResponse.isOk()) {
					event.doit = false;
					// TODO reload kons! -> needs resp events
					LockResponseHelper.showInfo(lockResponse, encounter, logger);
					return;
				}
				// lock event unlocks the text field, so we can directly execute the
				// modification
				executor.schedule(() -> {
					// maybe it was already released
					if (LocalLockServiceHolder.get().isLocked(encounter)) {
						LockResponse releaseLock = LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
						if (!releaseLock.isOk()) {
							LockResponseHelper.showInfo(releaseLock, encounter, logger);
						}
					}
				}, LOCK_WINDOW_SECS, TimeUnit.SECONDS);

			});
		}
		event.doit = true;
	}
}
