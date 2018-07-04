package ch.elexis.core.model.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EStructuralFeature;

import ch.elexis.core.jpa.entities.AbstractDBObjectId;
import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.jpa.model.adapter.AbstractModelQuery;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;

public class CoreQuery<T> extends AbstractModelQuery<T> implements IQuery<T> {
	
	public CoreQuery(Class<T> clazz, EntityManager entityManager){
		this(clazz, entityManager, false);
	}
	
	public CoreQuery(Class<T> clazz, EntityManager entityManager, boolean includeDeleted){
		super(clazz, entityManager, includeDeleted);
	}
	
	@Override
	protected void initialize(){
		adapterFactory = CoreModelAdapterFactory.getInstance();
		
		entityClazz = adapterFactory.getEntityClass(clazz);
		
		criteriaQuery = criteriaBuilder.createQuery(entityClazz);
		rootQuery = criteriaQuery.from(entityClazz);
		
		if (AbstractDBObjectIdDeleted.class.isAssignableFrom(entityClazz) && !includeDeleted) {
			add(ModelPackage.Literals.DELETEABLE__DELETED, COMPARATOR.NOT_EQUALS, true);
		}
	}
	
	@Override
	public void add(EStructuralFeature feature, COMPARATOR comparator, Object value){
		String entityAttributeName = getAttributeName(feature);
		Optional<SingularAttribute> attribute =
			resolveAttribute(entityClazz.getName(), entityAttributeName);
		value = resolveValue(value);
		if (attribute.isPresent()) {
			Optional<Predicate> predicate = getPredicate(attribute.get(), comparator, value);
			predicate.ifPresent(p -> {
				if (criteriaQuery.getRestriction() == null) {
					criteriaQuery.where(criteriaBuilder.and(p));
				} else {
					criteriaQuery.where(criteriaBuilder.and(criteriaQuery.getRestriction(), p));
				}
			});
		} else {
			// feature could not be resoved, mapping?
			throw new IllegalStateException(
				"Could not resolve attribute [" + feature + "] of entity [" + entityClazz + "]");
		}
	}
	
	private String getAttributeName(EStructuralFeature feature){
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
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> execute(){
		try {
			TypedQuery<?> query = (TypedQuery<?>) entityManager.createQuery(criteriaQuery);
			List<T> ret = (List<T>) query
				.getResultStream().parallel().map(e -> adapterFactory
					.getModelAdapter((AbstractDBObjectId) e, clazz, true).orElse(null))
				.filter(o -> o != null).collect(Collectors.toList());
			return ret;
		} finally {
			entityManager.close();
		}
	}
}
