package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class SickCertificateTest extends AbstractTest {
	
	private IPatient patient1;
	
	@Override
	@Before
	public void before(){
		super.before();
		patient1 = coreModelService.create(IPatient.class);
		patient1.setDescription1("test patient 1");
		coreModelService.save(patient1);
	}
	
	@Override
	@After
	public void after(){
		coreModelService.remove(patient1);
		super.after();
	}
	
	@Test
	public void create() throws IOException{
		ISickCertificate certificate = coreModelService.create(ISickCertificate.class);
		assertNotNull(certificate);
		assertTrue(certificate instanceof ISickCertificate);
		
		certificate.setPatient(patient1);
		certificate.setPercent(23);
		certificate.setDate(LocalDate.of(2000, 1, 1));
		certificate.setStart(LocalDate.of(2000, 1, 2));
		certificate.setEnd(LocalDate.of(2000, 1, 5));
		certificate.setReason("is sick");
		certificate.setNote("test");
		assertTrue(coreModelService.save(certificate));
		
		Optional<ISickCertificate> loadedCertificate =
			coreModelService.load(certificate.getId(), ISickCertificate.class);
		assertTrue(loadedCertificate.isPresent());
		assertEquals(patient1, loadedCertificate.get().getPatient());
		assertFalse(certificate == loadedCertificate.get());
		assertEquals(certificate, loadedCertificate.get());
		assertEquals(certificate.getNote(), loadedCertificate.get().getNote());
		coreModelService.remove(certificate);
	}
}
