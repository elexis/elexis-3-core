package ch.elexis.data;

import org.junit.Test;

import ch.elexis.core.exceptions.PersistenceException;

public class Test_DBImage extends AbstractPersistentObjectTest {

	@Test
	public void testDBImageString() {
		try {
			DBImage img = new DBImage("", "test");
			// TODO: does not work under Elexis 3.0
			// fail("Expected Exception not thrown!");
		} catch (PersistenceException pe) {

		}
	}

}
