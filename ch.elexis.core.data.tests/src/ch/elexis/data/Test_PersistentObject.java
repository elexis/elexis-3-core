package ch.elexis.data;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.model.IXid;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkException;

public class Test_PersistentObject extends AbstractPersistentObjectTest {
	
	private JdbcLink link;
	
	@Before
	public void setUp() throws IOException{
		link = initDB();
		DBConnection dbc = new DBConnection();
		dbc.setJdbcLink(link);
		executeDBScript(dbc, "/rsc/UserContactsToMigrate.sql");
	}
	
	@After
	public void tearDown(){
		try {
			if (link == null || !link.isAlive())
				return;
			link.exec("DROP ALL OBJECTS");
			link.disconnect();
		} catch (JdbcLinkException je) {
			// just tell what happend and resume
			// excpetion is allowed for tests which get rid of the connection on their own
			// for example testConnect(), ...
			je.printStackTrace();
		}
	}
	
	@Test
	public void testConnect(){
		boolean ret = PersistentObject.connect(link);
		assertTrue(ret);
		PersistentObject.disconnect();
	}
	
	@Test
	public void testConnectFail(){
		// this connect methods opens its own JdbcLink by all means
		// it is looking for a demo db:
		// File demo = new File(base.getParentFile().getParent() + "/demoDB");
		
		// then for dom SWTBot related db:
		// String template = System.getProperty("SWTBot-DBTemplate");
		// File dbDir = new File(Hub.getTempDir(), "Elexis-SWTBot");
		
		// then from some user provided config
		// String connection = Hub.getCfgVariant();
		
		// then if provider is Medelexis the db wizard is opened else
		// look for db at default location
		// String d = PreferenceInitializer.getDefaultDBPath();
		
		// this is nice for runtime but makes testing really hard :)
		// we need to mock JdbcLink.createH2Link to stop creation of database
		// PowerMockito.mockStatic(JdbcLink.class);
		// PowerMockito.when(JdbcLink.createH2Link(Matchers.anyString())).thenReturn(
		// new JdbcLink("", "", ""));
		// connect and simulate db creation failure with JdbcLink mock
		try {
			PersistentObject.connect(CoreHub.localCfg);
			// TODO: does not work at the moment in Elexis 3.0
			// fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	
	@Test
	public void testGet(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.get("TestGet");
		assertNotNull(ret);
		assertEquals("test", ret);
	}
	
	@Test
	public void testState(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		impl.tablename = "abc";
		int ret = impl.state();
		assertEquals(PersistentObject.INEXISTENT, ret);
	}
	
	@Test
	public void testStoreToString(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.storeToString();
		assertNotNull(ret);
		assertTrue(ret.startsWith("ch.elexis.data.Test_PersistentObject"));
	}
	
	@Test
	public void testGetXid(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		IXid ret = impl.getXid();
		assertNotNull(ret);
	}
	
	@Test
	public void testAddXid(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		Xid.localRegisterXIDDomain("test", "test", 1);
		boolean ret = impl.addXid("test", "addXid", false);
		assertTrue(ret);
		IXid id = impl.getXid();
		assertNotNull(id);
	}
	
	@Test(expected = PersistenceException.class)
	public void testGetFail(){
		// mock a status manager for ignoring the error status
		// StatusManager statusMock = PowerMockito.mock(StatusManager.class);
		// PowerMockito.mockStatic(StatusManager.class);
		// PowerMockito.when(StatusManager.getManager()).thenReturn(statusMock);
		
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.get("");
		assertNotNull(ret);
		assertEquals(PersistentObject.MAPPING_ERROR_MARKER + "**", ret);
		
		// if we pass ID we should get to code that reaches into the db
		// we have no table specified so a JdbcLinkException is expected
		String id = impl.get("ID");
		fail("Expected Exception not thrown! Value is " + id);
	}
	
	@Test
	public void testTableExists(){
		assertTrue(PersistentObject.tableExists("CONFIG"));
		assertTrue(PersistentObject.tableExists("KONTAKT"));
		// SQL can be case sensitive !!
		// assertEquals(false, PersistentObject.tableExists("kontakt"));
		assertEquals(false, PersistentObject.tableExists("THIS_TABLE_SHOULD_NOT_EXISTS"));
	}
	
	@Ignore
	public void testCaseSensitiveIdLoad(){
		//#5514
		Anwender anw = new Anwender("Username", "Uservorname", "16.1.1973", "w");
		new User(anw, "user", "pass");
		
		assertFalse(User.load("USER").exists());
		assertFalse(User.load("User").exists());
		assertTrue(User.load("user").exists());
	}
	
	@Test
	public void testCreateOrModifyTable(){
		/** Definition of the database table */
		String version = "1.0.0";
		String createTable = "CREATE TABLE Dummy" + "(" + "ID VARCHAR(25) primary key," // This
		// field must always be present
			+ "lastupdate BIGINT," // This field must always be present
			+ "deleted CHAR(1) default '0'," // This field must always be present
			+ "PatientID VARCHAR(25)," + "Title      VARCHAR(50)," // Use VARCHAR, CHAR, TEXT and
			// BLOB
			+ "FunFactor VARCHAR(6)," // No numeric fields
			+ "BoreFactor	VARCHAR(6)," // VARCHARS can be read as integrals
			+ "Date		CHAR(8)," // use always this for dates
			+ "Remarks	TEXT," + "FunnyStuff BLOB);" + "CREATE INDEX idx1 on Dummy (FunFactor);"
			// Do not forget to insert some version information
			+ "INSERT INTO Dummy (ID, Title) VALUES ('VERSION'," + JdbcLink.wrap(version) + ");";
		String modifyTable = "ALTER TABLE Dummy MODIFY BoreFactor VARCHAR(12);";
		PersistentObject.getConnection().DBFlavor = "h2";
		// create
		PersistentObject.createOrModifyTable(createTable);
		// modify
		PersistentObject.createOrModifyTable(modifyTable);
		// test the JdbcException thrown by the statement if FunFactor was still VARCHAR(6)
		// will stop the test if one of the createOrModifyTable failed ...
		JdbcLink link = PersistentObject.getConnection();
		Stm statement = link.getStatement();
		statement.exec("INSERT INTO Dummy (ID, BoreFactor) VALUES ('TEST', '1234567890');");
		link.releaseStatement(statement);
	}
	
	private class PersistentObjectImpl extends PersistentObject {
		
		String tablename;
		
		@SuppressWarnings("unused")
		public String getTestGet(){
			return "test";
		}
		
		@Override
		public String getLabel(){
			return null;
		}
		
		@Override
		protected String getTableName(){
			return tablename;
		}
		
	}
}
