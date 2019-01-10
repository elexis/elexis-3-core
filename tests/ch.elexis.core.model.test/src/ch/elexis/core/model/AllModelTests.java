package ch.elexis.core.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AppointmentTest.class, ConfigTest.class, ContactTest.class, CoverageTest.class,
	DbImageTest.class, DocumentBriefTest.class, EncounterTest.class, LabItemTest.class,
	LabMappingTest.class, LaboratoryTest.class, LabOrderTest.class, LabResultTest.class,
	OrderEntryTest.class, PatientTest.class, RoleTest.class, StockEntryTest.class, UserTest.class,
	ArticleTest.class, UserConfigTest.class, XidTest.class, BillingSystemFactorTest.class,
	LocalServiceTest.class, DiagnosisReferenceTest.class, BilledTest.class, PrescriptionTest.class,
	FreeTextDiagnosisTest.class, RecipeTest.class, DefaultSignatureTest.class
})
public class AllModelTests {
	
}
