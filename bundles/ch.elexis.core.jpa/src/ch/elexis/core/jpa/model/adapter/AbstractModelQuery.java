package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.AoboEntity;
import ch.elexis.core.ac.AoboEntityColumn;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.internal.PredicateGroupStack;
import ch.elexis.core.jpa.model.adapter.internal.PredicateHandler;
import ch.elexis.core.jpa.model.adapter.internal.QueryCursor;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.utils.OsgiServiceUtil;

/**
 * Abstract super class for JPA based {@link IQuery} implementations.
 *
 * @author thomas
 *
 * @param <T>
 */
public abstract class AbstractModelQuery<T> implements IQuery<T> {

	protected Class<T> clazz;

	protected EntityManager entityManager;
	protected CriteriaBuilder criteriaBuilder;

	protected List<Order> orderByList;

	protected CriteriaQuery<?> criteriaQuery;
	protected Root<? extends EntityWithId> rootQuery;

	protected AbstractModelAdapterFactory adapterFactory;

	protected Class<? extends EntityWithId> entityClazz;

	protected boolean includeDeleted;
	protected boolean refreshCache;
	protected int limit;
	protected int offset;

	private PredicateGroupStack predicateGroups;
	private PredicateHandler predicateHandler;

	private IAccessControlService accessControlService;

