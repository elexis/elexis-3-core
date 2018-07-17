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

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class UserConfigTest {
	private IModelService modelSerice;
	
	private IContact contact1;
	private IContact contact2;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		contact1 = modelSerice.create(IContact.class);
		contact1.setDescription1("test contact 1");
		modelSerice.save(contact1);
		contact2 = modelSerice.create(IContact.class);
		contact2.setDescription1("test contact 2");
		modelSerice.save(contact2);
	}
	
	@After
	public void after(){
		modelSerice.remove(contact1);
		modelSerice.remove(contact2);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		IUserConfig config = modelSerice.create(IUserConfig.class);
		assertNotNull(config);
		assertTrue(config instanceof IUserConfig);
		
		config.setOwner(contact1);
		config.setKey("test key1");
		config.setValue("test value 1");
		assertTrue(modelSerice.save(config));
		
		Optional<IUserConfig> loadedConfig = modelSerice.load(config.getId(), IUserConfig.class);
		assertTrue(loadedConfig.isPresent());
		assertFalse(config == loadedConfig.get());
		assertEquals(config, loadedConfig.get());
		assertEquals(config.getValue(), loadedConfig.get().getValue());
		assertEquals(contact1, loadedConfig.get().getOwner());
		
		modelSerice.remove(config);
	}
	
	@Test
	public void query(){
		IUserConfig config1 = modelSerice.create(IUserConfig.class);
		config1.setOwner(contact1);
		config1.setKey("test key 1");
		config1.setValue("test value 1");
		assertTrue(modelSerice.save(config1));
		
		IUserConfig config2 = modelSerice.create(IUserConfig.class);
		config2.setOwner(contact2);
		config2.setKey("test key 2");
		config2.setValue("test value 2");
		assertTrue(modelSerice.save(config2));
		
		IQuery<IUserConfig> query = modelSerice.getQuery(IUserConfig.class);
		query.and(ModelPackage.Literals.IUSER_CONFIG__OWNER, COMPARATOR.EQUALS, contact2);
		List<IUserConfig> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(config2 == existing.get(0));
		assertEquals(config2, existing.get(0));
		assertEquals(config2.getValue(), existing.get(0).getValue());
		
		modelSerice.remove(config1);
		modelSerice.remove(config2);
	}
}
