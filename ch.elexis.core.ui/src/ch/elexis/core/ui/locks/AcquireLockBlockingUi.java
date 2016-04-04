package ch.elexis.core.ui.locks;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;

public class AcquireLockBlockingUi {
	private static Logger logger = LoggerFactory.getLogger(AcquireLockBlockingUi.class);
	
	public static void aquireAndRun(IPersistentObject lockPo, Runnable runnable){
		Display display = Display.getDefault();
		
		ProgressMonitorDialog progress = new ProgressMonitorDialog(display.getActiveShell());
		try {
			progress.run(true, true, new AcquireLockRunnable(lockPo, runnable));
		} catch (InvocationTargetException | InterruptedException e) {
			logger.warn("Exception during acquire lock.", e);
		}
	}
	
	private static class AcquireLockRunnable implements IRunnableWithProgress {
		private IPersistentObject lockPo;
		private Runnable runnable;
		
		public AcquireLockRunnable(IPersistentObject lockPo, Runnable runnable){
			this.lockPo = lockPo;
			this.runnable = runnable;
		}
		
		@Override
		public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException{

			LockResponse result = CoreHub.getLocalLockService().acquireLockBlocking(lockPo, 30, monitor);
			Display display = Display.getDefault();
			if(result.isOk()) {
				monitor.beginTask("Lock aquired ...", IProgressMonitor.UNKNOWN);
				display.syncExec(runnable);
				CoreHub.getLocalLockService().releaseLock(lockPo);
				monitor.done();
			} else {
				display.syncExec(new Runnable() {
					@Override
					public void run(){
						logger.error("Could not acquire lock for [" + lockPo.getLabel()
							+ "] reason [" + result.getStatus() + "]");
						MessageDialog.openError(display.getActiveShell(), "Could not acquire Lock",
							"Could not acquire lock for [" + lockPo.getLabel() + "]");
					}
				});
			}
		}
		
	}
}
