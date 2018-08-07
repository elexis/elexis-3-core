package ch.elexis.core.eigendiagnosen.model.service;

import javax.persistence.EntityManager;

import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.model.adapter.AbstractModelQuery;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;

public class DiagnosisQuery<T> extends AbstractModelQuery<T> implements IQuery<T> {
	
	public DiagnosisQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager,
		boolean includeDeleted){
		super(clazz, refreshCache, entityManager, includeDeleted);
	}
	
	@Override
	protected void initialize(){
		adapterFactory = DiagnosisModelAdapterFactory.getInstance();
		
		entityClazz = adapterFactory.getEntityClass(clazz);
		
		criteriaQuery = criteriaBuilder.createQuery(entityClazz);
		rootQuery = criteriaQuery.from(entityClazz);
		
		if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
	}
}
