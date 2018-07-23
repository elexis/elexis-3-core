package ch.elexis.core.jpa.entitymanager;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.osgi.service.component.annotations.Component;
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
	
	private static Logger log = LoggerFactory.getLogger(ElexisEntityManger.class);
	
	private EntityManagerFactoryBuilder factoryBuilder;
	
	private EntityManagerFactory factory;
	
	private DataSource dataSource;
	
	@Reference(service = DataSource.class, cardinality = ReferenceCardinality.MANDATORY)
	protected synchronized void bindDataSource(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	@Reference(service = EntityManagerFactoryBuilder.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, target = "(osgi.unit.name=elexis)")
	protected synchronized void bind(EntityManagerFactoryBuilder factoryBuilder){
		log.debug("Binding " + factoryBuilder.getClass().getName());
		this.factoryBuilder = factoryBuilder;
	}
	
	/**
	 * Get an {@link EntityManager} instance for the Elexis persistence unit.
	 * 
	 * @return
	 */
	public synchronized EntityManager getEntityManager(){
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
			return factory.createEntityManager();
		} else {
			throw new IllegalStateException("No EntityManagerFactory available");
		}
	}
}
