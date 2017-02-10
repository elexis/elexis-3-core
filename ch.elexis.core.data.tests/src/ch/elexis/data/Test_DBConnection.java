package ch.elexis.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import ch.rgw.tools.JdbcLink;

public class Test_DBConnection extends AbstractPersistentObjectTest {

	public Test_DBConnection(JdbcLink link){
		super(link);
	}

	private static Organisation organisation;
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		// create a instance of an PersistentObject ex. Organisation to test the query
		organisation = new Organisation("orgname", "orgzusatz1");
	}
	
	@AfterClass
	public static void afterClass() {
		organisation.delete();
	}
	
	@Ignore
	public void testConnect(){
		// connect using link
		DBConnection connection = new DBConnection();
		assertFalse(connection.connect());
		connection.setJdbcLink(getLink());
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
