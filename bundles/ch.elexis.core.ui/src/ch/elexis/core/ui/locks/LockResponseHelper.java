package ch.elexis.core.ui.locks;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;

public class LockResponseHelper {
	
	public static void showInfo(LockResponse lr, Object object, Logger log){
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
					+ ((object != null) ? getStoreToString(object) : "null") + " - "
					+ lr.getLockInfo().getUser() + "@" + lr.getLockInfo().getSystemUuid());
			}
			String format = MessageFormat.format(Messages.DenyLock_Message,
				lr.getLockInfo().getUser() + "@" + lr.getLockInfo().getSystemUuid());
			SWTHelper.showError(Messages.DenyLock_Title, format);
		}
	}
	
	private static String getStoreToString(Object object){
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).storeToString();
		} else if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
				.orElseThrow(
					() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
}
