package ch.elexis.data;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ Test_DBInitialState.class, Test_PersistentObject.class, Test_Prescription.class, Test_Patient.class,
		Test_LabItem.class, Test_DBImage.class, Test_Query.class, Test_Verrechnet.class, Test_Reminder.class,
		Test_StockService.class, Test_OrderService.class, Test_Konsultation.class,
		Test_VkPreise.class, Test_ZusatzAdresse.class, Test_Rechnung.class, Test_Trace.class, Test_User.class,
		Test_LabResult.class, Test_BezugsKontakt.class })
public class AllDataTests {

	public static final boolean PERFORM_UPDATE_TESTS = false;

}