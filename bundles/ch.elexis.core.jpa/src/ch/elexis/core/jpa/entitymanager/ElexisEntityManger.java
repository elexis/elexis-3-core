package ch.elexis.core.jpa.entitymanager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.EntityManagerProperties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.liquibase.LiquibaseDBInitializer;
import ch.elexis.core.jpa.liquibase.LiquibaseDBScriptExecutor;
import ch.elexis.core.jpa.liquibase.LiquibaseDBUpdater;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.utils.CoreUtil;

@Component
public class ElexisEntityManger implements IElexisEntityManager {
	
	private static Logger logger = LoggerFactory.getLogger(ElexisEntityManger.class);
	
	private EntityManagerFactoryBuilder factoryBuilder;
	
	private EntityManagerFactory factory;
	
	private DataSource dataSource;
	
	private final ThreadLocal<EntityManager> threadLocal;
	
	private final Map<Thread, EntityManager> threadManagerMap;
	
	private ScheduledExecutorService entityManagerCollector;
	
	public ElexisEntityManger(){
		threadLocal = new ThreadLocal<EntityManager>();
		threadManagerMap = new ConcurrentHashMap<>();
		entityManagerCollector = Executors.newSingleThreadScheduledExecutor();
	}
	
	@Activate
	public void activate(){
		// collect EntityManagers of terminated threads
		entityManagerCollector.scheduleAtFixedRate(new EntityManagerCollector(), 2, 2,
			TimeUnit.SECONDS);
	}
	
	@Deactivate
	public void deactivate(){
		entityManagerCollector.shutdown();
	}
	
	@Reference(service = DataSource.class, unbind = "unbindDataSource")
	protected synchronized void bindDataSource(DataSource dataSource){
		logger.debug("Binding " + dataSource.getClass().getName());
		this.dataSource = dataSource;
	}
	
	protected synchronized void unbindDataSource(DataSource dataSource){
		logger.debug("Unbinding " + dataSource.getClass().getName());
		this.factory.close();
		this.factory = null;
		this.dataSource = null;
	}
	
	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=elexis)")
	protected synchronized void bind(EntityManagerFactoryBuilder factoryBuilder){
		logger.debug("Binding " + factoryBuilder.getClass().getName());
		this.factoryBuilder = factoryBuilder;
	}
	
	@Override
	public synchronized EntityManager getEntityManager(boolean managed){
		// do lazy initialization on first access
		if (factory == null) {
			// try to initialize
			if (factoryBuilder != null) {
				// make sure database is up to date
				LiquibaseDBInitializer initializer = new LiquibaseDBInitializer(dataSource);
				initializer.init();
				LiquibaseDBUpdater updater = new LiquibaseDBUpdater(dataSource);
				updater.update();
				// initialize the entity manager factory
				HashMap<String, Object> props = new HashMap<String, Object>();
				props.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.NONE);
				props.put("gemini.jpa.providerConnectedDataSource", dataSource);
				props.put("javax.persistence.nonJtaDataSource", dataSource);
				// we keep EntityManager instances possibly for the whole application lifecycle
				// so enable GC to clear entities from EntityManager cache
				props.put(EntityManagerProperties.PERSISTENCE_CONTEXT_REFERENCE_MODE, "WEAK");
				this.factory = factoryBuilder.createEntityManagerFactory(props);
			} else {
				throw new IllegalStateException("No EntityManagerFactoryBuilder available");
			}
		}
		
		if (factory != null) {
			if (managed) {
				EntityManager em = threadLocal.get();
				if (em == null || !em.isOpen()) {
					em = createManagedEntityManager();
				} else {
					// save happens in separate EntityManager
					// clear L1 cache, use L2 cache -> detach current L1 cache objects
					em.clear();
				}
				return em;
			} else {
				return factory.createEntityManager();
			}
		} else {
			throw new IllegalStateException("No EntityManagerFactory available");
		}
	}
	
	private EntityManager createManagedEntityManager(){
		logger.debug("Creating new EntityManager for Thread [" + Thread.currentThread() + "]");
		EntityManager em = factory.createEntityManager();
		threadLocal.set(em);
		threadManagerMap.put(Thread.currentThread(), em);
		return em;
	}
	
	@Override
	public synchronized void closeEntityManager(Object em){
		if (threadLocal.get() == em) {
			threadLocal.set(null);
			threadManagerMap.remove(Thread.currentThread());
		}
		((EntityManager) em).close();
	}
	
	@Override
	public void clearCache(){
		factory.getCache().evictAll();
	}
	
	private class EntityManagerCollector implements Runnable {
		@Override
		public void run(){
			if (threadManagerMap != null && !threadManagerMap.isEmpty()) {
				for (Thread thread : threadManagerMap.keySet().toArray(new Thread[0])) {
					if (!thread.isAlive()) {
						logger.debug("Closing EntityManager of non active thread ["
							+ thread.getName() + "]");
						EntityManager em = threadManagerMap.get(thread);
						if (em != null) {
							em.close();
						}
						threadManagerMap.remove(thread);
					}
				}
			}
		}
	}
	
	@Override
	public boolean executeSQLScript(String changeId, String sqlScript){
		if (CoreUtil.isTestMode() || Boolean.valueOf(System.getProperty("forceExecuteSqlScript"))) {
			LiquibaseDBScriptExecutor executor = new LiquibaseDBScriptExecutor(dataSource);
			return executor.execute(changeId, sqlScript);
		}
		logger.warn("Did not execute script [" + changeId + "] as system not started in mode "
			+ CoreUtil.TEST_MODE);
		return false;
	}
}
