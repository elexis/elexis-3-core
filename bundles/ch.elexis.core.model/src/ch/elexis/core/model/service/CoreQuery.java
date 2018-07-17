package ch.elexis.core.model.service;

import javax.persistence.EntityManager;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.jpa.model.adapter.AbstractModelQuery;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;

public class CoreQuery<T> extends AbstractModelQuery<T> implements IQuery<T> {
	
	public CoreQuery(Class<T> clazz, EntityManager entityManager){
		this(clazz, entityManager, false);
	}
	
	public CoreQuery(Class<T> clazz, EntityManager entityManager, boolean includeDeleted){
		super(clazz, entityManager, includeDeleted);
	}
	
	@Override
	protected void initialize(){
		adapterFactory = CoreModelAdapterFactory.getInstance();
		
		entityClazz = adapterFactory.getEntityClass(clazz);
		
		criteriaQuery = criteriaBuilder.createQuery(entityClazz);
		rootQuery = criteriaQuery.from(entityClazz);
		
		if (AbstractDBObjectIdDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
	}
}
