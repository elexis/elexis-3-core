package ch.elexis.core.jpa.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.jpa.entitymanger.InitPersistenceUnit;

@RunWith(Suite.class)
@SuiteClasses({
	InitPersistenceUnit.class
})
public class AllPluginTests {
	

}
