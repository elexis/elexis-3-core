package ch.elexis.core.services.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.services.AllServiceTests;

@RunWith(Suite.class)
@SuiteClasses({
	AllServiceTests.class
})
public class AllTests {
	
}
