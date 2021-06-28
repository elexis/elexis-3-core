package ch.elexis.core.services.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.java.AllJavaTests;
import ch.elexis.core.model.builder.AllBuilderTests;
import ch.elexis.core.services.AllServiceTests;
import ch.elexis.core.services.eenv.RocketchatMessageTest;

@RunWith(Suite.class)
@SuiteClasses({
	AllJavaTests.class, AllBuilderTests.class, AllServiceTests.class, RocketchatMessageTest.class
})
public class AllTests {
	
}
