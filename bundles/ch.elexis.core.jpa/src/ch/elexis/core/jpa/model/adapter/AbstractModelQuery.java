package ch.elexis.core.jpa.model.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaQuery;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.internal.PredicateGroupStack;
import ch.elexis.core.jpa.model.adapter.internal.PredicateHandler;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;

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
	protected List<Selection<?>> selections;
	
	protected CriteriaQuery<?> criteriaQuery;
	protected Root<? extends EntityWithId> rootQuery;
	
	protected AbstractModelAdapterFactory adapterFactory;
	
	protected Class<? extends EntityWithId> entityClazz;
	
	protected boolean includeDeleted;
	protected boolean refreshCache;
	protected boolean ignoreCaching;
	
	private PredicateGroupStack predicateGroups;
	private PredicateHandler predicateHandler;

	public AbstractModelQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager,
		boolean includeDeleted){
		this.clazz = clazz;
		this.entityManager = entityManager;
		this.criteriaBuilder = entityManager.getCriteriaBuilder();
		this.includeDeleted = includeDeleted;
		this.refreshCache = refreshCache;
		this.predicateGroups = new PredicateGroupStack(criteriaBuilder);
		this.orderByList = new ArrayList<>();
		
		initialize();
		// now entityClazz, rootQuery and adapterFactory can be used ...
		this.predicateHandler =
			new PredicateHandler(predicateGroups, entityClazz, criteriaBuilder, rootQuery);
		
		if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
	}
	
	/**
	 * Initialize the {@link AbstractModelAdapterFactory} and dependent fields entityClazz,
	 * criteriaQuery and rootQuery.
	 * 
	 */
	protected abstract void initialize();
	
	@Override
	public void and(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		predicateHandler.and(feature, comparator, value, ignoreCase);
	}
	
	@Override
	public void and(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		predicateHandler.and(entityAttributeName, comparator, value, ignoreCase);
	}
	
	@Override
	public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
		EStructuralFeature otherFeature){
		predicateHandler.andFeatureCompare(feature, comparator, otherFeature);
	}
	
	@Override
	public void or(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		predicateHandler.or(feature, comparator, value, ignoreCase);
	}
	
	@Override
	public void or(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		predicateHandler.or(entityAttributeName, comparator, value, ignoreCase);
	}
	
	public void orderBy(EStructuralFeature feature, ORDER order){
		String entityAttributeName = predicateHandler.getAttributeName(feature, entityClazz);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			predicateHandler.resolveAttribute(entityClazz.getName(), entityAttributeName);
		if (attribute.isPresent()) {
			orderBy(attribute.get(), order);
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException("Could not resolve attribute [" + entityAttributeName
				+ "] of entity [" + entityClazz + "]");
		}
	}
	
	@SuppressWarnings({
		"unchecked", "rawtypes"
	})
	private void orderBy(SingularAttribute attribute, ORDER direction){
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
	
	public void orderBy(String fieldOrderBy, ORDER order){
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			predicateHandler.resolveAttribute(entityClazz.getName(), fieldOrderBy);
		if (attribute.isPresent()) {
			orderBy(attribute.get(), order);
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException("Could not resolve attribute [" + fieldOrderBy
				+ "] of entity [" + entityClazz + "]");
		}
	}
	
	@Override
	public void selections(String[] fields){
		selections = new ArrayList<>();
		for (String field : fields) {
			Path<Object> selection = rootQuery.get(field);
			selections.add(selection);
		}
		criteriaQuery.multiselect(selections);
	}
	
	@Override
	public void ignoreCaching(){
		ignoreCaching = true;
	}
	
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	private Case<Object> getCaseExpression(Map<String, Object> caseContext){
		Case<Object> caseExpression = criteriaBuilder.selectCase();
		for (String caseInfo : caseContext.keySet()) {
			caseInfo = caseInfo.toLowerCase();
			Object value = caseContext.get(caseInfo);
			if (caseInfo.startsWith("when")) {
				String[] parts = caseInfo.split("\\|");
				if (parts.length == 4) {
					Optional<SingularAttribute> attribute =
						predicateHandler.resolveAttribute(entityClazz.getName(), parts[1]);
					if (attribute.isPresent()) {
						if ("equals".equals(parts[2])) {
							caseExpression.when(
								criteriaBuilder.equal(rootQuery.get(attribute.get()), parts[3]),
								value);
						} else if ("like".equals(parts[2])) {
							caseExpression.when(
								criteriaBuilder.like(rootQuery.get(attribute.get()), parts[3]),
								value);
						}
					} else {
						throw new IllegalStateException(
							"[" + parts[1] + "] is not a known attribute");
					}
				} else {
					throw new IllegalStateException("[" + caseInfo + "] is not in a known format");
				}
			} else if (caseInfo.startsWith("otherwise")) {
				caseExpression.otherwise(value);
			}
		}
		return caseExpression;
	}
	
	public void orderBy(Map<String, Object> caseContext, ORDER order){
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
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> ISubQuery<S> createSubQuery(Class<S> clazz, IModelService modelService){
		Class<? extends EntityWithId> subEntityClazz =
			(Class<? extends EntityWithId>) modelService.getEntityClass(clazz);
		Subquery<? extends EntityWithId> sq = criteriaQuery.subquery(subEntityClazz);
		return new SubQuery<S>(sq, subEntityClazz);
	}
	
	@Override
	public void exists(ISubQuery<?> subQuery){
		predicateHandler.exists((Subquery<?>) subQuery.getQuery());
	}
	
	@Override
	public void notExists(ISubQuery<?> subQuery){
		predicateHandler.notExists((Subquery<?>) subQuery.getQuery());
	}
	
	@Override
	public void startGroup(){
		predicateGroups.createPredicateGroup();
	}
	
	@Override
	public void andJoinGroups(){
		predicateGroups.andPredicateGroups();
	}
	
	@Override
	public void orJoinGroups(){
		predicateGroups.orPredicateGroups();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> execute(){
		// apply the predicate groups to the criteriaQuery
		int groups = predicateGroups.getPredicateGroupsSize();
		if (groups > 0) {
			if (groups == 2
				&& (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted)) {
				andJoinGroups();
				groups = predicateGroups.getPredicateGroupsSize();
			}
			
			if (groups == 1) {
				criteriaQuery =
					criteriaQuery.where(predicateGroups.getCurrentPredicateGroup().getPredicate());
			} else {
				throw new IllegalStateException("Query has open groups [" + groups + "]");
			}
			
			criteriaQuery.orderBy(orderByList);
		}
		TypedQuery<?> query = entityManager.createQuery(criteriaQuery);
		// update cache with results (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			query.setHint(QueryHints.REFRESH, HintValues.TRUE);
		}

		if (ignoreCaching || selections != null) {
			if (query instanceof JpaQuery<?>) {
				JpaQuery<?> jpaQuery = (JpaQuery<?>) query;
				if (jpaQuery.getDatabaseQuery().shouldMaintainCache()) {
					jpaQuery.getDatabaseQuery().dontMaintainCache();
				}
			}
		}

		List<T> ret = (List<T>) query.getResultStream().parallel()
			.map(e -> adapterFactory.getModelAdapter((EntityWithId) e, clazz, true).orElse(null))
			.filter(Objects::nonNull).collect(Collectors.toList());
		return ret;
	}
	
	@Override
	public Optional<T> executeSingleResult(){
		List<T> result = execute();
		if (!result.isEmpty()) {
			if (result.size() > 1) {
				LoggerFactory.getLogger(getClass())
					.warn("Multiple results in list where single result expected, using first element", new Throwable());
			}
			return Optional.of(result.get(0));
		}
		return Optional.empty();
	}
	
	private class SubQuery<S> implements ISubQuery<S> {
		
		private Subquery<? extends EntityWithId> subQuery;
		private Root<? extends EntityWithId> subRootQuery;
		
		private Class<? extends EntityWithId> entityClazz;
		
		private PredicateGroupStack predicateGroups;
		private PredicateHandler predicateHandler;
		
		public SubQuery(Subquery<? extends EntityWithId> subQuery,
			Class<? extends EntityWithId> entityClazz){
			this.subQuery = subQuery;
			this.entityClazz = entityClazz;
			this.subRootQuery = subQuery.from(entityClazz);
			this.predicateGroups = new PredicateGroupStack(criteriaBuilder);
			this.predicateHandler =
				new PredicateHandler(predicateGroups, entityClazz, criteriaBuilder, subRootQuery);
			
			if (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
				and(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
			}
		}
		
		@Override
		public Object getQuery(){
			// apply the predicate groups to the subQuery
			int groups = predicateGroups.getPredicateGroupsSize();
			if (groups > 0) {
				if (groups == 2
					&& (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted)) {
					andJoinGroups();
					groups = predicateGroups.getPredicateGroupsSize();
				}
				
				if (groups == 1) {
					subQuery =
						subQuery.where(predicateGroups.getCurrentPredicateGroup().getPredicate());
				} else {
					throw new IllegalStateException("Query has open groups [" + groups + "]");
				}
			}
			return subQuery;
		}
		
		@Override
		public void startGroup(){
			predicateGroups.createPredicateGroup();
		}
		
		@Override
		public void andJoinGroups(){
			predicateGroups.andPredicateGroups();
		}
		
		@Override
		public void orJoinGroups(){
			predicateGroups.orPredicateGroups();
		}
		
		@Override
		public void and(EStructuralFeature feature, COMPARATOR comparator, Object value,
			boolean ignoreCase){
			predicateHandler.and(feature, comparator, value, ignoreCase);
		}
		
		@Override
		public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
			EStructuralFeature otherFeature){
			predicateHandler.andFeatureCompare(feature, comparator, otherFeature);
		}
		
		@Override
		public void and(String entityAttributeName, COMPARATOR comparator, Object value,
			boolean ignoreCase){
			predicateHandler.and(entityAttributeName, comparator, value, ignoreCase);
		}
		
		@Override
		public void or(EStructuralFeature feature, COMPARATOR comparator, Object value,
			boolean ignoreCase){
			predicateHandler.or(feature, comparator, value, ignoreCase);
		}
		
		@Override
		public void or(String entityAttributeName, COMPARATOR comparator, Object value,
			boolean ignoreCase){
			predicateHandler.or(entityAttributeName, comparator, value, ignoreCase);
		}
		
		@Override
		public void andParentCompare(String parentEntityAttributeName, COMPARATOR comparator,
			String entityAttributeName){
			predicateHandler.andCompare(AbstractModelQuery.this.rootQuery,
				AbstractModelQuery.this.entityClazz, parentEntityAttributeName, comparator,
				entityAttributeName);
		}
	}
}
