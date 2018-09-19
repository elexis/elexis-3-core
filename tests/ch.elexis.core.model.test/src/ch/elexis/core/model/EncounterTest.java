package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;

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
		
		IQuery<IEncounter> query = modelService.getQuery(IEncounter.class);
		query.and(ModelPackage.Literals.IENCOUNTER__COVERAGE, COMPARATOR.EQUALS, coverage);
		assertEquals(encounter, query.executeSingleResult().get());
		
		modelService.delete(encounter);
	}
	
}
