package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class LocalServiceTest extends AbstractTest {
	
	@Override
	@Before
	public void before(){
		super.before();
		super.createUserSetActiveInContext();
		super.createMandator();
		super.createPatient();
		super.createCoverage();
	}
	
	@Override
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void create(){
		ICustomService service = coreModelService.create(ICustomService.class);
		assertNotNull(service);
		assertTrue(service instanceof ICustomService);
		
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(512));
		service.setPrice(new Money(1024));
		coreModelService.save(service);
		
		Optional<ICustomService> loaded =
			coreModelService.load(service.getId(), ICustomService.class);
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
		ICustomService service = coreModelService.create(ICustomService.class);
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(12));
		service.setPrice(new Money(13));
		coreModelService.save(service);
		
		ICustomService service1 = coreModelService.create(ICustomService.class);
		service1.setText("test service 1");
		service1.setCode("9876");
		service1.setNetPrice(new Money(24));
		service1.setPrice(new Money(25));
		coreModelService.save(service1);
		
		IQuery<ICustomService> query = coreModelService.getQuery(ICustomService.class);
		query.and(ModelPackage.Literals.ICODE_ELEMENT__CODE, COMPARATOR.EQUALS, "1234");
		List<ICustomService> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(service, existing.get(0));
		
		coreModelService.remove(service);
		coreModelService.remove(service1);
	}
	
	
	@Test
	public void optifier(){
		ICustomService service = coreModelService.create(ICustomService.class);
		service.setText("test service");
		service.setCode("1234");
		service.setNetPrice(new Money(12));
		service.setPrice(new Money(13));
		coreModelService.save(service);
		
		IEncounter encounter =
			new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		// DER LEGT HIER IRGENDWIE EINEN PATIENT ZUSÄTZLICH AN!!
		Result<IBilled> result = service.getOptifier().add(service, encounter, 1.5);
		assertTrue(result.isOK());
		assertFalse(encounter.getBilled().isEmpty());
		IBilled billed = encounter.getBilled().get(0);
		assertEquals(1.5, billed.getAmount(), 0.01);
		assertEquals(service.getPrice(), billed.getPrice());
		assertEquals(service.getNetPrice(), billed.getNetPrice());
		assertEquals(service.getText(), billed.getText());
		
		coreModelService.remove(billed);
		coreModelService.remove(encounter);
		coreModelService.remove(service);
	}
}
