package ch.elexis.core.ui.locks;

import java.text.MessageFormat;

import org.slf4j.Logger;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;

public class LockResponseHelper {
	
	public static void showInfo(LockResponse lr, IPersistentObject object, Logger log){
		if (LockResponse.Status.DENIED_PERMANENT == lr.getStatus()) {
			SWTHelper.showError(Messages.DenyLockPermanent_Title,
				Messages.DenyLockPermanent_Message);
		} else {
			if (log != null) {
				log.warn("Unable to acquire lock for " + object.storeToString() + " - "
					+ lr.getLockInfos().getUser() + "@" + lr.getLockInfos().getSystemUuid());
			}
			String format = MessageFormat.format(Messages.DenyLock_Message,
				lr.getLockInfos().getUser() + "@" + lr.getLockInfos().getSystemUuid());
			SWTHelper.showError(Messages.DenyLock_Title, format);
		}
	}
	
}
