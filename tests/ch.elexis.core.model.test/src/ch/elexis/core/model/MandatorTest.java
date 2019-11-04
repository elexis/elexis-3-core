package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;

public class MandatorTest extends AbstractTest {

	@Before
	public void before() {
		super.before();
		createMandator();
	}

	@After
	public void after() {
		super.after();
	}


	@Test
	public void extInfoWithRefetch() {
		assertTrue(person.isMandator());
		assertTrue(person.getExtInfo("testKey") == null);
		person.setMobile("011");
		person.setExtInfo("testKey", "testValue");
		
		Optional<IMandator> loaded = coreModelService.load(person.getId(), IMandator.class);
		
		// refetch
		assertTrue(loaded.get().getMobile() == null);
		assertTrue(loaded.get().getExtInfo("testKey") == null);
		
		coreModelService.save(person);

		assertEquals("011", loaded.get().getMobile()); // works
		// the issue [16648] happened here - because of refetch the value of testKey was null if extInfoHandler won't be reseted after entity change
		assertEquals("testValue", loaded.get().getExtInfo("testKey"));
	}
	
	@Test
	public void extInfoWithoutRefetch() {
		// working solution without refetch
		assertTrue(person.isMandator());
		assertTrue(person.getExtInfo("testKey") == null);
		assertTrue(person.getMobile() == null);
		person.setMobile("01");
		person.setExtInfo("testKey", "testValue");
	
		Optional<IMandator> loaded = coreModelService.load(person.getId(), IMandator.class);
		// no refetch save directly
		coreModelService.save(person);
		
		assertEquals("01", loaded.get().getMobile());
		assertEquals("testValue", loaded.get().getExtInfo("testKey"));
	}	
	
	@Test
	public void extInfoMutlipleSaveAndRefresh() {
		assertTrue(person.isMandator());
		assertTrue(person.getExtInfo("testKey") == null);
		assertTrue(person.getMobile() == null);
		person.setMobile("01");
		person.setExtInfo("testKey1", "testValue1");
	
		Optional<IMandator> loaded = coreModelService.load(person.getId(), IMandator.class);
		coreModelService.save(person);

		assertEquals("01", loaded.get().getMobile());
		assertEquals("testValue1", loaded.get().getExtInfo("testKey1"));
		
		// some more value changes
		person.setExtInfo("testKey2", "testValue2");
		person.setExtInfo("testKey3", "testValue3");
		person.setMobile("02");
		
		// refresh changed object and db object
		CoreModelServiceHolder.get().refresh(person);
		loaded = coreModelService.load(person.getId(), IMandator.class);
		CoreModelServiceHolder.get().refresh(loaded.get());

		// check changes is still present 
		assertTrue(person.getExtInfo("testKey2") != null);
		assertEquals("02", person.getMobile());
		
		// db object should consist db values
		assertTrue(loaded.get().getExtInfo("testKey2") == null);
		assertEquals("01", loaded.get().getMobile());
		
		// save changes to synchronize changed object with db object
		coreModelService.save(person);
		
		assertEquals("02", loaded.get().getMobile());
		assertEquals("testValue1", loaded.get().getExtInfo("testKey1"));
		assertEquals("testValue2", loaded.get().getExtInfo("testKey2"));
		assertEquals("testValue3", loaded.get().getExtInfo("testKey3"));
		
		// refresh again to ensure nothing get changed
		CoreModelServiceHolder.get().refresh(loaded.get());
		
		assertEquals("02", loaded.get().getMobile());
		assertEquals("testValue1", loaded.get().getExtInfo("testKey1"));
		assertEquals("testValue2", loaded.get().getExtInfo("testKey2"));
		assertEquals("testValue3", loaded.get().getExtInfo("testKey3"));
		
		// load again to ensure nothing get changed
		loaded = coreModelService.load(person.getId(), IMandator.class);
		
		assertEquals("02", loaded.get().getMobile());
		assertEquals("testValue1", loaded.get().getExtInfo("testKey1"));
		assertEquals("testValue2", loaded.get().getExtInfo("testKey2"));
		assertEquals("testValue3", loaded.get().getExtInfo("testKey3"));
	}	
}
