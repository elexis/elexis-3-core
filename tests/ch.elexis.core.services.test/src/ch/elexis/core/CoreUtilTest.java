package ch.elexis.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.utils.CoreUtil;

public class CoreUtilTest {

	@Test
	public void testGetDatabaseType() {
		String databaseType = CoreUtil.getDatabaseProductName();
		assertEquals("H2", databaseType);
	}

}
