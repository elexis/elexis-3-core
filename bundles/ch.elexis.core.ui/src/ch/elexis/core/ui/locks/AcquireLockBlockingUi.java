package ch.elexis.core.ui.locks;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ILocalLockService;

public class AcquireLockBlockingUi {
	private static Logger logger = LoggerFactory.getLogger(AcquireLockBlockingUi.class);
	
	public static void aquireAndRun(IPersistentObject lockPo, ILockHandler handler){
		if (LocalLockServiceHolder.get().getStatus() == ILocalLockService.Status.STANDALONE) {
			handler.lockAcquired();
			return;
		}
		
		Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			
			@Override
			public void run(){
				ProgressMonitorDialog progress =
					new ProgressMonitorDialog(display.getActiveShell());
				try {
					progress.run(true, true, new AcquireLockRunnable(lockPo, handler));
				} catch (InvocationTargetException | InterruptedException e) {
					logger.warn("Exception during acquire lock.", e);
				}
			}
		});
	}
	
	public static void aquireAndRun(Identifiable identifiable, ILockHandler handler){
		Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			
			@Override
			public void run(){
				ProgressMonitorDialog progress =
					new ProgressMonitorDialog(display.getActiveShell());
				try {
					progress.run(true, true, new AcquireLockRunnable(identifiable, handler));
				} catch (InvocationTargetException | InterruptedException e) {
					logger.warn("Exception during acquire lock.", e);
				}
			}
		});
	}
	
	private static class AcquireLockRunnable implements IRunnableWithProgress {
		private IPersistentObject lockPo;
		private Identifiable lockIdentifiable;
		
		private ILockHandler lockhander;
		private LockResponse result;
		
		public AcquireLockRunnable(IPersistentObject lockPo, ILockHandler lockhander){
			this.lockPo = lockPo;
			this.lockhander = lockhander;
		}
		
		public AcquireLockRunnable(Identifiable lockIdentifiable, ILockHandler lockhander){
			this.lockIdentifiable = lockIdentifiable;
			this.lockhander = lockhander;
		}
		
		@Override
		public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException{

			if (lockPo != null) {
				result = LocalLockServiceHolder.get().acquireLockBlocking(lockPo, 30, monitor);
			} else if (lockIdentifiable != null) {
				result = LocalLockServiceHolder.get().acquireLockBlocking(lockIdentifiable, 30,
					monitor);
			}
			if (result != null) {
				Display display = Display.getDefault();
				if (result.isOk()) {
					display.syncExec(new Runnable() {
						@Override
						public void run(){
							lockhander.lockAcquired();
						}
					});
					monitor.beginTask("Releasing lock ...", IProgressMonitor.UNKNOWN);
					LocalLockServiceHolder.get().releaseLock(lockPo);
					monitor.done();
				} else {
					display.syncExec(new Runnable() {
						@Override
						public void run(){
							lockhander.lockFailed();
							LockResponseHelper.showInfo(result, lockPo, logger);
						}
					});
				}
			}
		}
	}
}
