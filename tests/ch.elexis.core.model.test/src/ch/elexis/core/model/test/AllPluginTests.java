package ch.elexis.core.model.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.AllModelTests;
import ch.elexis.core.model.BillingLawTest;
import ch.elexis.core.model.service.CoreModelServiceTest;
import ch.elexis.core.model.service.CoreQueryTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreModelServiceTest.class, CoreQueryTest.class, AllModelTests.class, BillingLawTest.class
})
public class AllPluginTests {
	
}
