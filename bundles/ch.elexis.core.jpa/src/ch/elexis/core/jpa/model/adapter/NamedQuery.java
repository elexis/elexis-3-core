package ch.elexis.core.jpa.model.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.services.INamedQuery;

public class NamedQuery<R, T> implements INamedQuery<R> {
	
	private AbstractModelAdapterFactory adapterFactory;
	private Class<T> interfaceClazz;
	private Class<R> returnValueClazz;
	
	private Class<? extends EntityWithId> entityClazz;
	private TypedQuery<?> query;
	
	public NamedQuery(Class<R> returnValueClazz, Class<T> interfaceClazz, boolean refreshCache,
		AbstractModelAdapterFactory adapterFactory, EntityManager entityManager, String queryName){
		this.adapterFactory = adapterFactory;
		this.interfaceClazz = interfaceClazz;
		this.returnValueClazz = returnValueClazz;
		this.entityClazz = adapterFactory.getEntityClass(interfaceClazz);
		
		this.query = entityManager.createNamedQuery(queryName, entityClazz);
		// update cache with results (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			this.query.setHint(QueryHints.REFRESH, HintValues.TRUE);
		}
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
	public List<R> executeWithParameters(Map<String, Object> parameters){
		parameters.forEach((k, v) -> {
			v = resolveValue(v);
			query.setParameter(k, v);
		});
		if (returnValueClazz.equals(interfaceClazz)) {
			List<R> ret = (List<R>) query
				.getResultStream().parallel().map(e -> adapterFactory
					.getModelAdapter((EntityWithId) e, interfaceClazz, true).orElse(null))
				.filter(o -> o != null).collect(Collectors.toList());
			return ret;
		} else {
			// query result list can contain null values, we do not want to see them
			return (List<R>) query.getResultList().parallelStream().filter(r -> r != null)
				.collect(Collectors.toList());
		}
	}
	
	@Override
	public Optional<R> executeWithParametersSingleResult(Map<String, Object> parameters) {
		List<R> result = executeWithParameters(parameters);
		if (!result.isEmpty()) {
			if (result.size() > 1) {
				LoggerFactory.getLogger(getClass()).warn("Multiple results in list where single result expected",
						new Throwable());
			}
			return Optional.of(result.get(0));
		}
		return Optional.empty();
	}
}
