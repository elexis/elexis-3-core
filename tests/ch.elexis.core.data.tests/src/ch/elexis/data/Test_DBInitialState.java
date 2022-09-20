package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.services.holder.ConfigServiceHolder;

public class Test_DBInitialState extends AbstractPersistentObjectTest {

	@Test
	public void testDatabaseUpdatedToRequiredVersion() {
		// undo means refresh from database
		String requiredDb = ConfigServiceHolder.getGlobal("dbversion", "wr0ng");
		assertEquals("liquibase", requiredDb);
	}
}
