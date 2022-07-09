package ch.elexis.core.ui.e4.locks;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;

public class AcquireLockBlockingUi {
	private static Logger logger = LoggerFactory.getLogger(AcquireLockBlockingUi.class);

	public static void aquireAndRun(Identifiable identifiable, ILockHandler handler) {
		if (ElexisServerServiceHolder.get().getConnectionStatus() == ConnectionStatus.STANDALONE) {
			handler.lockAcquired();
			return;
		}

		Display display = Display.getDefault();
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				ProgressMonitorDialog progress = new ProgressMonitorDialog(display.getActiveShell());
				try {
					progress.run(true, true, new AcquireLockRunnable(identifiable, handler));
				} catch (InvocationTargetException | InterruptedException e) {
					logger.warn("Exception during acquire lock.", e); //$NON-NLS-1$
				}
			}
		});
	}

	private static class AcquireLockRunnable implements IRunnableWithProgress {
		private Identifiable lockIdentifiable;

		private ILockHandler lockhander;
		private LockResponse result;

		public AcquireLockRunnable(Identifiable lockIdentifiable, ILockHandler lockhander) {
			this.lockIdentifiable = lockIdentifiable;
			this.lockhander = lockhander;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			result = LocalLockServiceHolder.get().acquireLockBlocking(lockIdentifiable, 30, monitor);
			if (result != null) {
				Display display = Display.getDefault();
				if (result.isOk()) {
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							lockhander.lockAcquired();
						}
					});
					monitor.beginTask("Releasing lock ...", IProgressMonitor.UNKNOWN);
					LocalLockServiceHolder.get().releaseLock(result.getLockInfo());
					monitor.done();
				} else {
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							lockhander.lockFailed();
							LockResponseHelper.showInfo(result, lockIdentifiable, logger);
						}
					});
				}
			}
		}
	}
}
