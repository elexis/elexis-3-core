package ch.elexis.core.jpa.entities.entitymanager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IElexisEntityManager;

@Component
public class ElexisEntityManagerServiceHolder {
	
	private static IElexisEntityManager entityManager;
	
	@Reference
	public synchronized void ElexisEntityManager(IElexisEntityManager entityManager){
		ElexisEntityManagerServiceHolder.entityManager = entityManager;
	}
	
	public static IElexisEntityManager getEntityManager(){
		if (entityManager == null) {
			throw new IllegalStateException("No EntityManager available");
		}
		return entityManager;
	}
}
