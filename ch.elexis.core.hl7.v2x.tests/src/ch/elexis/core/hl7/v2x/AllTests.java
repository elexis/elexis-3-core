package ch.elexis.core.hl7.v2x;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Test_HL7_Imports.class, Test_HL7_v271_Imports.class, Test_HL7_v25_Imports.class,
	Test_HL7_v251_Imports.class, Test_HL7_v26_Imports.class, Test_HL7_v24_Imports.class
})
public class AllTests {
	public static Test suite() throws ClassNotFoundException{
		return new TestSuite("HL7 Import Tests");
	}
}
