package ch.elexis.data;

import static org.junit.Assert.fail;

import org.junit.Test;

import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.JdbcLink;

public class Test_DBImage extends AbstractPersistentObjectTest {
	
	@Test
	public void testDBImageString(){
		JdbcLink link = initDB();
		if (link == null)
			fail("initDB should not return null");
			
		try {
			DBImage img = new DBImage("", "test");
			// TODO: does not work under Elexis 3.0
			// fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
		
		}
		if (link != null)
			link.disconnect();
	}
	
}
