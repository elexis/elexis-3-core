/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich, MEDEVIT and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT <office@medevit.at> - re-implementation
 *******************************************************************************/

package ch.elexis.core.ui.wizards;

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObject.IClassResolver;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class DBConnectWizard extends Wizard {
	private List<DBConnection> storedConnectionList;
	private DBConnection targetedConnection;
	private boolean restartAfterChange = false;
	
	private DBConnectWizardPage dbConnSelectionPage;
	private DBConnectWizardPage dbConnNewConnPage;

	public DBConnectWizard(){
		super();
		setWindowTitle(Messages.DBConnectWizard_connectDB);
		dbConnSelectionPage =
			new DBConnectSelectionConnectionWizardPage(Messages.DBConnectWizard_typeOfDB);
		dbConnNewConnPage = new DBConnectNewOrEditConnectionWizardPage();
		initStoredJDBCConnections();
		targetedConnection = getCurrentConnection();
		if(targetedConnection==null && storedConnectionList.size()>0) {
			targetedConnection = storedConnectionList.get(0);
		} else {
			targetedConnection = new DBConnection();
		}
	}
	
	@Override
	public void addPages(){
		addPage(dbConnSelectionPage);
		addPage(dbConnNewConnPage);
	}
	
	public List<DBConnection> getStoredConnectionList(){
		ArrayList<DBConnection> arrayList = new ArrayList<DBConnection>(storedConnectionList);
		arrayList.add(0, new DBConnection());
		return arrayList;
	}
	
	@Override
	public boolean performFinish(){
		if(testDatabaseConnection()==false) return false;
		
		if (!storedConnectionList.contains(targetedConnection)) {
			// this is a new entry
			storedConnectionList.add(targetedConnection);
		}

		storeJDBCConnections();
		setUsedConnection();
		
		if (restartAfterChange) {
			UiDesk.asyncExec(new Runnable() {
				@Override
				public void run(){
					PlatformUI.getWorkbench().restart();
				}
			});
		}
		
		return true;
	}
	
	@Override
	public boolean canFinish(){
		return targetedConnection.allValuesSet();
	}
	
	public void setTargetedConnection(DBConnection targetedConnection){
		this.targetedConnection = targetedConnection;
		if(getContainer().getCurrentPage()!=null) getContainer().updateButtons();
	}
	
	public DBConnection getTargetedConnection(){
		return targetedConnection;
	}
	
	public void setRestartAfterConnectionChange(boolean restartAfterChange){
		this.restartAfterChange = restartAfterChange;
	}
	
	public boolean getRestartAfterConnectionChange(){
		return restartAfterChange;
	}
	
	/**
	 * load the stored connections from the local config, initializes {@link #storedConnectionList}
	 * if there are not yet any connections it initializes the list starting with the current
	 * connection (if available)
	 */
	@SuppressWarnings("unchecked")
	private void initStoredJDBCConnections(){
		String storage = CoreHub.localCfg.get(Preferences.CFG_STORED_JDBC_CONN, null);
		if (storage != null) {
			storedConnectionList =
				(List<DBConnection>) PersistentObject.foldObject(StringTool.dePrintable(storage), ccResolver);
		} else {
			// initialize the current connection (if available)
			storedConnectionList = new ArrayList<DBConnection>();
			String cnt = CoreHub.localCfg.get(Preferences.CFG_FOLDED_CONNECTION, null);
			if (cnt != null) {
				Hashtable<Object, Object> hConn =
					PersistentObject.fold(StringTool.dePrintable(cnt), ccResolver);
				if (hConn != null) {
					String connectionString =
						PersistentObject.checkNull(hConn
							.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING));
					String user =
						PersistentObject.checkNull(hConn
							.get(Preferences.CFG_FOLDED_CONNECTION_USER));
					String pwd =
						PersistentObject.checkNull(hConn
							.get(Preferences.CFG_FOLDED_CONNECTION_PASS));
					
					DBConnection dbc = new DBConnection();
					dbc.connectionString = connectionString;
					dbc.rdbmsType = parseDBTyp(connectionString);
					dbc.hostName = parseHostname(connectionString);
					dbc.username = user;
					dbc.password = pwd;
					
					storedConnectionList.add(dbc);
					
					storeJDBCConnections();
				}
			} else {
				// TODO skip selection page!
			}
		}
	}
	
	private String parseHostname(String connectionString){
		int i = connectionString.indexOf("//")+2;
		if(i==-1) return StringTool.leer;
		String woHeader = connectionString.substring(i);
		int ij = woHeader.indexOf(":");
		if(ij==-1) return StringTool.leer;
		return woHeader.substring(0, ij);
	}

	private @Nullable DBType parseDBTyp(String connectionString){
		if (connectionString.contains(StringConstants.COLON + JdbcLink.DBFLAVOR_H2
			+ StringConstants.COLON)) {
			return DBType.H2;
		} else if (connectionString.contains(StringConstants.COLON + JdbcLink.DBFLAVOR_MYSQL
			+ StringConstants.COLON)) {
			return DBType.MySQL;
		} else if (connectionString.contains(StringConstants.COLON + JdbcLink.DBFLAVOR_POSTGRESQL
			+ StringConstants.COLON)) {
			return DBType.PostgreSQL;
		}
		return null;
	}
	
	/**
	 * serialize the {@link #storedConnectionList} to the local config file
	 */
	void storeJDBCConnections(){
		Assert.isNotNull(storedConnectionList);
		byte[] flatten = PersistentObject.flattenObject(storedConnectionList);
		String enPrintable = StringTool.enPrintable(flatten);
		CoreHub.localCfg.set(Preferences.CFG_STORED_JDBC_CONN, enPrintable);
		CoreHub.localCfg.flush();
	}
	
	private final IClassResolver ccResolver = new IClassResolver() {
		// map DBConnection classes due to moving the implementation to ch.elexis.core.common
		@Override
		public Class<?> resolveClass(ObjectStreamClass desc)
			throws ClassNotFoundException{
			if (desc.getName().equals("ch.elexis.core.data.util.DBConnection")) {
				return Thread.currentThread().getContextClassLoader()
					.loadClass("ch.elexis.core.common.DBConnection");
			} else if (desc.getName()
				.equals("ch.elexis.core.data.util.DBConnection$DBType")) {
				return Thread.currentThread().getContextClassLoader()
					.loadClass("ch.elexis.core.common.DBConnection$DBType");
			}
			return null;
		}
	};
	
	/**
	 * retrieve the current {@link DBConnection} by parsing the value stored in the local
	 * configuration with key {@link Preferences#CFG_FOLDED_CONNECTION}
	 * 
	 * @return the respective {@link DBConnection} from {@link #storedConnectionList} or
	 *         <code>null</code>
	 */
	public @Nullable DBConnection getCurrentConnection(){
		String cnt = CoreHub.localCfg.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (cnt != null) {
			Hashtable<Object, Object> hConn =
				PersistentObject.fold(StringTool.dePrintable(cnt), ccResolver);
			if (hConn != null) {
				String currConnString =
					PersistentObject.checkNull(hConn
						.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING));
				String user =
					PersistentObject.checkNull(hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER));
				String combined = user + "@" + currConnString;
				
				for (DBConnection dbConnection : storedConnectionList) {
					if (combined.equalsIgnoreCase(dbConnection.username + "@"
						+ dbConnection.connectionString)) {
						return dbConnection;
					}
				}
			}
		}
		return null;
	}
	
	private boolean setUsedConnection(){
		Hashtable<String, String> h = new Hashtable<String, String>();
		h.put(Preferences.CFG_FOLDED_CONNECTION_DRIVER, targetedConnection.rdbmsType.driverName);
		h.put(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING, targetedConnection.connectionString);
		h.put(Preferences.CFG_FOLDED_CONNECTION_USER, targetedConnection.username);
		h.put(Preferences.CFG_FOLDED_CONNECTION_PASS, targetedConnection.password);
		h.put(Preferences.CFG_FOLDED_CONNECTION_TYPE, targetedConnection.rdbmsType.dbType);
		
		String conn = StringTool.enPrintable(PersistentObject.flatten(h));
		CoreHub.localCfg.set(Preferences.CFG_FOLDED_CONNECTION, conn);
		CoreHub.localCfg.flush();
		
		return true;
	}

	public void removeConnection(DBConnection connection){
		storedConnectionList.remove(connection);
		storeJDBCConnections();
	}
	
	/**
	 * test the {@link #targetedConnection} for its validity
	 * @return
	 */
	private boolean testDatabaseConnection(){
		boolean error = true;
		
		JdbcLink j = null;
		String text = null;
		
		try {
			String hostname =
				(targetedConnection.port != null) ? targetedConnection.hostName + ":"
					+ targetedConnection.port : targetedConnection.hostName;
			
			if (targetedConnection.databaseName == null
				|| targetedConnection.databaseName.isEmpty()) {
				throw new IllegalArgumentException("No database name provided.");
			}
			
			switch (targetedConnection.rdbmsType) {
			case H2:
				j = JdbcLink.createH2Link(targetedConnection.databaseName);
				break;
			case MySQL:
				j = JdbcLink.createMySqlLink(hostname, targetedConnection.databaseName);
				break;
			case PostgreSQL:
				j = JdbcLink.createPostgreSQLLink(hostname, targetedConnection.databaseName);
				break;
			default:
				j = null;
				break;
			}
			
			Assert.isNotNull(j);
			
			j.connect(targetedConnection.username, targetedConnection.password);
			
			text = "Verbindung hergestellt";
			error = false;
		} catch (Exception e) {
			e.printStackTrace();
			text = "Exception " + e.getMessage();
		}
		
		dbConnNewConnPage.getTdbg().setTestResult(error, text);
		
		if(j==null) {
			// thats an error situation
			return true;
		}
		
		if(!error) {
			targetedConnection.connectionString = j.getConnectString();
		}
		
		return !error;
	}
}
