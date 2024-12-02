package ch.elexis.core.findings.util.fhir.transformer;

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
		AppointmentTerminTransformerTest.class, SlotTerminTransformerTest.class })
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

//	@BeforeClass
//	public static void beforeClass() throws InterruptedException{
//
//		for (int i = 0; i < 10; i++) {
//			if (coreModelService == null) {
//				System.out.println("Waiting for services");
//				Thread.sleep(1000);
//			} else {
//				continue;
//			}
//		}
//		if (coreModelService == null) {
//			fail();
//		}
//	}

	public static IFhirTransformerRegistry getTransformerRegistry() {
		return transformerRegistry;
	}

	public static IModelService getCoreModelService() {
		return coreModelService;
	}
}