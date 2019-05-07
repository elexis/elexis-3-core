package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IStoreToStringServiceTest extends AbstractServiceTest {
	
	private IStoreToStringService stsService =
		OsgiServiceUtil.getService(IStoreToStringService.class).get();
	
	@Test
	public void storeToStringPatient(){
		Optional<String> patientString = stsService.storeToString(AllServiceTests.getPatient());
		assertEquals("ch.elexis.data.Patient::" + AllServiceTests.getPatient().getId(),
			patientString.get());
	}
	
	@Test
	public void loadFromStringPatient(){
		Optional<Identifiable> patientObject = stsService
			.loadFromString("ch.elexis.data.Patient::" + AllServiceTests.getPatient().getId());
		assertTrue(patientObject.get().getClass().toString(),
			(patientObject.get() instanceof IPatient));
	}
	
	@Test
	public void storeToStringLaboratory(){
		Optional<String> laboratoryString =
			stsService.storeToString(AllServiceTests.getLaboratory());
		assertEquals("ch.elexis.data.Labor::" + AllServiceTests.getLaboratory().getId(),
			laboratoryString.get());
	}
	
	@Test
	public void loadFromStringLaboratory(){
		Optional<Identifiable> laboratoryObject = stsService
			.loadFromString("ch.elexis.data.Labor::" + AllServiceTests.getLaboratory().getId());
		assertTrue(laboratoryObject.get().getClass().toString(),
			(laboratoryObject.get() instanceof ILaboratory));
	}
	
	@Test
	public void storeToStringArtikel(){
		Optional<String> eigenartikelString =
			stsService.storeToString(AllServiceTests.getEigenartikel());
		assertEquals("ch.elexis.core.eigenartikel.Eigenartikel::"
			+ AllServiceTests.getEigenartikel().getId(), eigenartikelString.get());
	}
	
	@Test
	public void loadFromStringArtikel(){
		Optional<Identifiable> articleObject =
			stsService.loadFromString("ch.elexis.core.eigenartikel.Eigenartikel::"
				+ AllServiceTests.getEigenartikel().getId());
		Optional<Identifiable> articleObjectLegacy = stsService.loadFromString(
			"ch.elexis.eigenartikel.Eigenartikel::" + AllServiceTests.getEigenartikel().getId());
		assertEquals(articleObject.get().getId(), articleObjectLegacy.get().getId());
	}
}
