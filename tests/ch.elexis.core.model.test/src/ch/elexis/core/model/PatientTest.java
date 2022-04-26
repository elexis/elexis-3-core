package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class PatientTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createPatient();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void createDeletePatient() {
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
	}

	@Test
	public void queryByPatientNumber() {
		IPatient patient1 = new IContactBuilder.PatientBuilder(coreModelService, "testfirst", "testlast",
				LocalDate.of(2018, 10, 24), Gender.FEMALE).build();
		patient1.setPatientNr("123");
		CoreModelServiceHolder.get().save(patient1);

		INamedQuery<IPatient> namedQuery = CoreModelServiceHolder.get().getNamedQuery(IPatient.class, "code");
		Optional<IPatient> loaded = namedQuery
				.executeWithParametersSingleResult(namedQuery.getParameterMap("code", StringTool.normalizeCase("123")));
		assertTrue(loaded.isPresent());
		assertEquals(patient1, loaded.get());

		CoreModelServiceHolder.get().remove(patient1);
	}

	@Test
	public void queryByName() {
		IPatient patient1 = new IContactBuilder.PatientBuilder(coreModelService, "testfirst", "testlast",
				LocalDate.of(2018, 10, 24), Gender.FEMALE).build();
		patient1.setPatientNr("123");
		CoreModelServiceHolder.get().save(patient1);

		IQuery<IPatient> query = CoreModelServiceHolder.get().getQuery(IPatient.class);
		query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE, "testfirst", true);
		List<IPatient> loaded = query.execute();
		assertFalse(loaded.isEmpty());

		query = CoreModelServiceHolder.get().getQuery(IPatient.class);
		query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE, "testlast", true);
		loaded = query.execute();
		assertTrue(loaded.isEmpty());

		CoreModelServiceHolder.get().remove(patient1);
	}

	@Test
	public void queryByDateOfBirth() {
		IPatient patient1 = new IContactBuilder.PatientBuilder(coreModelService, "testfirst", "testlast",
				LocalDate.of(2018, 10, 24), Gender.FEMALE).build();
		patient1.setPatientNr("123");
		CoreModelServiceHolder.get().save(patient1);

		IQuery<IPatient> query = CoreModelServiceHolder.get().getQuery(IPatient.class);
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
				new TimeTool("24.10.2018").toLocalDate());
		List<IPatient> loaded = query.execute();
		assertFalse(loaded.isEmpty());

		CoreModelServiceHolder.get().remove(patient1);
	}
}
