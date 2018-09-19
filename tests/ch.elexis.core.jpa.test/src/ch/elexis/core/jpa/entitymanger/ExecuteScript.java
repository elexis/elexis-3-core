package ch.elexis.core.jpa.entitymanger;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.jpa.test.AllPluginTests;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ExecuteScript {
	
	@Test
	public void execute() throws IOException{
		Optional<IElexisEntityManager> elexisEntityManager =
			OsgiServiceUtil.getService(IElexisEntityManager.class);
		assertTrue(elexisEntityManager.isPresent());
		
		assertTrue(elexisEntityManager.get().executeSQLScript("testScriptId",
			AllPluginTests.loadFile("/rsc/testContacts.sql")));
		OsgiServiceUtil.ungetService(elexisEntityManager.get());
	}
	
}
