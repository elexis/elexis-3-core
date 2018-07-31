package ch.elexis.core.findings.fhir.model.service;

import javax.persistence.EntityManager;

import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.model.adapter.AbstractModelQuery;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;

public class FindingsQuery<T> extends AbstractModelQuery<T> implements IQuery<T> {
	
	public FindingsQuery(Class<T> clazz, EntityManager entityManager){
		this(clazz, entityManager, false);
	}
	
	public FindingsQuery(Class<T> clazz, EntityManager entityManager, boolean includeDeleted){
		super(clazz, entityManager, includeDeleted);
	}
	
	@Override
	protected void initialize(){
		adapterFactory = FindingsModelAdapterFactory.getInstance();
		
		entityClazz = adapterFactory.getEntityClass(clazz);
		
		criteriaQuery = criteriaBuilder.createQuery(entityClazz);
		rootQuery = criteriaQuery.from(entityClazz);
		
		if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
	}
}
