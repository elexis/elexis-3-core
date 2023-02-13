package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Organization;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.test.util.TestUtil;
import ch.elexis.core.types.Country;

public class OrganizationIOrganizationTransformerTest {

	private static IFhirTransformer<Organization, IOrganization> transformer;

	@BeforeClass
	public static void beforeClass() {
		transformer = (IFhirTransformer<Organization, IOrganization>) AllTransformerTests.getTransformerRegistry()
				.getTransformerFor(Organization.class, IOrganization.class);
		assertNotNull(transformer);

	}

	@Test
	public void toElexisObject() throws IOException {
		String expectedFile = TestUtil.loadFile(getClass(), "/rsc/json/Organization.json");
		Organization expected = (Organization) toBaseResource(expectedFile);

		IOrganization org = transformer.createLocalObject(expected).get();
		assertEquals("Laurstrasse 10", org.getStreet());
		assertEquals("Brugg", org.getCity());
		assertEquals("5201", org.getZip());
		assertEquals(Country.CH, org.getCountry());
		assertEquals("daten@agrisano.ch", org.getEmail());
		assertEquals("056 461 71 11", org.getPhone1());
		assertEquals("www.agrisano.ch", org.getWebsite());

		assertEquals("1560", org.getXid(XidConstants.DOMAIN_BSVNUM).getDomainId());
		assertEquals("7601003000436", org.getXid(XidConstants.DOMAIN_RECIPIENT_EAN).getDomainId());

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
