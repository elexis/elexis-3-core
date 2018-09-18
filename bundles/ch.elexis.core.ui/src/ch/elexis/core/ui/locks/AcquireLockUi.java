package ch.elexis.core.ui.locks;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;

public class AcquireLockUi {
	private static Logger logger = LoggerFactory.getLogger(AcquireLockUi.class);
	
	public static void aquireAndRun(IPersistentObject lockPo, ILockHandler lockhandler){
		Display display = Display.getDefault();
		LockResponse result = LocalLockServiceHolder.get().acquireLock(lockPo);
		if (result.isOk()) {
			
			display.syncExec(new Runnable() {
				@Override
				public void run(){
					lockhandler.lockAcquired();
				}
			});
			LocalLockServiceHolder.get().releaseLock(lockPo);
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
