package ch.elexis.data;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	Test_Prescription.class, Test_Patient.class, Test_LabItem.class, Test_PersistentObject.class,
	Test_DBImage.class, Test_Query.class
})
public class AllTests {
	// System.out.println("Started AllTests");
}