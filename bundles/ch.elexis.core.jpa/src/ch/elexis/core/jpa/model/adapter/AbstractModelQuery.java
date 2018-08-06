package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;

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
	
	protected Stack<PredicateGroup> predicateGroups;
	
	protected CriteriaQuery<?> criteriaQuery;
	protected Root<? extends EntityWithId> rootQuery;
	
	protected AbstractModelAdapterFactory adapterFactory;
	
	protected Class<? extends EntityWithId> entityClazz;
	
	protected boolean includeDeleted;
	protected boolean refreshCache;
	
	public AbstractModelQuery(Class<T> clazz, boolean refreshCache, EntityManager entityManager,
		boolean includeDeleted){
		this.clazz = clazz;
		this.entityManager = entityManager;
		this.criteriaBuilder = entityManager.getCriteriaBuilder();
		this.includeDeleted = includeDeleted;
		this.refreshCache = refreshCache;
		this.predicateGroups = new Stack<>();
		
		initialize();
	}
	
	/**
	 * Get the current (top) {@link PredicateGroup} from the stack, or create a new
	 * {@link PredicateGroup} and put it on top of the stack.
	 * 
	 * @return
	 */
	protected PredicateGroup getCurrentPredicateGroup(){
		PredicateGroup ret = null;
		if (predicateGroups.isEmpty()) {
			ret = createPredicateGroup();
		} else {
			return predicateGroups.peek();
		}
		return ret;
	}
	
	/**
	 * Create a new {@link PredicateGroup} on the stack.
	 * 
	 * @return
	 */
	protected PredicateGroup createPredicateGroup(){
		PredicateGroup ret = new PredicateGroup(criteriaBuilder);
		predicateGroups.push(ret);
		return ret;
	}
	
	/**
	 * Join the 2 last created {@link PredicateGroup}s with and, and return the resulting
	 * {@link PredicateGroup}. It is also the new top of the stack.
	 * 
	 * @return
	 */
	protected PredicateGroup andPredicateGroups(){
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(new PredicateGroup(criteriaBuilder,
				criteriaBuilder.and(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for and operation");
		}
	}
	
	/**
	 * Join the 2 last created {@link PredicateGroup}s with or, and return the resulting
	 * {@link PredicateGroup}. It is also the new top of the stack.
	 * 
	 * @return
	 */
	protected PredicateGroup orPredicateGroups(){
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(new PredicateGroup(criteriaBuilder,
				criteriaBuilder.or(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for or operation");
		}
	}
	
	/**
	 * Get the current size of the {@link PredicateGroup} stack.
	 * 
	 * @return
	 */
	protected int getPredicateGroupsSize(){
		return predicateGroups.size();
	}
	
	/**
	 * Initialize the {@link AbstractModelAdapterFactory} and dependent fields entityClazz,
	 * criteriaQuery and rootQuery.
	 * 
	 */
	protected abstract void initialize();
	
	@SuppressWarnings({
		"unchecked", "rawtypes"
	})
	protected Optional<Predicate> getPredicate(SingularAttribute attribute, COMPARATOR comparator,
		Object value, boolean ignoreCase){
		switch (comparator) {
		case EQUALS:
			return Optional.of(criteriaBuilder.equal(rootQuery.get(attribute), value));
		case NOT_EQUALS:
			return Optional.of(criteriaBuilder.notEqual(rootQuery.get(attribute), value));
		case LIKE:
			if (value instanceof String) {
				if (ignoreCase) {
					return Optional
						.of(criteriaBuilder.like(criteriaBuilder.lower(rootQuery.get(attribute)),
							((String) value).toLowerCase()));
				} else {
					return Optional
						.of(criteriaBuilder.like(rootQuery.get(attribute), (String) value));
				}
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case NOT_LIKE:
			if (value instanceof String) {
				if (ignoreCase) {
					return Optional
						.of(criteriaBuilder.notLike(criteriaBuilder.lower(rootQuery.get(attribute)),
							((String) value).toLowerCase()));
				} else {
					return Optional
						.of(criteriaBuilder.notLike(rootQuery.get(attribute), (String) value));
				}
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case GREATER:
			if (value instanceof String) {
				return Optional
					.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional
					.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(
					criteriaBuilder.greaterThan(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional
					.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (LocalDate) value));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case GREATER_OR_EQUAL:
			if (value instanceof String) {
				return Optional.of(
					criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute),
					(Integer) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute),
					(LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute),
					(LocalDate) value));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case LESS:
			if (value instanceof String) {
				return Optional
					.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional
					.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof LocalDateTime) {
				return Optional
					.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional
					.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (LocalDate) value));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case LESS_OR_EQUAL:
			if (value instanceof String) {
				return Optional.of(
					criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(
					criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(
					criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute),
						(LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(
					criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (LocalDate) value));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		default:
			break;
		}
		return Optional.empty();
	}
	
	@SuppressWarnings("rawtypes")
	protected Optional<SingularAttribute> resolveAttribute(String entityClazzName,
		String featureName){
		try {
			Class<?> metaClazz = getClass().getClassLoader().loadClass(entityClazzName + "_");
			Field[] fields = metaClazz.getFields();
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(featureName)) {
					Object value = field.get(null);
					if (value instanceof SingularAttribute) {
						return Optional.of((SingularAttribute) value);
					}
				}
			}
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			LoggerFactory.getLogger(getClass())
				.error("Could not find metamodel class for entity [" + entityClazzName + "]");
		}
		return Optional.empty();
	}
	
	protected Object resolveValue(Object value){
		Object ret = value;
		if (value instanceof AbstractIdModelAdapter) {
			ret = ((AbstractIdModelAdapter<?>) value).getEntity();
		}
		return ret;
	}
	
	@Override
	public void and(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		String entityAttributeName = getAttributeName(feature);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate =
				getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				getCurrentPredicateGroup().and(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
				"Could not resolve attribute [" + feature + "] of entity [" + entityClazz + "]");
		}
	}
	
	@Override
	public void and(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate =
				getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				getCurrentPredicateGroup().and(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException("Could not resolve attribute [" + entityAttributeName
				+ "] of entity [" + entityClazz + "]");
		}
	}
	
	@Override
	public void or(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		String entityAttributeName = getAttributeName(feature);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate =
				getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				getCurrentPredicateGroup().or(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
				"Could not resolve attribute [" + feature + "] of entity [" + entityClazz + "]");
		}
	}
	
	@Override
	public void or(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase){
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute =
			resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate =
				getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				getCurrentPredicateGroup().or(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException("Could not resolve attribute [" + entityAttributeName
				+ "] of entity [" + entityClazz + "]");
		}
	}
	
	@Override
	public void startGroup(){
		createPredicateGroup();
	}
	
	@Override
	public void andJoinGroups(){
		andPredicateGroups();
	}
	
	@Override
	public void orJoinGroups(){
		orPredicateGroups();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> execute(){
		// apply the predicate groups to the criteriaQuery
		int groups = getPredicateGroupsSize();
		if (groups > 0) {
			if (groups == 2
				&& (EntityWithDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted)) {
				andJoinGroups();
				groups = getPredicateGroupsSize();
			}
			
			if (groups == 1) {
				criteriaQuery = criteriaQuery.where(getCurrentPredicateGroup().getPredicate());
			} else {
				throw new IllegalStateException("Query has open groups [" + groups + "]");
			}
		}
		TypedQuery<?> query = (TypedQuery<?>) entityManager.createQuery(criteriaQuery);
		// update cache with results (https://wiki.eclipse.org/EclipseLink/UserGuide/JPA/Basic_JPA_Development/Querying/Query_Hints)
		if (refreshCache) {
			query.setHint(QueryHints.REFRESH, HintValues.TRUE);
		}
		
		List<T> ret = (List<T>) query.getResultStream().parallel()
			.map(e -> adapterFactory.getModelAdapter((EntityWithId) e, clazz, true).orElse(null))
			.filter(o -> o != null).collect(Collectors.toList());
		return ret;
	}
	
	protected String getAttributeName(EStructuralFeature feature){
		String ret = feature.getName();
		EAnnotation mappingAnnotation =
			feature.getEAnnotation(IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING);
		if (mappingAnnotation != null) {
			// test class specific first
			ret = mappingAnnotation.getDetails().get(entityClazz.getSimpleName() + "#"
				+ IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME);
			if (ret == null) {
				// fallback to direct mapping
				ret = mappingAnnotation.getDetails()
					.get(IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME);
			}
		}
		return ret;
	}
}
