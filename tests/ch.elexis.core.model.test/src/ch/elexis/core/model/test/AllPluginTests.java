package ch.elexis.core.model.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.ConfigTest;
import ch.elexis.core.model.DocumentBriefTest;
import ch.elexis.core.model.UserConfigTest;
import ch.elexis.core.model.service.CoreModelServiceTest;
import ch.elexis.core.model.service.CoreQueryTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreModelServiceTest.class, CoreQueryTest.class, ConfigTest.class, UserConfigTest.class,
	DocumentBriefTest.class
})
public class AllPluginTests {
	
}
