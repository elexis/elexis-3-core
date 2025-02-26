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

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.jpa.entitymanager.ui.IDatabaseUpdateUi;
import ch.elexis.core.jpa.liquibase.LiquibaseDBInitializer;
import ch.elexis.core.jpa.liquibase.LiquibaseDBScriptExecutor;
import ch.elexis.core.jpa.liquibase.LiquibaseDBUpdater;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.utils.CoreUtil;

@Component(property = "id=default")
public class ElexisEntityManger implements IElexisEntityManager {

	private static Logger logger = LoggerFactory.getLogger(ElexisEntityManger.class);

	private EntityManagerFactoryBuilder factoryBuilder;

	private EntityManagerFactory factory;

	private DataSource dataSource;

	private final ThreadLocal<EntityManager> threadLocal;

	private final Map<Thread, EntityManager> threadManagerMap;

	private ScheduledExecutorService entityManagerCollector;

	private boolean updateSuccess;

	public ElexisEntityManger() {
		threadLocal = new ThreadLocal<>();
		threadManagerMap = new ConcurrentHashMap<>();
		entityManagerCollector = Executors.newSingleThreadScheduledExecutor();
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private IDatabaseUpdateUi updateProgress;

	private final boolean SKIP_LIQUIBASE = Boolean
			.valueOf(System.getProperty(ElexisSystemPropertyConstants.CONN_SKIP_LIQUIBASE));

	@Activate
	public void activate() {
		updateSuccess = false;
		// collect EntityManagers of terminated threads
		entityManagerCollector.scheduleAtFixedRate(new EntityManagerCollector(), 2, 2, TimeUnit.SECONDS);
	}

	@Deactivate
	public void deactivate() {
		entityManagerCollector.shutdown();
	}

	@Reference(service = DataSource.class, unbind = "unbindDataSource", target = "(id=default)")
	protected synchronized void bindDataSource(DataSource dataSource) {
		logger.debug("Binding " + dataSource.getClass().getName()); //$NON-NLS-1$
		this.dataSource = dataSource;
	}

	protected synchronized void unbindDataSource(DataSource dataSource) {
		logger.debug("Unbinding " + dataSource.getClass().getName()); //$NON-NLS-1$
		if (this.factory != null) {
			this.factory.close();
			this.factory = null;
		}
		this.dataSource = null;
	}

	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=elexis)")
	protected synchronized void bind(EntityManagerFactoryBuilder factoryBuilder) {
		logger.debug("Binding " + factoryBuilder.getClass().getName()); //$NON-NLS-1$
		this.factoryBuilder = factoryBuilder;
	}

	@Override
	public synchronized EntityManager getEntityManager(boolean managed) {
		// do lazy initialization on first access
		if (factory == null) {
			// try to initialize
			if (factoryBuilder != null) {

				if (!SKIP_LIQUIBASE) {
					if (updateProgress != null) {
						try {
							updateProgress.executeWithProgress(Messages.ElexisEntityManger_Database_Init, () -> {
								dbInit(updateProgress);
							});
							updateProgress.executeWithProgress(Messages.ElexisEntityManger_Database_Update, () -> {
								dbUpdate(updateProgress);
							});
						} catch (Exception e) {
							logger.warn("Exeption executing database update with ui", e); //$NON-NLS-1$
						}
					} else {
						dbInit(null);
						dbUpdate(null);
					}
				} else {
					logger.warn("Skipping liquibase execution");
					updateSuccess = true;
				}

				// initialize the entity manager factory
				HashMap<String, Object> props = new HashMap<>();
				props.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.NONE);
				props.put("gemini.jpa.providerConnectedDataSource", dataSource); //$NON-NLS-1$
				props.put("javax.persistence.nonJtaDataSource", dataSource); //$NON-NLS-1$
				// we keep EntityManager instances possibly for the whole application lifecycle
				// so enable GC to clear entities from EntityManager cache
				props.put(EntityManagerProperties.PERSISTENCE_CONTEXT_REFERENCE_MODE, "WEAK"); //$NON-NLS-1$
				this.factory = factoryBuilder.createEntityManagerFactory(props);
			} else {
				throw new IllegalStateException("No EntityManagerFactoryBuilder available"); //$NON-NLS-1$
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
			throw new IllegalStateException("No EntityManagerFactory available"); //$NON-NLS-1$
		}
	}

	private void dbUpdate(IDatabaseUpdateUi updateProgress2) {
		LiquibaseDBUpdater updater = new LiquibaseDBUpdater(dataSource, updateProgress);
		updateSuccess = updater.update();
	}

	private void dbInit(IDatabaseUpdateUi updateProgress2) {
		LiquibaseDBInitializer initializer = new LiquibaseDBInitializer(dataSource, updateProgress);
		initializer.init();
	}

	@Override
	public boolean isUpdateSuccess() {
		return updateSuccess;
	}

	private EntityManager createManagedEntityManager() {
		logger.debug("Creating new EntityManager for Thread [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		EntityManager em = factory.createEntityManager();
		threadLocal.set(em);
		threadManagerMap.put(Thread.currentThread(), em);
		return em;
	}

	@Override
	public synchronized void closeEntityManager(Object em) {
		if (threadLocal.get() == em) {
			threadLocal.set(null);
			threadManagerMap.remove(Thread.currentThread());
		}
		((EntityManager) em).close();
	}

	@Override
	public void clearCache() {
		factory.getCache().evictAll();
	}

	private class EntityManagerCollector implements Runnable {
		@Override
		public void run() {
			if (threadManagerMap != null && !threadManagerMap.isEmpty()) {
				for (Thread thread : threadManagerMap.keySet().toArray(new Thread[0])) {
					if (!thread.isAlive()) {
						logger.debug("Closing EntityManager of non active thread [" + thread.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
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
	public boolean executeSQLScript(String changeId, String sqlScript) {
		if (CoreUtil.isTestMode() || Boolean.valueOf(System.getProperty("forceExecuteSqlScript"))) { //$NON-NLS-1$
			LiquibaseDBScriptExecutor executor = new LiquibaseDBScriptExecutor(dataSource);
			return executor.execute(changeId, sqlScript);
		}
		logger.warn("Did not execute script [" + changeId + "] as system not started in mode " + CoreUtil.TEST_MODE); //$NON-NLS-1$ //$NON-NLS-2$
		return false;
	}
}
