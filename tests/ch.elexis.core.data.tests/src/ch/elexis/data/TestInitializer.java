package ch.elexis.data;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

public class TestInitializer {
	
	public static final String FLAVOR_H2_MEM = "h2";
	public static final String FLAVOR_MYSQL = "mysql";
	public static final String FLAVOR_POSTGRES = "postgresql";
	
	/**
	 * 
	 * @param dbflavor
	 * @return
	 */
	public static JdbcLink initTestDBConnection(String dbflavor){
		JdbcLink link = null;
		if (dbflavor == FLAVOR_H2_MEM) {
			link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "h2");
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
			if (connectionOk) {
				return link;
			}
		} catch (Exception jle) {
			jle.printStackTrace();
			link = null;
		}
		return link;
	}
	
	protected static void executeDBScript(DBConnection connection, String filename)
		throws IOException{
		Stm stm = null;
		try (InputStream is = PersistentObject.class.getResourceAsStream(filename)) {
			stm = connection.getStatement();
			boolean success = stm.execScript(is, true, true);
			if (!success) {
				fail("Error executing script!");
				throw new IOException();
			}
		} finally {
			connection.releaseStatement(stm);
		}
	}
	
	public static JdbcLink initDemoDbConnection(){
		String demoDBLocation = System.getProperty(ElexisSystemPropertyConstants.DEMO_DB_LOCATION);
		if (demoDBLocation == null) {
			demoDBLocation = CoreHub.getWritableUserDir() + File.separator + "demoDB";
		}
		
		File demo = new File(demoDBLocation);
		
		// --
		// returns if either, demo db, direct connection or run from scratch
		// --
		if (demo.exists() && demo.isDirectory()) {
			// open demo database connection
			JdbcLink link = JdbcLink.createH2Link(demo.getAbsolutePath() + File.separator + "db");
			try {
				boolean connectionOk = link.connect("sa", "");
				if (connectionOk) {
					return link;
				}
			} catch (Exception jle) {
				jle.printStackTrace();
				link = null;
			}
		}
		return null;
	}
}
