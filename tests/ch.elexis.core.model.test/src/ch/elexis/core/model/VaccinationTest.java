package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IVaccinationBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;

public class VaccinationTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		createPatient();
		createLocalArticle();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void createFindDelete() {
		IVaccination vaccination = new IVaccinationBuilder(coreModelService, null, localArticle, patient)
				.buildAndSave();
		assertEquals(patient, vaccination.getPatient());
		assertEquals(localArticle, vaccination.getArticle());
		assertNotNull(vaccination.getDateOfAdministration());

		List<IVaccination> vaccinations = coreModelService.getQuery(IVaccination.class)
				.and("id", COMPARATOR.NOT_EQUALS, "VERSION").execute();
		assertEquals(vaccination, vaccinations.get(0));

		coreModelService.remove(vaccination);
	}

	@Test
	public void patientGetVaccination() {
		IVaccination vaccination = new IVaccinationBuilder(coreModelService, null, "vaccinationName", "0123456789012",
				"J07BK03", patient).dateOfAdministration(LocalDate.of(2000, 1, 1))
				.buildAndSave();
		assertEquals(patient, vaccination.getPatient());
		assertNull(vaccination.getArticle());
		assertEquals("vaccinationName", vaccination.getArticleName());
		assertEquals(LocalDate.of(2000, 1, 1), vaccination.getDateOfAdministration());

		List<IVaccination> vaccinations = coreModelService.getQuery(IVaccination.class)
				.and("id", COMPARATOR.NOT_EQUALS, "VERSION")
				.and(ModelPackage.Literals.IVACCINATION__PATIENT, COMPARATOR.EQUALS, patient).execute();
		assertEquals(vaccination, vaccinations.get(0));

		coreModelService.remove(vaccination);
	}
}
