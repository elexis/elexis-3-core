package ch.elexis.core.tasks.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.importer.div.tasks.test.Hl7ImporterTaskIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses({
	TaskServiceTest.class, Hl7ImporterTaskIntegrationTest.class
})
public class AllTests {
	

	
}
