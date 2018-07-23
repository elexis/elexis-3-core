package ch.elexis.core.model.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.ConfigTest;
import ch.elexis.core.model.DocumentBriefTest;
import ch.elexis.core.model.LabItemTest;
import ch.elexis.core.model.LabMappingTest;
import ch.elexis.core.model.LabOrderTest;
import ch.elexis.core.model.LabResultTest;
import ch.elexis.core.model.LaboratoryTest;
import ch.elexis.core.model.UserConfigTest;
import ch.elexis.core.model.XidTest;
import ch.elexis.core.model.perf.LaborPerformanceTest;
import ch.elexis.core.model.service.CoreModelServiceTest;
import ch.elexis.core.model.service.CoreQueryTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreModelServiceTest.class, CoreQueryTest.class, ConfigTest.class, UserConfigTest.class,
	LabItemTest.class, DocumentBriefTest.class, LabResultTest.class, LabOrderTest.class,
	LaboratoryTest.class, XidTest.class, LabMappingTest.class, LaborPerformanceTest.class
})
public class AllPluginTests {
	
}
