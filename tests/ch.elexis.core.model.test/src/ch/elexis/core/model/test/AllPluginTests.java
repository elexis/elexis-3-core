package ch.elexis.core.model.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.service.CoreModelServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreModelServiceTest.class
})
public class AllPluginTests {
	
}
