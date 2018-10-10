package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class UserConfigTest {
	private IModelService modelService;
	
	private IContact contact1;
	private IContact contact2;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
		contact1 = modelService.create(IContact.class);
		contact1.setDescription1("test contact 1");
		modelService.save(contact1);
		contact2 = modelService.create(IContact.class);
		contact2.setDescription1("test contact 2");
		modelService.save(contact2);
	}
	
	@After
	public void after(){
		modelService.remove(contact1);
		modelService.remove(contact2);
		
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	@Test
	public void create(){
		IUserConfig config = modelService.create(IUserConfig.class);
		assertNotNull(config);
		assertTrue(config instanceof IUserConfig);
		
		config.setOwner(contact1);
		config.setKey("test key1");
		config.setValue("test value 1");
		assertTrue(modelService.save(config));
		
		// modelService.load is string only, hence not applicable to Userconfig
//		Optional<IUserConfig> loadedConfig = modelService.load(config.getId(), IUserConfig.class);
//		assertTrue(loadedConfig.isPresent());
//		assertFalse(config == loadedConfig.get());
//		assertEquals(config, loadedConfig.get());
//		assertEquals(config.getValue(), loadedConfig.get().getValue());
//		assertEquals(contact1, loadedConfig.get().getOwner());
		
		modelService.remove(config);
	}
	
	@Test
	public void query(){
		IUserConfig config1 = modelService.create(IUserConfig.class);
		config1.setOwner(contact1);
		config1.setKey("test key 1");
		config1.setValue("test value 1");
		assertTrue(modelService.save(config1));
		
		IUserConfig config2 = modelService.create(IUserConfig.class);
		config2.setOwner(contact2);
		config2.setKey("test key 2");
		config2.setValue("test value 2");
		assertTrue(modelService.save(config2));
		
		IQuery<IUserConfig> query = modelService.getQuery(IUserConfig.class);
		query.and(ModelPackage.Literals.IUSER_CONFIG__OWNER, COMPARATOR.EQUALS, contact2.getId());
		List<IUserConfig> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(config2 == existing.get(0));
		assertEquals(config2, existing.get(0));
		assertEquals(config2.getValue(), existing.get(0).getValue());
		
		modelService.remove(config1);
		modelService.remove(config2);
	}
}
