package ch.elexis.core.findings.test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.findings.ChangeContentTest;
import ch.elexis.core.findings.CreateFindingsTest;
import ch.elexis.core.findings.FindingsServiceComponent;
import ch.elexis.core.findings.FindingsServiceTest;
import ch.elexis.core.findings.IFinding;

@RunWith(Suite.class)
@SuiteClasses({
	FindingsServiceTest.class, CreateFindingsTest.class, ChangeContentTest.class
})
public class AllTests {
	public static final String PATIENT_ID = "defaultPatient";
	public static final String CONSULTATION_ID = "defaultConsultation";
	
	public static void deleteAllFindings() {
		List<IFinding> allFindings = FindingsServiceComponent.getService().getPatientsFindings(PATIENT_ID,
				IFinding.class);
		for (IFinding iFinding : allFindings) {
			FindingsServiceComponent.getService().deleteFinding(iFinding);
		}
	}

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		// check if for server test mode and init db if so
		String testMode = System.getProperty("es.test");
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase("true")) {
				ServerDatabaseInitailizer initializer = new ServerDatabaseInitailizer();
				initializer.initalize();
			}
		}
	}
}
