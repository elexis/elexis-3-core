package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ConfigTest {
	private IModelService modelSerice;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		IConfig config = modelSerice.create(IConfig.class);
		assertNotNull(config);
		assertTrue(config instanceof IConfig);
		
		config.setKey("test key1");
		config.setValue("test value 1");
		assertTrue(modelSerice.save(config));
		
		Optional<IConfig> loadedConfig = modelSerice.load(config.getId(), IConfig.class);
		assertTrue(loadedConfig.isPresent());
		assertFalse(config == loadedConfig.get());
		assertEquals(config, loadedConfig.get());
		assertEquals(config.getValue(), loadedConfig.get().getValue());
		
		modelSerice.remove(config);
	}
	
	@Test
	public void query(){
		IConfig config1 = modelSerice.create(IConfig.class);
		config1.setKey("test key 1");
		config1.setValue("test value 1");
		assertTrue(modelSerice.save(config1));
		IConfig config2 = modelSerice.create(IConfig.class);
		config2.setKey("test key 2");
		config2.setValue("test value 2");
		assertTrue(modelSerice.save(config2));
		
		IQuery<IConfig> query = modelSerice.getQuery(IConfig.class);
		query.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS, "test key 2");
		List<IConfig> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertFalse(config2 == existing.get(0));
		assertEquals(config2, existing.get(0));
		assertEquals(config2.getValue(), existing.get(0).getValue());
		
		// key id also the id, try load
		Optional<IConfig> loaded = modelSerice.load("test key 2", IConfig.class);
		assertTrue(loaded.isPresent());
		assertEquals(config2, loaded.get());
		
		modelSerice.remove(config1);
		modelSerice.remove(config2);
	}
	
	@Test
	public void optimisticLock() throws InterruptedException{
		IConfig config1 = modelSerice.create(IConfig.class);
		config1.setKey("test key 1");
		config1.setValue("test value 1");
		assertTrue(modelSerice.save(config1));
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run(){
				int affected = modelSerice
					.executeNativeUpdate("UPDATE config SET wert = 'test key', lastupdate = "
						+ 1 + " WHERE param = 'test key 1'");
				assertEquals(1, affected);
			}
		});
		executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		config1.setValue("test key 1");
		RollbackException rbe = null;
		try {
			modelSerice.save(config1);
		} catch (RollbackException e) {
			rbe = e;
		}
		assertNotNull(rbe);
	}
}
