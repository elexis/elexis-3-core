package ch.elexis.data;

import java.io.IOException;
import java.io.InputStream;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

public class TestInitializer {
	
	public static final String FLAVOR_H2 = "h2";
	public static final String FLAVOR_MYSQL = "mysql";
	public static final String FLAVOR_POSTGRES = "postgresql";
	
	/**
	 * 
	 * @param dbflavor
	 * @return
	 */
	public static JdbcLink initTestDBConnection(String dbflavor){
		JdbcLink link = null;
		if (dbflavor == FLAVOR_H2) {
			link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "hsql");
		} else if (dbflavor == FLAVOR_MYSQL) {
			link = JdbcLink.createMySqlLink("localhost", "unittests");
		} else if (dbflavor == FLAVOR_POSTGRES) {
			link = JdbcLink.createPostgreSQLLink("localhost", "unittests");
		}
		
		if (link == null) {
			return link;
		}
		try {
			boolean connectionOk = link.connect("elexisTest", "elexisTest");
			PersistentObject.connect(link);
			// TODO why is the next set necessary?
			PersistentObject.getConnection().DBFlavor = dbflavor;
			if (connectionOk) {
				return link;
			}
		} catch (Exception jle) {
			jle.printStackTrace();
			link = null;
		}
		return link;
	}
	
	protected static void initElexisDatabase(DBConnection connection) throws IOException{
		executeDBScript(connection, "/rsc/createDB.script");
	}
	
	protected static void executeDBScript(DBConnection connection, String filename)
			throws IOException{
			Stm stm = null;
			try (InputStream is = PersistentObject.class.getResourceAsStream(filename)) {
				stm = connection.getStatement();
				boolean success = stm.execScript(is, true, true);
				if (!success) {
					throw new IOException();
				}
			} finally {
				connection.releaseStatement(stm);
			}
		}
}
