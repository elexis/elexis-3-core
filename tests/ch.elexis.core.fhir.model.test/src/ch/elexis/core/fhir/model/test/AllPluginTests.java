package ch.elexis.core.fhir.model.test;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.core.fhir.model.FhirModelServiceTest;
import ch.elexis.core.fhir.model.FhirReminderTest;
import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.fhir.model.adapter.ModelAdapterFactoryTest;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.utils.OsgiServiceUtil;

/**
 * Test requires bundles from elexis server, therefore it can only be run via
 * IDE not on build server.
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ModelAdapterFactoryTest.class, FhirReminderTest.class, FhirModelServiceTest.class })
public class AllPluginTests {

	private static IFhirModelService fhirModelService;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		fhirModelService = OsgiServiceUtil.getService(IFhirModelService.class).get();

		assertTrue(waitRemote());
	}

	public static IFhirModelService getModelService() {
		return fhirModelService;
	}

	private static boolean waitRemote() throws InterruptedException {
		int retry = 100;
		while (fhirModelService.getConnectionStatus() != ConnectionStatus.REMOTE && retry > 0) {
			Thread.sleep(100);
			retry--;
		}
		return fhirModelService.getConnectionStatus() == ConnectionStatus.REMOTE;
	}
}
