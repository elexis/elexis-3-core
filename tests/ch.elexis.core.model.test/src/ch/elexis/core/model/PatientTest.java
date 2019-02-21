package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.StringTool;

public class PatientTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		super.createPatient();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createDeletePatient(){
		patient.setExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		coreModelService.save(patient);
		assertTrue(patient.isPatient());
		assertTrue(patient.isPerson());
		assertFalse(patient.isMandator());
		assertFalse(patient.isOrganization());
		assertFalse(patient.isLaboratory());
		
		String id = patient.getId();
		assertNotNull(id);
		assertNotNull(patient.getCode());
		IContact findById = coreModelService.load(id, IContact.class).get();
		assertNotNull(findById);
		assertEquals("Birthname", findById.getExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME));
		coreModelService.delete(patient);
	}
	
	@Test
	public void queryByPatientNumber(){
		IPatient patient1 = new IContactBuilder.PatientBuilder(coreModelService, "testfirst",
			"testlast", LocalDate.of(2018, 10, 24), Gender.FEMALE).build();
		patient1.setPatientNr("123");
		CoreModelServiceHolder.get().save(patient1);
		
		INamedQuery<IPatient> namedQuery =
			CoreModelServiceHolder.get().getNamedQuery(IPatient.class, "code");
		Optional<IPatient> loaded = namedQuery.executeWithParametersSingleResult(
			namedQuery.getParameterMap("code", StringTool.normalizeCase("123")));
		assertTrue(loaded.isPresent());
		assertEquals(patient1, loaded.get());
		
		CoreModelServiceHolder.get().remove(patient1);
	}
}
