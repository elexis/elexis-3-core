package ch.elexis.core.services.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.TimeUtilTest;
import ch.elexis.core.java.AllJavaTests;
import ch.elexis.core.model.builder.AllBuilderTests;
import ch.elexis.core.services.AllServiceTests;
import ch.elexis.core.services.ace.AllACETests;

@RunWith(Suite.class)
@SuiteClasses({ TimeUtilTest.class, AllJavaTests.class, AllBuilderTests.class, AllACETests.class,
		AllServiceTests.class })
public class AllTests {

}
