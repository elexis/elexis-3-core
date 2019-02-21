package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IStoreToStringServiceTest extends AbstractServiceTest {

	private IStoreToStringService stsService = OsgiServiceUtil.getService(IStoreToStringService.class).get();

	@Test
	public void storeToStringPatient() {
		Optional<String> patientString = stsService.storeToString(AllServiceTests.getPatient());
		assertTrue(patientString.isPresent());
		assertEquals("ch.elexis.data.Patient::" + AllServiceTests.getPatient().getId(), patientString.get());
	}

	@Test
	public void loadFromStringPatient() {
		Optional<Identifiable> patientObject = stsService
				.loadFromString("ch.elexis.data.Patient::" + AllServiceTests.getPatient().getId());
		assertTrue(patientObject.isPresent());
	}
}
