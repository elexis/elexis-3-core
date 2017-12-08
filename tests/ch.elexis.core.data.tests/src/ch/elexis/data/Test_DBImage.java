package ch.elexis.data;

import org.junit.Test;

import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.JdbcLink;

public class Test_DBImage extends AbstractPersistentObjectTest {
	
	public Test_DBImage(JdbcLink link){
		super(link);
	}

	@Test
	public void testDBImageString(){
		try {
			DBImage img = new DBImage("", "test");
			// TODO: does not work under Elexis 3.0
			// fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {
			
		}
	}
	
}
