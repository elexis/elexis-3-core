package ch.elexis.core.jpa.model.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.provider.JpaProviderAdapter;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQueryCursor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class NamedQuery<R, T> implements INamedQuery<R> {

	private AbstractModelAdapterFactory adapterFactory;
	private Class<T> interfaceClazz;
	private Class<R> returnValueClazz;
	protected int limit;

	private Class<? extends EntityWithId> entityClazz;
	private TypedQuery<?> query;

	private IAccessControlService accessControlService;

	public NamedQuery(Class<R> returnValueClazz, Class<T> interfaceClazz, boolean refreshCache,
			AbstractModelAdapterFactory adapterFactory, EntityManager entityManager, String queryName) {
		this.adapterFactory = adapterFactory;
		this.interfaceClazz = interfaceClazz;
		this.returnValueClazz = returnValueClazz;
		this.entityClazz = adapterFactory.getEntityClass(interfaceClazz);

		if (returnValueClazz.equals(interfaceClazz)) {
			this.query = entityManager.createNamedQuery(queryName, entityClazz);
		} else {
			this.query = entityManager.createNamedQuery(queryName, returnValueClazz);
		}
		// update cache with results
		// (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			if (refreshCache) {
				// eclipselink
				// queryHints.put(QueryHints.REFRESH, HintValues.TRUE);
				this.query.setHint("eclipselink.refresh", "True");
				Map<String, Object> hints = query.getHints();
				if (hints != null && hints.containsKey("eclipselink.query-results-cache")) {
					this.query.setHint("eclipselink.cache-usage", "Invalidate");
				}
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
			accessControlService = PortableServiceLoader.getOptional(IAccessControlService.class).orElse(null);
		}
		parameters.put("aoboids", accessControlService.getAoboMandatorIdsForSqlIn());
	}

	private boolean isAoboQuery() {
		return query.getParameters().stream().filter(p -> p.getName().equals("aoboids")).findFirst().isPresent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IQueryCursor<R> executeAsCursorWithParameters(Map<String, Object> parameters) {
		if (isAoboQuery()) {
			addAoboParameter(parameters);
		}
		parameters.forEach((k, v) -> {
			v = resolveValue(v);
			query.setParameter(k, v);
		});

		JpaProviderAdapter jpaProviderAdapter = PortableServiceLoader.getOptional(JpaProviderAdapter.class)
				.orElseThrow();

		if (returnValueClazz.equals(interfaceClazz)) {
			return (IQueryCursor<R>) jpaProviderAdapter.executeAsCursor(adapterFactory, interfaceClazz, query, null);
		} else {
			return (IQueryCursor<R>) jpaProviderAdapter.executeAsCursor(null, null, query, null);
		}
	}

	@SuppressWarnings("unchecked")
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

	@Override
	public INamedQuery<R> limit(int limit) {
		this.limit = limit;
		return this;
	}
}
