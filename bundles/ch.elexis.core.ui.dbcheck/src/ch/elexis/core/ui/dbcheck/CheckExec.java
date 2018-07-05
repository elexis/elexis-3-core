/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck;

import java.sql.Connection;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkSyntaxException;

public abstract class CheckExec {
	public static String MYSQL_DB = "com.mysql.jdbc.Driver";
	public static String POSTG_DB = "org.postgresql.Driver";
	
	/** Holds connection to database. */
	protected static Connection conn;
	protected static JdbcLink j;
	protected static String sqlDriver;
	protected static String sqlConnection;
	
	public static String getDBInformation(){
		return sqlDriver + "@" + sqlConnection;
	}
	
	public static String checkDBVersionConsistence(){
		String version = getDBVersion();
		if (CoreHub.DBVersion.equalsIgnoreCase(version))
			return version;
		return "Verlangte DB: " + CoreHub.DBVersion + " Gefundene DB: " + version
			+ "; Teste auf Version " + version;
	}
	
	public static String getDBVersion(){
		String version = "";
		if (sqlDriver.equalsIgnoreCase(MYSQL_DB)) {
			try {
				version = j.queryString("SELECT wert FROM config WHERE param=\"dbversion\"");
			} catch (JdbcLinkSyntaxException e) {
				version = j.queryString("SELECT wert FROM CONFIG WHERE param=\"dbversion\"");
			}
		}
		if (sqlDriver.equalsIgnoreCase(POSTG_DB)) {
			version = j.queryString("SELECT wert FROM CONFIG WHERE param LIKE \'dbversion\'");
		}
		return version;
	}
	
	/**
	 * Set the JDBCLink
	 * 
	 * @param link
	 */
	public static void setJDBCLink(JdbcLink link){
		j = link;
		sqlDriver = j.getDriverName();
		sqlConnection = j.getConnectString();
	}
	
	public static void finishJDBCLink(){
		j.disconnect();
	}
}