	public AbstractModelQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager,
			boolean includeDeleted) {
		this.clazz = clazz;
		this.entityManager = entityManager;
		this.criteriaBuilder = entityManager.getCriteriaBuilder();
		this.includeDeleted = includeDeleted;
		this.refreshCache = refreshCache;
		this.predicateGroups = new PredicateGroupStack(criteriaBuilder);
		this.orderByList = new ArrayList<>();

		initialize();
		// now entityClazz, rootQuery and adapterFactory can be used ...
		this.predicateHandler = new PredicateHandler(predicateGroups, entityClazz, criteriaBuilder, rootQuery);

		if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
		addAobo();

		MappingEntry mappingForInterface = adapterFactory.getMappingForInterface(clazz);
		mappingForInterface.applyQueryPrecondition(this);
	}

	private void addAobo() {
		if (isAoboClass(entityClazz)) {
			Optional<ACEAccessBitMapConstraint> aoboOrSelf = isAoboOrSelf(entityClazz);
			Field aoboColumn = getAoboColumn(entityClazz);

			startGroup();
			or(aoboColumn.getName(), COMPARATOR.EQUALS, null);
			if (aoboOrSelf.get() == ACEAccessBitMapConstraint.AOBO) {
				or(aoboColumn.getName(), COMPARATOR.IN, getAoboMandatorIds());
			} else if (aoboOrSelf.get() == ACEAccessBitMapConstraint.SELF) {
				or(aoboColumn.getName(), COMPARATOR.EQUALS, getSelfMandatorId());
			}
			andJoinGroups();
		}
	}

	private Field getAoboColumn(Class<? extends EntityWithId> entityClazz) {
		for (Field field : entityClazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(AoboEntityColumn.class)) {
				return field;
			}
		}
		throw new IllegalStateException(
				"AOBO entity class [" + entityClazz.getName() + "] has no AOBO column annotation");
	}

	private boolean isAoboClass(Class<? extends EntityWithId> entityClazz) {
		return entityClazz.isAnnotationPresent(AoboEntity.class) && isAoboAccessControl(entityClazz);
	}

	private String getSelfMandatorId() {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		return accessControlService != null ? accessControlService.getSelfMandatorId() : "-1";
	}

	private List<String> getAoboMandatorIds() {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		return accessControlService != null ? accessControlService.getAoboMandatorIds()
				: Collections.singletonList("-1");
	}

	private boolean isAoboAccessControl(Class<? extends EntityWithId> entityClazz) {
		return accessControlService != null ? isAoboOrSelf(entityClazz).isPresent() : false;
	}

	private Optional<ACEAccessBitMapConstraint> isAoboOrSelf(Class<? extends EntityWithId> entityClazz) {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		return accessControlService.isAoboOrSelf(new ObjectEvaluatableACE(entityClazz, Right.READ));
	}

	/**
	 * Initialize the {@link AbstractModelAdapterFactory} and dependent fields
	 * entityClazz, criteriaQuery and rootQuery.
	 *
	 */
	protected abstract void initialize();

	@Override
	public IQuery<T> and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		predicateHandler.and(feature, comparator, value, ignoreCase);
		return this;
	}

	@Override
	public IQuery<T> and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		predicateHandler.and(entityAttributeName, comparator, value, ignoreCase);
		return this;
	}

	@Override
	public IQuery<T> andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
			EStructuralFeature otherFeature) {
		predicateHandler.andFeatureCompare(feature, comparator, otherFeature);
		return this;
	}

	@Override
	public IQuery<T> or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		predicateHandler.or(feature, comparator, value, ignoreCase);
		return this;
	}

	@Override
	public IQuery<T> or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		predicateHandler.or(entityAttributeName, comparator, value, ignoreCase);
		return this;
	}

	@Override
	public IQuery<T> orderBy(EStructuralFeature feature, ORDER order) {
		String entityAttributeName = predicateHandler.getAttributeName(feature, entityClazz);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = predicateHandler.resolveAttribute(entityClazz.getName(),
				entityAttributeName);
		if (attribute.isPresent()) {
			orderBy(attribute.get(), order);
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + entityAttributeName + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void orderBy(SingularAttribute attribute, ORDER direction) {
		Order orderBy = null;
		if (direction == ORDER.ASC) {
			orderBy = criteriaBuilder.asc(rootQuery.get(attribute));
		} else if (direction == ORDER.DESC) {
			orderBy = criteriaBuilder.desc(rootQuery.get(attribute));
		}
		if (orderBy != null) {
			orderByList.add(orderBy);
		}
	}

	@Override
	public IQuery<T> orderBy(String fieldOrderBy, ORDER order) {
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = predicateHandler.resolveAttribute(entityClazz.getName(), fieldOrderBy);
		if (attribute.isPresent()) {
			orderBy(attribute.get(), order);
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + fieldOrderBy + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return this;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IQuery<T> orderByLeftPadded(String fieldOrderBy, ORDER order) {
		Optional<SingularAttribute> attribute = predicateHandler.resolveAttribute(entityClazz.getName(), fieldOrderBy);
		if (attribute.isPresent()) {
			Expression<String> function = criteriaBuilder.function("LPAD", String.class, //$NON-NLS-1$
					rootQuery.get(attribute.get()), criteriaBuilder.literal(10), criteriaBuilder.literal('0'));
			Order orderBy = criteriaBuilder.asc(function);
			criteriaQuery.orderBy(orderBy);
			orderByList.add(orderBy);
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + fieldOrderBy + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return this;
	}

	@Override
	public IQuery<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public IQuery<T> offset(int offset) {
		this.offset = offset;
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Case<Object> getCaseExpression(Map<String, Object> caseContext) {
		Case<Object> caseExpression = criteriaBuilder.selectCase();
		for (String caseInfo : caseContext.keySet()) {
			caseInfo = caseInfo.toLowerCase();
			Object value = caseContext.get(caseInfo);
			if (caseInfo.startsWith("when")) { //$NON-NLS-1$
				String[] parts = caseInfo.split("\\|"); //$NON-NLS-1$
				if (parts.length == 4) {
					Optional<SingularAttribute> attribute = predicateHandler.resolveAttribute(entityClazz.getName(),
							parts[1]);
					if (attribute.isPresent()) {
						if ("equals".equals(parts[2])) { //$NON-NLS-1$
							caseExpression.when(criteriaBuilder.equal(rootQuery.get(attribute.get()), parts[3]), value);
						} else if ("like".equals(parts[2])) { //$NON-NLS-1$
							caseExpression.when(criteriaBuilder.like(rootQuery.get(attribute.get()), parts[3]), value);
						}
					} else {
						throw new IllegalStateException("[" + parts[1] + "] is not a known attribute"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					throw new IllegalStateException("[" + caseInfo + "] is not in a known format"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (caseInfo.startsWith("otherwise")) { //$NON-NLS-1$
				caseExpression.otherwise(value);
			}
		}
		return caseExpression;
	}

	@Override
	public IQuery<T> orderBy(Map<String, Object> caseContext, ORDER order) {
		if (caseContext != null && !caseContext.isEmpty()) {
			Case<Object> caseExpression = getCaseExpression(caseContext);
			Order orderBy = null;
			if (order == ORDER.ASC) {
				orderBy = criteriaBuilder.asc(caseExpression);
			} else if (order == ORDER.DESC) {
				orderBy = criteriaBuilder.desc(caseExpression);
			}
			if (orderBy != null) {
				orderByList.add(orderBy);
			}
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> ISubQuery<S> createSubQuery(Class<S> clazz, IModelService modelService) {
		Class<? extends EntityWithId> subEntityClazz = (Class<? extends EntityWithId>) modelService
				.getEntityClass(clazz);
		Subquery<? extends EntityWithId> sq = criteriaQuery.subquery(subEntityClazz);
		return new SubQuery<>(sq, subEntityClazz);
	}

	@Override
	public IQuery<T> exists(ISubQuery<?> subQuery) {
		predicateHandler.exists((Subquery<?>) subQuery.getQuery());
		return this;
	}

	@Override
	public IQuery<T> notExists(ISubQuery<?> subQuery) {
		predicateHandler.notExists((Subquery<?>) subQuery.getQuery());
		return this;
	}

	@Override
	public IQuery<T> startGroup() {
		predicateGroups.createPredicateGroup();
		return this;
	}

	@Override
	public IQuery<T> andJoinGroups() {
		predicateGroups.andPredicateGroups();
		return this;
	}

	@Override
	public IQuery<T> orJoinGroups() {
		predicateGroups.orPredicateGroups();
		return this;
	}

	private TypedQuery<?> getTypedQuery() {
		// apply the predicate groups to the criteriaQuery
		int groups = predicateGroups.getPredicateGroupsSize();
		if (groups > 0) {
			if (groups == 2 && (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted)) {
				andJoinGroups();
				groups = predicateGroups.getPredicateGroupsSize();
			}

			if (groups == 1) {
				criteriaQuery = criteriaQuery.where(predicateGroups.getCurrentPredicateGroup().getPredicate());
			} else {
				throw new IllegalStateException("Query has open groups [" + groups + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			criteriaQuery.orderBy(orderByList);
		}
		TypedQuery<?> query = entityManager.createQuery(criteriaQuery);
		// update cache with results
		// (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			query.setHint(QueryHints.REFRESH, HintValues.TRUE);
		}
		if (limit > 0) {
			query.setMaxResults(limit);
		}
		if (offset > 0) {
			query.setFirstResult(offset);
		}
		return query;
	}

	@Override
	public IQueryCursor<T> executeAsCursor() {
		return executeAsCursor(null);
	}

	@Override
	public IQueryCursor<T> executeAsCursor(Map<String, Object> queryHints) {
		TypedQuery<?> query = getTypedQuery();
		if (queryHints == null) {
			query.setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE);
		} else {
			queryHints.forEach(query::setHint);
		}
		query.setHint(QueryHints.SCROLLABLE_CURSOR, HintValues.TRUE);
		ScrollableCursor cursor = (ScrollableCursor) query.getSingleResult();
		return new QueryCursor<>(cursor, adapterFactory, clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> execute() {
		List<T> ret = (List<T>) getTypedQuery().getResultStream().parallel()
				.map(e -> adapterFactory.getModelAdapter((EntityWithId) e, clazz, true).orElse(null))
				.filter(Objects::nonNull).collect(Collectors.toList());
		// detach and clear L1 cache
		entityManager.clear();
		return ret;
	}

	@Override
	public Optional<T> executeSingleResult() {
		List<T> result = execute();
		if (!result.isEmpty()) {
			if (result.size() > 1) {
				StringBuilder info = new StringBuilder();
				info.append(result.get(0).getClass().getName() + ": "); //$NON-NLS-1$
				for (T t : result) {
					if (t instanceof Identifiable) {
						info.append(((Identifiable) t).getId() + StringUtils.SPACE);
					}
				}
				LoggerFactory.getLogger(getClass()).warn(
						"Multiple results where single expected. Returning first element of [{}]", info.toString(), //$NON-NLS-1$
						new Throwable());
			}
			return Optional.of(result.get(0));
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		Session session = entityManager.unwrap(JpaEntityManager.class).getActiveSession();
		DatabaseQuery databaseQuery = ((JpaQuery<?>) getTypedQuery()).getDatabaseQuery();
		databaseQuery.prepareCall(session, new DatabaseRecord());
		String sqlString = databaseQuery.getSQLString();
		return sqlString;
	}

	private class SubQuery<S> implements ISubQuery<S> {

		private Subquery<? extends EntityWithId> subQuery;
		private Root<? extends EntityWithId> subRootQuery;

		private Class<? extends EntityWithId> entityClazz;

		private PredicateGroupStack predicateGroups;
		private PredicateHandler predicateHandler;

		public SubQuery(Subquery<? extends EntityWithId> subQuery, Class<? extends EntityWithId> entityClazz) {
			this.subQuery = subQuery;
			this.entityClazz = entityClazz;
			this.subRootQuery = subQuery.from(entityClazz);
			this.predicateGroups = new PredicateGroupStack(criteriaBuilder);
			this.predicateHandler = new PredicateHandler(predicateGroups, entityClazz, criteriaBuilder, subRootQuery);

			if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
				and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
			}
		}

		@Override
		public Object getQuery() {
			// apply the predicate groups to the subQuery
			int groups = predicateGroups.getPredicateGroupsSize();
			if (groups > 0) {
				if (groups == 2 && (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted)) {
					andJoinGroups();
					groups = predicateGroups.getPredicateGroupsSize();
				}

				if (groups == 1) {
					subQuery = subQuery.where(predicateGroups.getCurrentPredicateGroup().getPredicate());
				} else {
					throw new IllegalStateException("Query has open groups [" + groups + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			return subQuery;
		}

		@Override
		public void startGroup() {
			predicateGroups.createPredicateGroup();
		}

		@Override
		public void andJoinGroups() {
			predicateGroups.andPredicateGroups();
		}

		@Override
		public void orJoinGroups() {
			predicateGroups.orPredicateGroups();
		}

		@Override
		public void and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
			predicateHandler.and(feature, comparator, value, ignoreCase);
		}

		@Override
		public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
				EStructuralFeature otherFeature) {
			predicateHandler.andFeatureCompare(feature, comparator, otherFeature);
		}

		@Override
		public void and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
			predicateHandler.and(entityAttributeName, comparator, value, ignoreCase);
		}

		@Override
		public void or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
			predicateHandler.or(feature, comparator, value, ignoreCase);
		}

		@Override
		public void or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
			predicateHandler.or(entityAttributeName, comparator, value, ignoreCase);
		}

		@Override
		public void andParentCompare(String parentEntityAttributeName, COMPARATOR comparator,
				String entityAttributeName) {
			predicateHandler.andCompare(AbstractModelQuery.this.rootQuery, AbstractModelQuery.this.entityClazz,
					parentEntityAttributeName, comparator, entityAttributeName);
		}
	}
}
