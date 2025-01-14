package ch.elexis.core.findings.fhir.model.service;

import ch.elexis.core.jpa.model.adapter.AbstractModelQuery;
import ch.elexis.core.services.IQuery;
import jakarta.persistence.EntityManager;

public class FindingsQuery<T> extends AbstractModelQuery<T> implements IQuery<T> {

	public FindingsQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager) {
		this(clazz, refreshCache, entityManager, false);
	}

	public FindingsQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager, boolean includeDeleted) {
		super(clazz, refreshCache, entityManager, includeDeleted);
	}

	@Override
	protected void initialize() {
		adapterFactory = FindingsModelAdapterFactory.getInstance();

		entityClazz = adapterFactory.getEntityClass(clazz);

		criteriaQuery = criteriaBuilder.createQuery(entityClazz);
		rootQuery = criteriaQuery.from(entityClazz);
	}
}
