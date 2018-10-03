package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
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
	
	@Test
	public void addRemoveDiagnosis(){
		IEncounter encounter =
			new IEncounterBuilder(modelService, coverage, mandator).buildAndSave();
		IDiagnosisReference diagnosis = modelService.create(IDiagnosisReference.class);
		diagnosis.setCode("test");
		modelService.save(diagnosis);
		IDiagnosisReference diagnosis1 = modelService.create(IDiagnosisReference.class);
		diagnosis.setCode("test1");
		modelService.save(diagnosis1);
		
		encounter.addDiagnosis(diagnosis);
		encounter.addDiagnosis(diagnosis1);
		modelService.save(encounter);
		
		Optional<IEncounter> loaded = modelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertEquals(encounter.getDiagnoses().size(), loaded.get().getDiagnoses().size());
		
		encounter.removeDiagnosis(diagnosis1);
		modelService.save(encounter);
		
		loaded = modelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertEquals(encounter.getDiagnoses().size(), loaded.get().getDiagnoses().size());
		
		encounter.removeDiagnosis(diagnosis);
		modelService.save(encounter);
		loaded = modelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertTrue(loaded.get().getDiagnoses().isEmpty());
		
		modelService.remove(diagnosis);
		modelService.remove(diagnosis1);
		modelService.remove(encounter);
	}
	
	@Test
	public void addRemoveBilled(){
		ILocalService service = modelService.create(ILocalService.class);
		service.setCode("12.34");
		service.setNetPrice(new Money(1));
		service.setPrice(new Money(2));
		service.setText("test");
		modelService.save(service);
		
		IEncounter encounter =
			new IEncounterBuilder(modelService, coverage, mandator).buildAndSave();
		modelService.save(encounter);
		
		Result<IBillable> result = service.getOptifier().add(service, encounter, 1.5);
		assertTrue(result.isOK());
		
		assertFalse(encounter.getBilled().isEmpty());
		Optional<IEncounter> loaded = modelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertFalse(loaded.get().getBilled().isEmpty());
		IBilled billed = loaded.get().getBilled().get(0);
		assertEquals(1.5, billed.getAmount(), 0.01);
		assertEquals(service.getText(), billed.getText());
		assertEquals(service, billed.getBillable());
		
		encounter.removeBilled(billed);
		assertFalse(encounter.getBilled().contains(billed));
		assertTrue(billed.isDeleted());
		
		modelService.remove(service);
		modelService.remove(encounter);
	}
}
