package ch.elexis.core.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test functionality thats was integrated within the JRE up to Java 8
 */
@RunWith(Suite.class)
@SuiteClasses({
	JAXBTest.class, JaxRsConsumerTest.class, JaxWsTest.class
})
public class AllJavaTests {
	

}
