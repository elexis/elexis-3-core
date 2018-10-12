package ch.elexis.core.jpa.datasource.test;

import ch.elexis.core.common.DBConnection;

/**
 * an h2 based test database connection
 */
public class TestDatabaseConnection extends DBConnection {
	
	private static final long serialVersionUID = 3263914071272054090L;
	
	public TestDatabaseConnection(){
		this(false);
	}
	
	/**
	 * @param asServer
	 *            if <code>true</code> db instance can be contacted via socket
	 */
	public TestDatabaseConnection(boolean asServer){
		if (asServer) {
			connectionString = "jdbc:h2:~/elexisTest/elexisTest;AUTO_SERVER=TRUE";
		} else {
			connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		}
		String trace = System.getProperty("elexis.test.dbtrace");
		if (trace != null && "true".equalsIgnoreCase(trace)) {
			connectionString += ";TRACE_LEVEL_SYSTEM_OUT=2";
		}
		rdbmsType = DBType.H2;
		username = "sa";
		password = "";
	}
}
