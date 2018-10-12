package ch.elexis.core.services;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.common.DBConnection;

public interface IElexisDataSource {
	
	public IStatus setDBConnection(DBConnection dbConnection);
}
