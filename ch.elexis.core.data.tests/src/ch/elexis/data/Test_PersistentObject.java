package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.model.IXid;
import ch.elexis.data.po.InvalidPersistentObjectImpl;
import ch.elexis.data.po.OtherJointPersistentObject;
import ch.elexis.data.po.OtherListPersistentObject;
import ch.elexis.data.po.PersistentObjectImpl;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkException;
import ch.rgw.tools.JdbcLinkSyntaxException;

public class Test_PersistentObject extends AbstractPersistentObjectTest {
	
	private static JdbcLink link;
	
	@BeforeClass
	public static void setUp() throws IOException{
		link = initDB();
		DBConnection dbc = new DBConnection();
		dbc.setJdbcLink(link);
		
		boolean ret = PersistentObject.connect(link);
		assertTrue(ret);
	}
	
	@AfterClass
	public static void tearDown(){
		PersistentObject.disconnect();
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
	public void testState(){
		PersistentObjectImpl impl = new PersistentObjectImpl(false);
		int ret = impl.state();
		assertEquals(PersistentObject.INEXISTENT, ret);
		
		impl = new PersistentObjectImpl();
		ret = impl.state();
		assertEquals(PersistentObject.EXISTS, ret);
		
		boolean delete = impl.delete();
		assertTrue(delete);
		ret = impl.state();
		assertEquals(PersistentObject.DELETED, ret);
	}
	
	@Test
	public void testDelete() throws InterruptedException{
		PersistentObjectImpl impl = new PersistentObjectImpl();
		long lastUpdate = impl.getLastUpdate();
		assertNotSame(0L, lastUpdate);
		assertFalse(impl.getBoolean(PersistentObject.FLD_DELETED));
		Thread.sleep(1);
		impl.delete();
		assertTrue(impl.getBoolean(PersistentObject.FLD_DELETED));
		// TODO test removal of Xid
		assertTrue(impl.getLastUpdate() > lastUpdate);
	}
	
	@Test
	public void testStoreToString(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.storeToString();
		assertNotNull(ret);
		assertTrue(ret.startsWith(PersistentObjectImpl.class.getName()));
	}
	
	@Test
	public void testGet(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		String ret = impl.get("TestGet");
		assertNotNull(ret);
		assertEquals("", ret);
	}
	
	@Test
	public void testGetFail(){
		InvalidPersistentObjectImpl impl = new InvalidPersistentObjectImpl();
		String ret = impl.get("");
		assertNotNull(ret);
		assertEquals(PersistentObject.MAPPING_ERROR_MARKER + "**", ret);
	}
	
	@Test
	public void testSet() throws InterruptedException{
		PersistentObjectImpl impl = new PersistentObjectImpl();
		long lastUpdate = impl.getLastUpdate();
		assertNotSame(0L, lastUpdate);
		Thread.sleep(1);
		impl.set(PersistentObjectImpl.FLD_TEST, "Blafooo");
		assertTrue(impl.getLastUpdate() > lastUpdate);
	}
	
	@Test(expected = JdbcLinkSyntaxException.class)
	public void testSetFail(){
		PersistentObjectImpl impl = new PersistentObjectImpl();
		impl.set("DOESNOTEXIST", "Nonsense");
	}
	
	@Test
	public void testAddRemoveToJoint() throws InterruptedException{
		PersistentObjectImpl impl = new PersistentObjectImpl();
		OtherJointPersistentObject opo = new OtherJointPersistentObject();
		long lastUpdate = impl.getLastUpdate();
		assertNotSame(0L, lastUpdate);
		Thread.sleep(2);
		int retVal =
			impl.addToList(PersistentObjectImpl.FLD_JOINT_OTHER, opo.getId(), new String[0]);
		assertNotSame(0, retVal);
		assertEquals(1, impl.getList(PersistentObjectImpl.FLD_JOINT_OTHER, new String[0]).size());
		assertTrue(impl.getLastUpdate() > lastUpdate);
		Thread.sleep(2);
		impl.removeFromList(PersistentObjectImpl.FLD_JOINT_OTHER, opo.getId());
		assertEquals(0, impl.getList(PersistentObjectImpl.FLD_JOINT_OTHER, new String[0]).size());
		assertTrue(impl.getLastUpdate() > lastUpdate);
	}
	
	/**
	 * https://redmine.medelexis.ch/issues/5655
	 * 
	 * @throws InterruptedException
	 */
	@Ignore
	public void testAddRemoveToList() throws InterruptedException{
		PersistentObjectImpl impl = new PersistentObjectImpl();
		OtherListPersistentObject opo = new OtherListPersistentObject();
		long lastUpdate = impl.getLastUpdate();
		assertNotSame(0L, lastUpdate);
		Thread.sleep(2);
		int retVal =
			impl.addToList(PersistentObjectImpl.FLD_LIST_OTHER, opo.getId(), new String[0]);
		assertNotSame(0, retVal);
		assertEquals(1, impl.getList(PersistentObjectImpl.FLD_LIST_OTHER, new String[0]).size());
		assertTrue(impl.getLastUpdate() > lastUpdate);
		Thread.sleep(2);
		impl.removeFromList(PersistentObjectImpl.FLD_LIST_OTHER, opo.getId());
		assertEquals(0, impl.getList(PersistentObjectImpl.FLD_LIST_OTHER, new String[0]).size());
		assertTrue(impl.getLastUpdate() > lastUpdate);
	}
	
	@Test
	public void testTableWideLastUpdate() throws InterruptedException{
		long highestLastUpdate = PersistentObjectImpl.getHighestLastUpdate();
		Thread.sleep(1);
		PersistentObjectImpl impl = new PersistentObjectImpl();
		long highestLastUpdate2 = PersistentObjectImpl.getHighestLastUpdate();
		assertTrue(highestLastUpdate2 > highestLastUpdate);
		Thread.sleep(1);
		impl.delete();
		highestLastUpdate = PersistentObjectImpl.getHighestLastUpdate();
		assertTrue(highestLastUpdate > highestLastUpdate2);
		assertEquals(impl.getLastUpdate(), highestLastUpdate);
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
}
