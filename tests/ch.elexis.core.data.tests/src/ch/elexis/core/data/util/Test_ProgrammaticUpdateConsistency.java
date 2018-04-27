package ch.elexis.core.data.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;

public class Test_ProgrammaticUpdateConsistency {
	
	@Test
	public void testProgrammaticDBUpdateConsistency(){
		assertEquals(CoreHub.DBVersion, DBUpdate.versions[DBUpdate.versions.length - 1]);
		assertEquals(DBUpdate.versions.length, DBUpdate.cmds.length);
	}
}
