package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ConfigServiceTest {
	
	private IModelService modelService = AllServiceTests.getModelService();
	private IConfigService configService = OsgiServiceUtil.getService(IConfigService.class).get();
	
	@Test
	public void getSetUserconfig(){
		IPerson person = new IContactBuilder.PersonBuilder(modelService, "TestPerson", "TestPerson",
			LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		IPerson person2 = new IContactBuilder.PersonBuilder(modelService, "TestPerson2",
			"TestPerson2", LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		
		assertTrue(configService.set(person, "key", "value"));
		assertTrue(configService.set(person2, "key", "value2"));
		
		assertEquals("value", configService.get(person, "key", null));
		assertEquals("value2", configService.get(person2, "key", null));
		
		assertTrue(configService.set(person, "key", null));
		assertNull(configService.get(person, "key", null));
		assertFalse(configService.set(person, "key", null));
	}
	
	@Test
	public void getSetConfig(){
		assertTrue(configService.set("key", "value"));
		assertEquals("value", configService.get("key", null));
		assertTrue(configService.set("key", null));
		assertFalse(configService.set("key", null));
	}
	
}
