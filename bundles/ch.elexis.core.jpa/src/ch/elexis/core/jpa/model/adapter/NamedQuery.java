package ch.elexis.core.jpa.model.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.ScrollableCursor;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.internal.QueryCursor;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class NamedQuery<R, T> implements INamedQuery<R> {

	private AbstractModelAdapterFactory adapterFactory;
	private Class<T> interfaceClazz;
	private Class<R> returnValueClazz;

	private Class<? extends EntityWithId> entityClazz;
	private TypedQuery<?> query;
	private EntityManager entityManager;

	private IAccessControlService accessControlService;

	public NamedQuery(Class<R> returnValueClazz, Class<T> interfaceClazz, boolean refreshCache,
			AbstractModelAdapterFactory adapterFactory, EntityManager entityManager, String queryName) {
		this.adapterFactory = adapterFactory;
		this.interfaceClazz = interfaceClazz;
		this.returnValueClazz = returnValueClazz;
		this.entityClazz = adapterFactory.getEntityClass(interfaceClazz);
		this.entityManager = entityManager;

		this.query = entityManager.createNamedQuery(queryName, entityClazz);
		// update cache with results
		// (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			this.query.setHint(QueryHints.REFRESH, HintValues.TRUE);
			Map<String, Object> hints = query.getHints();
			if (hints != null && hints.containsKey(QueryHints.QUERY_RESULTS_CACHE)) {
				this.query.setHint(QueryHints.CACHE_USAGE, CacheUsage.Invalidate);
			}
		}
	}

	protected Object resolveValue(Object value) {
		Object ret = value;
		if (value instanceof AbstractIdModelAdapter) {
			ret = ((AbstractIdModelAdapter<?>) value).getEntity();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<R> executeWithParameters(Map<String, Object> parameters) {
		if (isAoboQuery()) {
			addAoboParameter(parameters);
		}
		parameters.forEach((k, v) -> {
			v = resolveValue(v);
			query.setParameter(k, v);
		});
		if (returnValueClazz.equals(interfaceClazz)) {
			List<R> ret = (List<R>) query.getResultStream().parallel()
					.map(e -> adapterFactory.getModelAdapter((EntityWithId) e, interfaceClazz, true).orElse(null))
					.filter(o -> o != null).collect(Collectors.toList());
			return ret;
		} else {
			// query result list can contain null values, we do not want to see them
			return (List<R>) query.getResultList().parallelStream().filter(r -> r != null).collect(Collectors.toList());
		}
	}

	private void addAoboParameter(Map<String, Object> parameters) {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		parameters.put("aoboids", accessControlService.getAoboMandatorIdsForSqlIn());
	}

	private boolean isAoboQuery() {
		return query.getParameters().stream().filter(p -> p.getName().equals("aoboids")).findFirst().isPresent();
	}

	@Override
	public IQueryCursor<R> executeAsCursorWithParameters(Map<String, Object> parameters) {
		if (isAoboQuery()) {
			addAoboParameter(parameters);
		}
		parameters.forEach((k, v) -> {
			v = resolveValue(v);
			query.setParameter(k, v);
		});
		query.setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE);
		query.setHint(QueryHints.SCROLLABLE_CURSOR, HintValues.TRUE);
		if (returnValueClazz.equals(interfaceClazz)) {
			ScrollableCursor cursor = (ScrollableCursor) query.getSingleResult();
			return new QueryCursor<>(cursor, adapterFactory, interfaceClazz);
		} else {
			ScrollableCursor cursor = (ScrollableCursor) query.getSingleResult();
			return new QueryCursor<>(cursor, null, null);
		}
	}

	@Override
	public Optional<R> executeWithParametersSingleResult(Map<String, Object> parameters) {
		if (isAoboQuery()) {
			addAoboParameter(parameters);
		}
		parameters.forEach((k, v) -> {
			v = resolveValue(v);
			query.setParameter(k, v);
		});
		if (returnValueClazz.equals(interfaceClazz)) {
			Optional<?> findFirst = query.getResultStream().findFirst();
			if (findFirst.isPresent()) {
				@SuppressWarnings("unchecked")
				R orElse = (R) adapterFactory.getModelAdapter((EntityWithId) findFirst.get(), interfaceClazz, true)
						.orElse(null);
				return Optional.ofNullable(orElse);
			}

		} else {
			// query result list can contain null values, we do not want to see them
			return (Optional<R>) query.getResultStream().findFirst();
		}
		return Optional.empty();
	}
}
