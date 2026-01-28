package ch.elexis.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.utils.DBConnectionUtil;

public class CoreUtilTest {

	@Test
	public void testGetDatabaseType() {
		String databaseType = DBConnectionUtil.getDatabaseProductName();
		assertEquals("H2", databaseType);
	}

}
