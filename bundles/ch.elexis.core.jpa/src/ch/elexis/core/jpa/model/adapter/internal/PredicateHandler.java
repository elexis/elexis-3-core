package ch.elexis.core.jpa.model.adapter.internal;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery.COMPARATOR;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Handles creation of new {@link Predicate} instances using the provided
 * entityClazz. The created instances are added to the
 * {@link PredicateGroupStack}.
 *
 * @author thomas
 *
 */
public class PredicateHandler {

	protected CriteriaBuilder criteriaBuilder;
	protected Root<? extends EntityWithId> rootQuery;

	protected PredicateGroupStack predicateGroups;

	protected Class<? extends EntityWithId> entityClazz;

	public PredicateHandler(PredicateGroupStack predicateGroups, Class<? extends EntityWithId> entityClazz,
			CriteriaBuilder criteriaBuilder, Root<? extends EntityWithId> rootQuery) {
		this.predicateGroups = predicateGroups;
		this.entityClazz = entityClazz;
		this.criteriaBuilder = criteriaBuilder;
		this.rootQuery = rootQuery;
	}

	public String getAttributeName(EStructuralFeature feature, Class<? extends EntityWithId> entityClazz) {
		String ret = feature.getName();
		EAnnotation mappingAnnotation = feature.getEAnnotation(IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING);
		if (mappingAnnotation != null) {
			// test class specific first
			ret = mappingAnnotation.getDetails()
					.get(entityClazz.getSimpleName() + "#" + IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME); //$NON-NLS-1$
			if (ret == null) {
				// fallback to direct mapping
				ret = mappingAnnotation.getDetails().get(IModelService.EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME);
			}
		}
		return ret;
	}

	public void exists(Subquery<?> query) {
		predicateGroups.getCurrentPredicateGroup().and(criteriaBuilder.exists(query));
	}

	public void notExists(Subquery<?> query) {
		predicateGroups.getCurrentPredicateGroup().and(criteriaBuilder.exists(query).not());
	}

	public void and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		String entityAttributeName = getAttributeName(feature, entityClazz);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				predicateGroups.getCurrentPredicateGroup().and(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + feature + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator, EStructuralFeature otherFeature) {
		String entityAttributeName = getAttributeName(feature, entityClazz);
		String entityOtherAttributeName = getAttributeName(otherFeature, entityClazz);
		andCompare(rootQuery, entityClazz, entityOtherAttributeName, comparator, entityAttributeName);
	}

	@SuppressWarnings("rawtypes")
	public void andCompare(Root<? extends EntityWithId> otherRoot, Class<? extends EntityWithId> otherEntityClazz,
			String otherEntityAttributeName, COMPARATOR comparator, String entityAttributeName) {

		Optional<SingularAttribute> attribute = resolveAttribute(entityClazz.getName(), entityAttributeName);
		Optional<SingularAttribute> otherAttribute = resolveAttribute(otherEntityClazz.getName(),
				otherEntityAttributeName);
		if (attribute.isPresent() && otherAttribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, otherAttribute.get(), otherRoot,
					false);
			predicate.ifPresent(p -> predicateGroups.getCurrentPredicateGroup().and(p));
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException("Could not resolve attribute [" + entityAttributeName + "] of entity [" //$NON-NLS-1$ //$NON-NLS-2$
					+ entityClazz + "] or [" + otherEntityAttributeName + "] of entity [" + otherEntityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				predicateGroups.getCurrentPredicateGroup().and(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + entityAttributeName + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		String entityAttributeName = getAttributeName(feature, entityClazz);
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> {
				predicateGroups.getCurrentPredicateGroup().or(p);
			});
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + feature + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		@SuppressWarnings("rawtypes")
		Optional<SingularAttribute> attribute = resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, value, ignoreCase);
			predicate.ifPresent(p -> predicateGroups.getCurrentPredicateGroup().or(p));
		} else {
			// feature could not be resolved, mapping?
			throw new IllegalStateException(
					"Could not resolve attribute [" + entityAttributeName + "] of entity [" + entityClazz + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	@SuppressWarnings("rawtypes")
	protected Optional<Predicate> getPredicate(SingularAttribute attribute, COMPARATOR comparator, Object value,
			boolean ignoreCase) {
		return getPredicate(attribute, comparator, value, null, ignoreCase);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Optional<Predicate> getPredicate(SingularAttribute attribute, COMPARATOR comparator, Object value,
			Root<? extends EntityWithId> valueRoot, boolean ignoreCase) {
		switch (comparator) {
		case EQUALS:
			if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.equal(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				return Optional.of(criteriaBuilder.equal(rootQuery.get(attribute), value));
			}
		case NOT_EQUALS:
			if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.notEqual(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				return Optional.of(criteriaBuilder.notEqual(rootQuery.get(attribute), value));
			}
		case LIKE:
			if (value instanceof String) {
				if (ignoreCase) {
					return Optional.of(criteriaBuilder.like(criteriaBuilder.lower(rootQuery.get(attribute)),
							((String) value).toLowerCase()));
				} else {
					return Optional.of(criteriaBuilder.like(rootQuery.get(attribute), (String) value));
				}
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case NOT_LIKE:
			if (value instanceof String) {
				if (ignoreCase) {
					return Optional.of(criteriaBuilder.notLike(criteriaBuilder.lower(rootQuery.get(attribute)),
							((String) value).toLowerCase()));
				} else {
					return Optional.of(criteriaBuilder.notLike(rootQuery.get(attribute), (String) value));
				}
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case GREATER:
			if (value instanceof String) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof Long) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (Long) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute), (LocalDate) value));
			} else if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.greaterThan(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case GREATER_OR_EQUAL:
			if (value instanceof String) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof Long) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (Long) value));
			} else if (value instanceof LocalDateTime) {
				return Optional
						.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute), (LocalDate) value));
			} else if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.greaterThanOrEqualTo(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case LESS:
			if (value instanceof String) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof Long) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (Long) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute), (LocalDate) value));
			} else if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.lessThan(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case LESS_OR_EQUAL:
			if (value instanceof String) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (String) value));
			} else if (value instanceof Integer) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (Integer) value));
			} else if (value instanceof Long) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (Long) value));
			} else if (value instanceof LocalDateTime) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (LocalDateTime) value));
			} else if (value instanceof LocalDate) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute), (LocalDate) value));
			} else if (value instanceof SingularAttribute) {
				return Optional.of(criteriaBuilder.lessThanOrEqualTo(rootQuery.get(attribute),
						valueRoot != null ? valueRoot.get((SingularAttribute) value)
								: rootQuery.get((SingularAttribute) value)));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		case IN:
			if (value instanceof Iterable<?>) {
				Path expr = rootQuery.get(attribute);
				Iterable<?> values = (Iterable<?>) value;
				In<Object> in = criteriaBuilder.in(expr);
				values.forEach(p -> in.value(p));
				return Optional.of(in);
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		default:
			break;
		}
		return Optional.empty();
	}

	@SuppressWarnings("rawtypes")
	public Optional<SingularAttribute> resolveAttribute(String entityClazzName, String featureName) {
		try {
			Class<?> metaClazz = getClass().getClassLoader().loadClass(entityClazzName + "_"); //$NON-NLS-1$
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
					.error("Could not find metamodel class for entity [" + entityClazzName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return Optional.empty();
	}

	protected Object resolveValue(Object value) {
		Object ret = value;
		if (value instanceof AbstractIdModelAdapter) {
			ret = ((AbstractIdModelAdapter<?>) value).getEntity();
		}
		return ret;
	}
}
