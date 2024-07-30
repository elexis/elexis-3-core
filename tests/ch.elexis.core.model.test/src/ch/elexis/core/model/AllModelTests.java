package ch.elexis.core.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ IdentifiableTest.class, AccountTransactionTest.class, AppointmentTest.class, ConfigTest.class,
		ContactTest.class, CoverageTest.class, DbImageTest.class, DocumentLetterTest.class, EncounterTest.class,
		LabItemTest.class, LabMappingTest.class, LaboratoryTest.class, LabOrderTest.class, LabResultTest.class,
		OrderEntryTest.class, PatientTest.class, RoleTest.class, StockTest.class, StockEntryTest.class, UserTest.class,
		UserGroupTest.class, ArticleTest.class, UserConfigTest.class, XidTest.class, BillingSystemFactorTest.class,
		LocalServiceTest.class, DiagnosisReferenceTest.class, BilledTest.class, PrescriptionTest.class,
		FreeTextDiagnosisTest.class, RecipeTest.class, DefaultSignatureTest.class, PersonTest.class, MessageTest.class,
		MandatorTest.class, TextTemplateTest.class, SickCertificateTest.class, InvoiceTest.class, DayMessageTest.class,
		BlobTest.class, ReminderTest.class, VaccinationTest.class })
public class AllModelTests {

}
