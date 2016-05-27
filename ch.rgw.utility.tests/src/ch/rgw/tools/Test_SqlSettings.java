package ch.rgw.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.rgw.io.Settings;
import ch.rgw.io.SqlSettings;

public class Test_SqlSettings {

	private static JdbcLink mainLink;
	
	@BeforeClass
	public static void beforeClass(){
		mainLink = getJdbcLink(true);
	}
	
	@AfterClass
	public static void afterClass(){
		mainLink.disconnect();
	}
	
	@After
	public void after(){
		JdbcLink link = getJdbcLink(false);
		link.exec("DELETE FROM CONFIG WHERE 1=1;");
		link.exec("DELETE FROM USERCONFIG WHERE 1=1;");
		link.disconnect();
	}
	
	@Test
	public void testConstructors(){
		JdbcLink link = getJdbcLink(false);
		new SqlSettings(link, "CONFIG");
		
		new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		link.disconnect();
	}
	
	@Test
	public void testSetString(){
		JdbcLink link = getJdbcLink(false);
		SqlSettings globalSettings = new SqlSettings(link, "CONFIG");
		// returns false on first set
		assertFalse(globalSettings.set("key1", "value"));
		globalSettings.flush();
		// returns true if already set
		assertTrue(globalSettings.set("key1", "value1"));
		globalSettings.flush();
		link.disconnect();
		
		link = getJdbcLink(false);
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		// returns false on first set
		assertFalse(userSettings.set("key1", "value"));
		userSettings.flush();
		// returns true if already set
		assertTrue(userSettings.set("key1", "value1"));
		userSettings.flush();
		link.disconnect();
	}
	
	@Test
	public void testSetBoolean(){
		JdbcLink link = getJdbcLink(false);
		SqlSettings globalSettings = new SqlSettings(link, "CONFIG");
		// returns false on first set
		globalSettings.set("key1", true);
		globalSettings.flush();
		// returns true if already set
		globalSettings.set("key1", false);
		globalSettings.flush();
		link.disconnect();
		
		link = getJdbcLink(false);
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		// returns false on first set
		userSettings.set("key1", true);
		userSettings.flush();
		// returns true if already set
		userSettings.set("key1", false);
		userSettings.flush();
		link.disconnect();
	}
	
	@Test
	public void testSetInt(){
		JdbcLink link = getJdbcLink(false);
		SqlSettings globalSettings = new SqlSettings(link, "CONFIG");
		// returns false on first set
		globalSettings.set("key1", 1);
		globalSettings.flush();
		// returns true if already set
		globalSettings.set("key1", 2);
		globalSettings.flush();
		link.disconnect();
		
		link = getJdbcLink(false);
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		// returns false on first set
		userSettings.set("key1", 1);
		userSettings.flush();
		// returns true if already set
		userSettings.set("key1", 2);
		userSettings.flush();
		link.disconnect();
	}
	
	@Test
	public void testGetString(){
		JdbcLink link = getJdbcLink(false);
		Settings globalSettings = new SqlSettings(link, "CONFIG");
		globalSettings.set("key1", "value1");
		globalSettings.set("key2", "\\\\server\\order\\file");
		globalSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		globalSettings = new SqlSettings(link, "CONFIG");
		
		assertEquals("value1", globalSettings.get("key1", ""));
		assertEquals("\\\\server\\order\\file", globalSettings.get("key2", ""));
		
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		userSettings.set("key1", "value1");
		userSettings.set("key2", "\\\\server\\order\\file");
		userSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		userSettings = new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		
		assertEquals("value1", userSettings.get("key1", ""));
		assertEquals("\\\\server\\order\\file", userSettings.get("key2", ""));
		link.disconnect();
	}
	
	@Test
	public void testGetBoolean(){
		JdbcLink link = getJdbcLink(false);
		Settings globalSettings = new SqlSettings(link, "CONFIG");
		globalSettings.set("key1", true);
		globalSettings.set("key2", false);
		globalSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		globalSettings = new SqlSettings(link, "CONFIG");
		assertEquals(true, globalSettings.get("key1", false));
		assertEquals(false, globalSettings.get("key2", true));
		
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		userSettings.set("key1", true);
		userSettings.set("key2", false);
		userSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		userSettings = new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		
		assertEquals(true, userSettings.get("key1", false));
		assertEquals(false, userSettings.get("key2", true));
		link.disconnect();
	}
	
	@Test
	public void testGetInt(){
		JdbcLink link = getJdbcLink(false);
		Settings globalSettings = new SqlSettings(link, "CONFIG");
		globalSettings.set("key1", 1);
		globalSettings.set("key2", 2);
		globalSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		globalSettings = new SqlSettings(link, "CONFIG");
		
		assertEquals(1, globalSettings.get("key1", -1));
		assertEquals(2, globalSettings.get("key2", -1));
		
		SqlSettings userSettings =
			new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		userSettings.set("key1", 1);
		userSettings.set("key2", 2);
		userSettings.flush();
		link.disconnect();
		link = getJdbcLink(false);
		userSettings = new SqlSettings(link, "USERCONFIG", "Param", "Value", "UserID='1'");
		
		assertEquals(1, userSettings.get("key1", -1));
		assertEquals(2, userSettings.get("key2", -1));
		link.disconnect();
	}
	
	@Test
	public void testConcurrentChange(){
		JdbcLink link1 = getJdbcLink(false);
		SqlSettings globalSettings1 = new SqlSettings(link1, "CONFIG");
		globalSettings1.set("key1", "value1");
		globalSettings1.flush();
		
		JdbcLink link2 = getJdbcLink(false);
		SqlSettings globalSettings2 = new SqlSettings(link2, "CONFIG");
		// read value from 2
		assertEquals("value1", globalSettings2.get("key1", null));
		// change on 2 and flush
		globalSettings2.set("key1", "value2");
		globalSettings2.flush();
		// read value from 1 
		assertEquals("value1", globalSettings1.get("key1", null));
		// write something else on 1 and flush
		globalSettings1.set("key2", "value1");
		globalSettings1.flush();
		// flush and disconnect
		globalSettings2.flush();
		link1.disconnect();
		link2.disconnect();
		// test what was persisted
		JdbcLink link = getJdbcLink(false);
		SqlSettings globalSettings = new SqlSettings(link, "CONFIG");
		assertEquals("value2", globalSettings.get("key1", null));
		assertEquals("value1", globalSettings.get("key2", null));
		link.disconnect();
	}
	
	private static JdbcLink getJdbcLink(boolean create){
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_settings_mem", "");
		link.connect("", "");
		
		if (create) {
			// init config tables
			link.exec(
				"CREATE TABLE CONFIG(lastupdate BIGINT, param VARCHAR(80) primary key, wert TEXT);");
			link.exec(
				"CREATE TABLE USERCONFIG( lastupdate BIGINT, UserID VARCHAR(25), Param VARCHAR(80), Value TEXT);");
		}
		return link;
	}
}
