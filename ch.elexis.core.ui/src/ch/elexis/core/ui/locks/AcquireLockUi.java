package ch.elexis.core.ui.locks;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;

public class AcquireLockUi {
	private static Logger logger = LoggerFactory.getLogger(AcquireLockUi.class);
	
	public static void aquireAndRun(IPersistentObject lockPo, ILockHandler lockhandler){
		Display display = Display.getDefault();
		LockResponse result = CoreHub.getLocalLockService().acquireLock(lockPo);
		if (result.isOk()) {
			
			display.syncExec(new Runnable() {
				@Override
				public void run(){
					lockhandler.lockAcquired();
				}
			});
			CoreHub.getLocalLockService().releaseLock(lockPo);
		} else {
			
			display.syncExec(new Runnable() {
				@Override
				public void run(){
					lockhandler.lockFailed();
					LockResponseHelper.showInfo(result, lockPo, logger);
				}
			});
		}
	}
}
