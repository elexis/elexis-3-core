package ch.elexis.core.hl7.v2x;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Test_HL7_Imports.class, Test_HL7_v271_Imports.class
})
public class AllTests {
	public static Test suite() throws ClassNotFoundException{
		return new TestSuite("HL7 Import Tests");
	}
}
