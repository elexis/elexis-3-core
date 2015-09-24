package ch.elexis.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_DBConnection extends AbstractPersistentObjectTest {

	private static JdbcLink link;
	
	@BeforeClass
	public static void setUp() throws Exception{
		if (link != null) {
			PersistentObject.deleteAllTables();
			link.disconnect();
		}
		link = initDB();
		// create a instance of an PersistentObject ex. Organisation to test the query
		new Organisation("orgname", "orgzusatz1");
	}
	
	@AfterClass
	public static void tearDown() throws Exception{
		PersistentObject.deleteAllTables();
		link.exec("DROP ALL OBJECTS");
		link.disconnect();
	}
	
	@Test
	public void testConnect(){
		// connect using link
		DBConnection connection = new DBConnection();
		assertFalse(connection.connect());
		connection.setJdbcLink(link);
		connection.setDBUser("elexis");
		connection.setDBPassword("elexisTest");
		assertTrue(connection.connect());
		// connect using connection string
		connection = new DBConnection();
		connection.setDBConnectString("jdbc:h2:mem:test_connect_mem");
		connection.setDBUser("sa");
		connection.setDBPassword("");
		assertTrue(connection.connect());
		connection.disconnect();
		// connect using 
		connection = new DBConnection();
		connection.setDBFlavor("h2");
		connection.setDBConnectString("jdbc:h2:mem:test_connect_mem");
		connection.setDBUser("sa");
		connection.setDBPassword("");
		assertTrue(connection.isDirectConnectConfigured());
		assertTrue(connection.directConnect());
		connection.disconnect();
	}
}
