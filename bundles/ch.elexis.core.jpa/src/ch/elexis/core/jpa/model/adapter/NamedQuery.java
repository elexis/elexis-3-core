package ch.elexis.core.jpa.model.adapter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.services.INamedQuery;

public class NamedQuery<T> implements INamedQuery<T> {
	
	private EntityManager entityManager;
	private AbstractModelAdapterFactory adapterFactory;
	private Class<T> interfaceClazz;
	
	private Class<? extends EntityWithId> entityClazz;
	private TypedQuery<?> query;
	
	public NamedQuery(Class<T> clazz, AbstractModelAdapterFactory adapterFactory,
		EntityManager entityManager, String queryName){
		this.adapterFactory = adapterFactory;
		this.interfaceClazz = clazz;
		this.entityClazz = adapterFactory.getEntityClass(interfaceClazz);
		
		this.query = entityManager.createNamedQuery(queryName, entityClazz);
		this.entityManager = entityManager;
	}
	
	protected Object resolveValue(Object value){
		Object ret = value;
		if (value instanceof AbstractIdModelAdapter) {
			ret = ((AbstractIdModelAdapter<?>) value).getEntity();
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> executeWithParameters(Map<String, Object> paramters){
		try {
			paramters.forEach((k, v) -> {
				v = resolveValue(v);
				query.setParameter(k, v);
			});
			List<T> ret = (List<T>) query.getResultStream().parallel()
				.map(e -> adapterFactory
					.getModelAdapter((EntityWithId) e, interfaceClazz, true).orElse(null))
				.filter(o -> o != null).collect(Collectors.toList());
			return ret;
		} finally {
			entityManager.close();
		}
	}
}
