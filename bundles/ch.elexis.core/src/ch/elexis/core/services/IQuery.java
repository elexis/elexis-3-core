package ch.elexis.core.services;

import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

public interface IQuery<T> {
	public static enum COMPARATOR {
			LIKE, EQUALS, LESS, LESS_OR_EQUAL, GREATER, NOT_LIKE, NOT_EQUALS, GREATER_OR_EQUAL
	}
	
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
		and(entityAttributeName, comparator, value, false);
	}
	
	public void or(String entityAttributeName, COMPARATOR comparator, Object value,
		boolean ignoreCase);
	
	/**
	 * Execute the query and return a list with the resulting objects.
	 * 
	 * @return
	 */
	public List<T> execute();
}
