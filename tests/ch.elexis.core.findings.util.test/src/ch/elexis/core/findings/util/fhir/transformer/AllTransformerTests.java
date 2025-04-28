package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.fail;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.services.IModelService;


@Component
@RunWith(Suite.class)
@SuiteClasses({ CoverageICoverageTransformerTest.class, OrganizationIOrganizationTransformerTest.class,
		AppointmentTerminTransformerTest.class, SlotTerminTransformerTest.class, TaskReminderTransformerTest.class })
public class AllTransformerTests {

	private static IFhirTransformerRegistry transformerRegistry;

	private static IModelService coreModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private void setCoreModelService(IModelService coreModelService) {
		AllTransformerTests.coreModelService = coreModelService;
	}

	@Reference
	public void setIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		AllTransformerTests.transformerRegistry = transformerRegistry;
	}

	public static IFhirTransformerRegistry getTransformerRegistry() {
		int timeout = 5000;
		while (transformerRegistry == null) {
			try {
				Thread.sleep(100);
				timeout -= 100;
				if (timeout == 0) {
					fail("Timeout wait for transformer registry");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return transformerRegistry;
	}

	public static IModelService getCoreModelService() {
		int timeout = 5000;
		while (coreModelService == null) {
			try {
				Thread.sleep(100);
				timeout -= 100;
				if (timeout == 0) {
					fail("Timeout wait for core model serivce");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return coreModelService;
	}
}