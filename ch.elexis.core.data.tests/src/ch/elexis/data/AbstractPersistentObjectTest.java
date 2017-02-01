package ch.elexis.data;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

public abstract class AbstractPersistentObjectTest {
	
	protected static JdbcLink link;
	
	/**
	 * create a H2-JdbcLink with an initialized db for elexis.
	 * 
	 * the creation script is taken from the rsc directory of the host plugin when running a
	 * Plugin-Test
	 */
	protected static JdbcLink initDB(){
		link = initDB("h2");
		return link;
	}
	
	/**
	 * create a JdbcLink with an initialized db for elexis the creation script is taken from the rsc
	 * directory of the host plugin when running a Plugin-Test
	 */
	protected static JdbcLink initDB(String dbflavor){
		JdbcLink link = null;
		
		if (dbflavor == "h2")
			link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "hsql");
		else if (dbflavor == "mysql")
			link = JdbcLink.createMySqlLink("localhost", "unittests");
		else if (dbflavor == "postgresql")
			link = JdbcLink.createPostgreSQLLink("localhost", "unittests");
		
		assertNotNull(link);
		link.connect("elexis", "elexisTest");
		PersistentObject.connect(link);
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
