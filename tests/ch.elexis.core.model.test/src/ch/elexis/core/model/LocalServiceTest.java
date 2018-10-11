package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class LocalServiceTest extends AbstractTest {
	
	public IMandator mandator;
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createCoverage();
		
		IPerson mandatorPerson = new IContactBuilder.PersonBuilder(coreModelService, "Money", "Man",
			LocalDate.now(), Gender.MALE).build();
		mandatorPerson.setMandator(true);
		coreModelService.save(mandatorPerson);
		
		mandator = coreModelService.load(mandatorPerson.getId(), IMandator.class).get();
	}
	
	@Test
	public void create(){
		ILocalService service = coreModelService.create(ILocalService.class);
		assertNotNull(service);
		assertTrue(service instanceof ILocalService);
		
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(512));
		service.setPrice(new Money(1024));
		coreModelService.save(service);
		
		Optional<ILocalService> loaded = coreModelService.load(service.getId(), ILocalService.class);
		assertTrue(loaded.isPresent());
		assertFalse(service == loaded.get());
		assertEquals(service, loaded.get());
		assertEquals(service.getCode(), loaded.get().getCode());
		assertEquals(service.getText(), loaded.get().getText());
		assertEquals(service.getNetPrice(), loaded.get().getNetPrice());
		assertEquals(service.getPrice(), loaded.get().getPrice());
		
		coreModelService.remove(service);
	}
	
	@Test
	public void query(){
		ILocalService service = coreModelService.create(ILocalService.class);
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(12));
		service.setPrice(new Money(13));
		coreModelService.save(service);
		
		ILocalService service1 = coreModelService.create(ILocalService.class);
		service1.setText("test service 1");
		service1.setCode("9876");
		service1.setNetPrice(new Money(24));
		service1.setPrice(new Money(25));
		coreModelService.save(service1);
		
		IQuery<ILocalService> query = coreModelService.getQuery(ILocalService.class);
		query.and(ModelPackage.Literals.ICODE_ELEMENT__CODE, COMPARATOR.EQUALS, "1234");
		List<ILocalService> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(service, existing.get(0));
		
		coreModelService.remove(service);
		coreModelService.remove(service1);
	}
	
	@Test
	public void optifier(){
		ILocalService service = coreModelService.create(ILocalService.class);
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(12));
		service.setPrice(new Money(13));
		coreModelService.save(service);
		
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		Result<IBilled> result = service.getOptifier().add(service, encounter, 1.5);
		assertTrue(result.isOK());
		assertFalse(encounter.getBilled().isEmpty());
		IBilled billed = encounter.getBilled().get(0);
		assertEquals(1.5, billed.getAmount(), 0.01);
		assertEquals(service.getPrice(), billed.getPrice());
		assertEquals(service.getNetPrice(), billed.getNetPrice());
		assertEquals(service.getText(), billed.getText());
		
		coreModelService.remove(service);
	}
}
