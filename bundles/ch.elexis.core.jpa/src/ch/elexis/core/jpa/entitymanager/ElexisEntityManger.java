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
import ch.elexis.core.jpa.liquibase.LiquibaseDBUpdater;
import ch.elexis.core.services.IElexisEntityManager;

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
	
	@Reference(service = DataSource.class, cardinality = ReferenceCardinality.MANDATORY)
	protected synchronized void bindDataSource(DataSource dataSource){
		this.dataSource = dataSource;
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
				this.factory = factoryBuilder.createEntityManagerFactory(props);
			} else {
				throw new IllegalStateException("No EntityManagerFactoryBuilder available");
			}
		}
		
		if (factory != null) {
			if (managed) {
				EntityManager em = threadLocal.get();
				if (em == null) {
					logger.debug(
						"Creating new EntityManager for Thread [" + Thread.currentThread() + "]");
					em = factory.createEntityManager();
					threadLocal.set(em);
					threadManagerMap.put(Thread.currentThread(), em);
				}
				return em;
			} else {
				return factory.createEntityManager();
			}
		} else {
			throw new IllegalStateException("No EntityManagerFactory available");
		}
	}
	
	@Override
	public synchronized void closeEntityManager(Object em){
		if (threadLocal.get() == em) {
			threadLocal.set(null);
			threadManagerMap.remove(Thread.currentThread());
		}
		((EntityManager) em).close();
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
}
