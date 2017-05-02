package ch.elexis.core.data.util;

import java.util.HashMap;
import java.util.Optional;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * This is a lock implementation based on entries in the config DB table. <b>This implementation is
 * not guaranteed to give only one lock for an object if two instances try to lock at the same
 * time.</b> If there is a server based locking implementation available (Elexis >3.1), that
 * implementation should be used instead.
 * 
 * @since 3.1
 * @author thomas
 *
 */
public class LocalLock {
	
	private static HashMap<Object, LocalLock> managedLocks = new HashMap<>();
	
	private String lockString;
	private Object lockObject;
	
	public LocalLock(Object object){
		this.lockObject = object;
		if (object instanceof IPersistentObject) {
			lockString = "local_" + ((IPersistentObject) object).storeToString() + "_lock"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public boolean tryLock(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			String existing = statement
				.queryString("SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			if (existing != null && !existing.isEmpty()) {
				return false;
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO CONFIG (param,wert) VALUES (") //$NON-NLS-1$
					.append(JdbcLink.wrap(lockString)).append(",") //$NON-NLS-1$
					.append(JdbcLink.wrap("[" + CoreHub.actUser.getLabel() + "]")).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				statement.exec(sb.toString());
				managedLocks.put(lockObject, this);
				return true;
			}
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
	}
	
	public void unlock(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			statement.exec("DELETE FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			managedLocks.remove(lockObject);
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
	}
	
	public String getLockMessage(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			String existing = statement
				.queryString("SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			if (existing != null && !existing.isEmpty()) {
				return existing;
			}
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
		return ""; //$NON-NLS-1$
	}
	
	public static Optional<LocalLock> getManagedLock(Object object){
		return Optional.ofNullable(managedLocks.get(object));
	}
}
