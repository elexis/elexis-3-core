package ch.elexis.core.data.util;

import java.util.HashMap;
import java.util.Optional;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IPersistentObject;
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
	
	/**
	 * Create a new LocalLock, the lock is not written to the DB. Use {@link LocalLock#tryLock()} to
	 * write the lock to the DB.
	 * 
	 * @param object
	 */
	public LocalLock(Object object){
		this.lockObject = object;
		if (object instanceof IPersistentObject) {
			lockString = "local_" + ((IPersistentObject) object).storeToString() + "_lock"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (object instanceof String) {
			lockString = "local_" + (String) object + "_lock";
		}
	}
	
	/**
	 * Try to write the lock to the DB if there is not already a lock on the object.
	 * 
	 * @return true if lock written, false if there is already a lock in the DB.
	 */
	public boolean tryLock(){
		synchronized (LocalLock.class) {
			Stm statement = PersistentObject.getDefaultConnection().getStatement();
			try {
				String existing = statement.queryString(
					"SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
				if (existing != null && !existing.isEmpty()) {
					return false;
				} else {
					StringBuilder sb = new StringBuilder();
					String user = (CoreHub.actUser != null) ? CoreHub.actUser.getLabel() : "system";
					sb.append("INSERT INTO CONFIG (param,wert) VALUES (") //$NON-NLS-1$
						.append(JdbcLink.wrap(lockString)).append(",") //$NON-NLS-1$
						.append(JdbcLink.wrap(
							"[" + user + "]@" + System.currentTimeMillis())) //$NON-NLS-1$//$NON-NLS-2$
						.append(")"); //$NON-NLS-1$
					statement.exec(sb.toString());
					managedLocks.put(lockObject, this);
					return true;
				}
			} finally {
				PersistentObject.getDefaultConnection().releaseStatement(statement);
			}
		}
	}
	
	/**
	 * Checks if the user has the lock
	 * 
	 * @return true if the user has the lock otherwise return false
	 */
	public boolean hasLock(String userName){
		synchronized (LocalLock.class) {
			Stm statement = PersistentObject.getDefaultConnection().getStatement();
			try {
				String existing = statement.queryString(
					"SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
				if (existing != null && !existing.isEmpty()) {
					return existing.startsWith("[" + userName + "]@");
				}
				return false;
			} finally {
				PersistentObject.getDefaultConnection().releaseStatement(statement);
			}
		}
	}
	
	/**
	 * Delete the lock from the DB. <b>Always</b> deletes the lock, even if another instance created
	 * the lock. Can be used to remove pending locks.
	 */
	public void unlock(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			statement.exec("DELETE FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			managedLocks.remove(lockObject);
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
	}
	
	/**
	 * Get the message from the DB. Currently username only.
	 * 
	 * @return the username or ? if there is no information
	 */
	public String getLockMessage(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			String existing = statement
				.queryString("SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			if (existing != null && !existing.isEmpty()) {
				String[] parts = existing.split("@");
				if (parts.length > 0) {
					return parts[0];
				}
			}
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
		return "?"; //$NON-NLS-1$
	}
	
	/**
	 * Get the value of {@link System#currentTimeMillis()} when the Lock was created.
	 * 
	 * @return the value of -1 if there is no information
	 */
	public long getLockCurrentMillis(){
		Stm statement = PersistentObject.getDefaultConnection().getStatement();
		try {
			String existing = statement
				.queryString("SELECT wert FROM CONFIG WHERE param=" + JdbcLink.wrap(lockString)); //$NON-NLS-1$
			if (existing != null && !existing.isEmpty()) {
				String[] parts = existing.split("@");
				if (parts.length > 1) {
					return Long.parseLong(parts[1]);
				}
			}
		} finally {
			PersistentObject.getDefaultConnection().releaseStatement(statement);
		}
		return -1; //$NON-NLS-1$
	}
	
	/**
	 * Test if there this instance has a lock on the object. Does <b>not</b> query the DB, only a
	 * lookup in a local {@link HashMap} is performed. If lookup in the DB is needed use
	 * {@link LocalLock#tryLock()}.
	 * 
	 * @param object
	 * @return
	 */
	public static Optional<LocalLock> getManagedLock(Object object){
		return Optional.ofNullable(managedLocks.get(object));
	}
}
