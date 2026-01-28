package ch.elexis.core.findings.test;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.findings.AllergyIntoleranceTest;
import ch.elexis.core.findings.ConditionTest;
import ch.elexis.core.findings.CreateFindingsTest;
import ch.elexis.core.findings.DocumentReferenceTest;
import ch.elexis.core.findings.EncounterTest;
import ch.elexis.core.findings.FamilyMemberHistoryTest;
import ch.elexis.core.findings.FindingsServiceComponent;
import ch.elexis.core.findings.FindingsServiceTest;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.MigratorServiceTest;
import ch.elexis.core.findings.ObservationTest;
import ch.elexis.core.findings.ProcedureRequestTest;
import ch.elexis.core.findings.codings.CodingServiceTest;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.PersistentObject;

@RunWith(Suite.class)
@SuiteClasses({ FindingsServiceTest.class, CreateFindingsTest.class, EncounterTest.class, ConditionTest.class,
		CodingServiceTest.class, ProcedureRequestTest.class, ObservationTest.class, FamilyMemberHistoryTest.class,
		AllergyIntoleranceTest.class, DocumentReferenceTest.class, MigratorServiceTest.class })
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
	public static void beforeClass()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException,
			SecurityException, IllegalArgumentException, InvocationTargetException, SQLException {

		// check if for server test mode and init db if so
		String testMode = System.getProperty("es.test");
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase("true")) {
				ServerDatabaseInitailizer initializer = new ServerDatabaseInitailizer();
				initializer.initalize();
			}
		}

		DataSource dataSource = OsgiServiceUtil.getService(DataSource.class, "(id=default)").get();
		assertTrue(PersistentObject.connect(dataSource));
	}
}
