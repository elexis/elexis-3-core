package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.AbstractDBObjectId;
import ch.elexis.core.services.IQuery.COMPARATOR;

public abstract class AbstractModelQuery<T> {
	
	protected Class<T> clazz;
	
	protected EntityManager entityManager;
	protected CriteriaBuilder criteriaBuilder;
	
	protected CriteriaQuery<?> criteriaQuery;
	protected Root<? extends AbstractDBObjectId> rootQuery;
	
	protected AbstractModelAdapterFactory adapterFactory;
	
	protected Class<? extends AbstractDBObjectId> entityClazz;
	
	protected boolean includeDeleted;
	
	public AbstractModelQuery(Class<T> clazz, EntityManager entityManager, boolean includeDeleted){
		this.clazz = clazz;
		this.entityManager = entityManager;
		this.criteriaBuilder = entityManager.getCriteriaBuilder();
		this.includeDeleted = includeDeleted;
		
		initialize();
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
		Object value){
		switch (comparator) {
		case EQUALS:
			return Optional.of(criteriaBuilder.equal(rootQuery.get(attribute), value));
		case NOT_EQUALS:
			return Optional.of(criteriaBuilder.notEqual(rootQuery.get(attribute), value));
		case LIKE:
			if (value instanceof String) {
				return Optional.of(criteriaBuilder.like(rootQuery.get(attribute), (String) value));
			} else {
				throw new IllegalStateException("[" + value + "] is not a known type");
			}
		case NOT_LIKE:
			if (value instanceof String) {
				return Optional
					.of(criteriaBuilder.notLike(rootQuery.get(attribute), (String) value));
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
}
