package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.VersionedResource;

public class EncounterTest extends AbstractTest {
	
	public IMandator mandator;
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createCoverage();
		
		IPerson mandatorPerson = new IContactBuilder.PersonBuilder(modelService, "Money", "Man",
			LocalDate.now(), Gender.MALE).build();
		mandatorPerson.setMandator(true);
		modelService.save(mandatorPerson);
		
		mandator = modelService.load(mandatorPerson.getId(), IMandator.class).get();
	}
	
	@Test
	public void createFindDeleteEncounter(){
		IEncounter encounter = new IEncounterBuilder(modelService, coverage, mandator).buildAndSave();
		LocalDate date = LocalDate.of(2018, Month.SEPTEMBER, 21);
		encounter.setDate(date);
		VersionedResource vr = VersionedResource.load(null);
		vr.update("Test consultation\nWith some test text.", "Administrator");
		vr.update("Test consultation\n pdate done by user", "user");
		encounter.setVersionedEntry(vr);
		modelService.save(encounter);
		
		IQuery<IEncounter> query = modelService.getQuery(IEncounter.class);
		query.and(ModelPackage.Literals.IENCOUNTER__COVERAGE, COMPARATOR.EQUALS, coverage);
		assertEquals(encounter, query.executeSingleResult().get());
		assertEquals(date, encounter.getDate());
		assertTrue(encounter.getVersionedEntry().getHead(), encounter.getVersionedEntry().getHead().contains("done by user"));
		
		modelService.delete(encounter);
	}
	
}
