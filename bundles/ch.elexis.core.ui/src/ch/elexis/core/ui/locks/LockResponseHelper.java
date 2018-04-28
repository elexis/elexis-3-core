package ch.elexis.core.ui.locks;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.ui.util.SWTHelper;

public class LockResponseHelper {
	
	public static void showInfo(LockResponse lr, IPersistentObject object, Logger log){
		if (object == null) {
			if (log == null) {
				log = LoggerFactory.getLogger(LockResponseHelper.class);
			}
			log.warn("showInfo for null object", new Throwable());
		}
		
		if (LockResponse.Status.DENIED_PERMANENT == lr.getStatus()) {
			SWTHelper.showError(Messages.DenyLockPermanent_Title,
				Messages.DenyLockPermanent_Message);
		} else {
			if (log != null) {
				log.warn("Unable to acquire lock for "
					+ ((object != null) ? object.storeToString() : "null") + " - "
					+ lr.getLockInfo().getUser() + "@" + lr.getLockInfo().getSystemUuid());
			}
			String format = MessageFormat.format(Messages.DenyLock_Message,
				lr.getLockInfo().getUser() + "@" + lr.getLockInfo().getSystemUuid());
			SWTHelper.showError(Messages.DenyLock_Title, format);
		}
	}
	
}
