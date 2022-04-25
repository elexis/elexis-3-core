package ch.elexis.core.ui.e4.locks;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

public class LockResponseHelper {

	public static void showInfo(LockResponse lr, Object object, Logger log) {
		if (object == null) {
			if (log == null) {
				log = LoggerFactory.getLogger(LockResponseHelper.class);
			}
			log.warn("showInfo for null object", new Throwable());
		}

		if (LockResponse.Status.DENIED_PERMANENT == lr.getStatus()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.DenyLockPermanent_Title,
					Messages.DenyLockPermanent_Message);
		} else {
			if (log != null) {
				log.warn("Unable to " + lr.getLockRequestType() + " acquire lock for "
						+ ((object != null) ? getStoreToString(object) : "null") + " - " + lr.getLockInfo().getUser()
						+ "@" + lr.getLockInfo().getSystemUuid());
			}

			String format = MessageFormat.format(Messages.DenyLock_Message, getStoreToString(object),
					lr.getLockInfo().getUser() + "@" + lr.getLockInfo().getSystemUuid());
			MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.DenyLock_Title, format);
		}
	}

	private static String getStoreToString(Object object) {
		if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
					.orElseThrow(() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
}
