/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.io.Serializable;

public class DBConnection implements Serializable {
	
	private static final long serialVersionUID = -7571011690246990109L;
	
	public enum DBType {
		MySQL("com.mysql.jdbc.Driver", "mySQl", "3306"), PostgreSQL("org.postgresql.Driver",
			"PostgreSQL", "5432"), H2("org.h2.Driver", "H2", "");
		
		public final String driverName;
		public final String dbType;
		public final String defaultPort;
		
		DBType(String driverName, String dbType, String defaultPort){
			this.driverName = driverName;
			this.dbType = dbType;
			this.defaultPort = defaultPort;
		}
	}
	
	public DBType rdbmsType;
	public String hostName;
	public String port;
	public String databaseName;
	public String connectionString;
	public String username;
	public String password;
	public String text;
	
	/**
	 * are all required values for the DBConnection set?
	 * @return
	 */
	public boolean allValuesSet(){
		boolean result = true;
		
		result = (rdbmsType!=null);
		
		if (!DBType.H2.equals(rdbmsType)) {
			result = (hostName != null);
		}
		
		result = (databaseName != null);
		result = (username != null);
		
		return result;
	}
}
