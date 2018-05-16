package ch.elexis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.data.util.Test_ProgrammaticUpdateConsistency;
import ch.elexis.data.AllDataTests;

@RunWith(Suite.class)
@SuiteClasses({Test_ProgrammaticUpdateConsistency.class, AllDataTests.class})
public class AllTests {
	
}
