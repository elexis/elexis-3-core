package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.status.ObjectStatus;

public interface IElexisDataSource {

	public IStatus setDBConnection(DBConnection dbConnection);

	/**
	 * Get the current connection status
	 *
	 * @return <code>null</code> if no connection set, {@link ObjectStatus}
	 *         containing a {@link DBConnection} if status is ok, or the resp. error
	 *         message. The status code value is as follows:<br>
	 *         1 testdb connection, 2 environment provided connection, 3
	 *         configuration provided connection
	 */
	public @Nullable ObjectStatus getCurrentConnectionStatus();
}
