package ch.elexis.core.services;

import org.eclipse.emf.ecore.EStructuralFeature;

import ch.elexis.core.services.IQuery.COMPARATOR;

public interface ISubQuery<S> {
	
	/**
	 * Get the implementation specific subquery object.
	 * 
	 * @return
	 */
	public Object getQuery();
	
	/**
	 * Start a new group of where clauses that are joined with
	 * {@link #and(EStructuralFeature, COMPARATOR, Object)} or
	 * {@link #or(EStructuralFeature, COMPARATOR, Object)}. After at least 2 groups are started,
	 * {@link #andEndGroup()} or {@link #orEndGroup()} must be called to join the groups.
	 */
	public void startGroup();
	
	/**
	 * Join the current group to the previous group with and.
	 */
	public void andJoinGroups();
	
	/**
	 * Join the current group to the previous group with or.
	 */
	public void orJoinGroups();
	
	/**
	 * Add a where clause based on the value of the {@link EStructuralFeature} to the query. It will
	 * be connected by AND to existing clauses.
	 * 
	 * @param feature
	 * @param comparator
	 * @param value
	 */
	public default void and(EStructuralFeature feature, COMPARATOR comparator, Object value){
		and(feature, comparator, value, false);
	}
	
	public void and(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase);
	
	/**
	 * Add a where clause based in the two provided {@link EStructuralFeature} to the query. It will
	 * be connected by AND to existing clauses.
	 * 
	 * @param feature
	 * @param object
	 * @param otherFeature
	 */
	public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
		EStructuralFeature otherFeature);
	
	/**
	 * Add a where clause based on the value of the specified entityAttributeName to the query. It
	 * will be connected by AND to existing clauses.
	 * 
	 * @param entityAttributeName
	 * @param comparator
	 * @param value
	 */
	public default void and(String entityAttributeName, COMPARATOR comparator, Object value){
		and(entityAttributeName, comparator, value, false);
	}
	
	public void and(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase);
	
	/**
	 * Add a where clause based on the value of the {@link EStructuralFeature} to the query. It will
	 * be connected by OR to existing clauses.
	 * 
	 * @param feature
	 * @param equals
	 * @param patientId
	 */
	public default void or(EStructuralFeature feature, COMPARATOR comparator, Object value){
		or(feature, comparator, value, false);
	}
	
	public void or(EStructuralFeature feature, COMPARATOR comparator, Object value,
		boolean ignoreCase);
	
	/**
	 * Add a where clause based on the value of the specified entityAttributeName to the query. It
	 * will be connected by OR to existing clauses.
	 * 
	 * @param entityAttributeName
	 * @param comparator
	 * @param value
	 */
	public default void or(String entityAttributeName, COMPARATOR comparator, Object value){
		or(entityAttributeName, comparator, value, false);
	}
	
	public void or(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase);
	
	/**
	 * Compare the entity attribute of the parent query type to the attribute of the sub query type.
	 * 
	 * @param parentEntityAttributeName
	 * @param equals
	 * @param entityAttributeName
	 */
	public void andParentCompare(String parentEntityAttributeName, COMPARATOR equals,
		String entityAttributeName);
}
