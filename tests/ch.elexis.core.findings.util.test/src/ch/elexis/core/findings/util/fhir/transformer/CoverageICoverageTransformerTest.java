package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coverage;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IBillingSystemService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.test.util.TestUtil;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoverageICoverageTransformerTest {

	private static IFhirTransformer<Coverage, ICoverage> transformer;
	private static IPatient patient;

	@BeforeClass
	public static void beforeClass() {

		IBillingSystemService billingSystemService = OsgiServiceUtil.getServiceWait(IBillingSystemService.class, 2000)
				.orElseThrow();
		billingSystemService.addOrModifyBillingSystem(Messages.Case_UVG_Short, Messages.Fall_TarmedPrinter,
				Messages.Fall_UVGRequirements, BillingLaw.UVG);
		billingSystemService.addOrModifyBillingSystem(Messages.Fall_IV_Name, Messages.Fall_TarmedPrinter, null,
				BillingLaw.IV);

		transformer = AllTransformerTests.getTransformerRegistry()
				.getTransformerFor(Coverage.class, ICoverage.class);
		assertNotNull(transformer);

		patient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Firstname", "Lastname",
				LocalDate.of(1979, 5, 22), Gender.MALE).build();

		TestUtil.setId(patient, "c8c506aa2e1a4626b296d0ef0");
	}

	@Test
	public void getFhirObject_KVG() throws IOException {
		ICoverage coverage_kvg = new ICoverageBuilder(CoreModelServiceHolder.get(), patient, "Label_KVG", "Krankheit",
				"KVG").build();
		assertNotNull(coverage_kvg);
		assertEquals(BillingLaw.KVG, coverage_kvg.getBillingSystem().getLaw());
		coverage_kvg.setInsuranceNumber("756.1234.5678.97");
		coverage_kvg.setDateFrom(LocalDate.of(2022, 01, 28));
		TestUtil.setId(coverage_kvg, "2886830bbadb4b09980f3d84b");

		Optional<Coverage> fhirObject = transformer.getFhirObject(coverage_kvg);
		assertTrue(fhirObject.isPresent());

		String expectedFile = TestUtil.loadFile(getClass(), "/rsc/json/Coverage_KVG.json");
		Coverage expected = (Coverage) toBaseResource(expectedFile);

		assertFhirResourcesAreEqual(fhirObject.get(), expected);
	}

	@Test
	public void getFhirObject_UVG() throws IOException {
		ICoverage coverage_kvg = new ICoverageBuilder(CoreModelServiceHolder.get(), patient, "Label_UVG", "Unfall",
				Messages.Case_UVG_Short).build();
		assertNotNull(coverage_kvg);
		assertEquals(BillingLaw.UVG, coverage_kvg.getBillingSystem().getLaw());
		coverage_kvg.setDateFrom(LocalDate.of(2022, 01, 28));
		coverage_kvg.setExtInfo(FallConstants.UVG_UNFALLNUMMER, "0190222222");
		coverage_kvg.setExtInfo(FallConstants.UVG_UNFALLDATUM, "27.01.2022");
		TestUtil.setId(coverage_kvg, "UVG6830bbadb4b09980f3d84b");

		Optional<Coverage> fhirObject = transformer.getFhirObject(coverage_kvg);
		assertTrue(fhirObject.isPresent());

		String expectedFile = TestUtil.loadFile(getClass(), "/rsc/json/Coverage_UVG.json");
		Coverage expected = (Coverage) toBaseResource(expectedFile);

		assertFhirResourcesAreEqual(fhirObject.get(), expected);
	}

	@Test
	public void getFhirObject_IV() throws IOException {
		ICoverage coverage_kvg = new ICoverageBuilder(CoreModelServiceHolder.get(), patient, "Label_IV", "Krankheit",
				Messages.Fall_IV_Name).build();
		assertNotNull(coverage_kvg);
		assertEquals(BillingLaw.IV, coverage_kvg.getBillingSystem().getLaw());
		coverage_kvg.setDateFrom(LocalDate.of(2022, 01, 28));
		coverage_kvg.setExtInfo(FallConstants.IV_FALLNUMMER, "2222220190");
		TestUtil.setId(coverage_kvg, "IV6830bbadb4b09980f3d84b");

		Optional<Coverage> fhirObject = transformer.getFhirObject(coverage_kvg);
		assertTrue(fhirObject.isPresent());

		String expectedFile = TestUtil.loadFile(getClass(), "/rsc/json/Coverage_IV.json");
		Coverage expected = (Coverage) toBaseResource(expectedFile);

		assertFhirResourcesAreEqual(fhirObject.get(), expected);
	}

	public IBaseResource toBaseResource(String actual) {
		IParser jsonParser = FhirContext.forR4().newJsonParser();
		return jsonParser.parseResource(actual);
	}

	public void assertFhirResourcesAreEqual(IBaseResource expected, IBaseResource actual) {
		IParser jsonParser = FhirContext.forR4().newJsonParser();
		String actualAsJson = jsonParser.encodeResourceToString(actual);
		String expectedAsJson = jsonParser.encodeResourceToString(expected);
		assertEquals(actualAsJson, expectedAsJson);
	}

}
