package ch.elexis.core.jpa.entitymanger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;
import ch.elexis.core.jpa.test.AllPluginTests;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.OsgiServiceUtil;

public class InitPersistenceUnit {
	
	private static IElexisDataSource elexisDataSource;
	
	@BeforeClass
	public static void beforeClass(){
		elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class).get();
		elexisDataSource.setDBConnection(AllPluginTests.getTestDatabaseConnection());
	}
	
	@AfterClass
	public static void afterClass(){
		OsgiServiceUtil.ungetService(elexisDataSource);
	}
	
	@Test
	public void getEntityManger(){
		Optional<ElexisEntityManger> elexisEntityManager =
			OsgiServiceUtil.getService(ElexisEntityManger.class);
		assertTrue(elexisEntityManager.isPresent());
		EntityManager em = elexisEntityManager.get().getEntityManager();
		assertNotNull(em);
		em.close();
		OsgiServiceUtil.ungetService(elexisEntityManager.get());
	}
	
	@Test
	public void createKontakt(){
		Optional<ElexisEntityManger> elexisEntityManager =
			OsgiServiceUtil.getService(ElexisEntityManger.class);
		EntityManager em = elexisEntityManager.get().getEntityManager();
		assertNotNull(em);
		Kontakt kontakt = new Kontakt();
		kontakt.setDescription1("test");
		em.persist(kontakt);
		assertTrue(em.contains(kontakt));
		assertNotNull(kontakt.getId());
		em.remove(kontakt);
		assertFalse(em.contains(kontakt));
		em.close();
		OsgiServiceUtil.ungetService(elexisEntityManager.get());
	}
}
