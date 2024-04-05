package ch.elexis.core.findings.util.importer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.interfaces.IReferenceDataImporter;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirBundleReferenceDataImporterTest {

	private static IReferenceDataImporter bundleImporter;

	private static IXidService xidService;

	@BeforeClass
	public static void beforeClass() {
		bundleImporter = OsgiServiceUtil.getService(IReferenceDataImporter.class,
				"(" + IReferenceDataImporter.REFERENCEDATAID + "=fhirbundle)").get();
		xidService = OsgiServiceUtil.getService(IXidService.class).get();
	}

	@Test
	public void importInsuranceOrganizations() {
		Optional<IOrganization> agrisano = xidService.findObject(XidConstants.EAN, "7601003101362",
				IOrganization.class);
		assertFalse(agrisano.isPresent());
		IStatus status = bundleImporter.performImport(null,
				FhirBundleReferenceDataImporterTest.class.getResourceAsStream("/rsc/json/insurance-organizations.json"),
				null);
		assertTrue(status.isOK());
		agrisano = xidService.findObject(XidConstants.EAN, "7601003101362", IOrganization.class);
		assertTrue(agrisano.isPresent());
		// test re import, duplicates would throw exception on xidService.findObject
		status = bundleImporter.performImport(null,
				FhirBundleReferenceDataImporterTest.class.getResourceAsStream("/rsc/json/insurance-organizations.json"),
				null);
		assertTrue(status.isOK());
		agrisano = xidService.findObject(XidConstants.EAN, "7601003101362", IOrganization.class);
		assertTrue(agrisano.isPresent());
	}
}
