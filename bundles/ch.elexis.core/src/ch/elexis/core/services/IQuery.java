package ch.elexis.core.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EStructuralFeature;

public interface IQuery<T> {
	public static enum COMPARATOR {
			LIKE, EQUALS, LESS, LESS_OR_EQUAL, GREATER, NOT_LIKE, NOT_EQUALS, GREATER_OR_EQUAL
	}
	
	public static enum ORDER {
			ASC, DESC
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
	
	/**
	 * Execute the query and return a single result. If more than one result
	 * is available, a warning is logged, and the first result is returned.
	 * 
	 * @return
	 */
	public Optional<T> executeSingleResult();
	
	/**
	 * Add an order by to the query.
	 * 
	 * @param fieldOrderBy
	 * @param order
	 */
	public void orderBy(String fieldOrderBy, ORDER order);
	
	/**
	 * Add an order by to the query.
	 * 
	 * @param fieldOrderBy
	 * @param order
	 */
	public void orderBy(EStructuralFeature feature, ORDER order);
	
	/**
	 * Add an order by results of CASE statement. The caseContext map containes a String case
	 * description and an Object case value. The Syntax for the description is as follows.</br>
	 * 
	 * <b>when</b></br>
	 * when|fieldname|predicate|value</br>
	 * fieldname, must be a valid field name of the selected type.</br>
	 * predicate, specify how the fieldname is compared to the value. Known values are equals and
	 * like.</br>
	 * value, the used to compare the field with. </br>
	 * example: "when|description2|equals|test3"</br>
	 * 
	 * <b>otherwise</b></br>
	 * Currently only otherwise without comparison is supported. </br>
	 * example: "otherwise"</br>
	 * 
	 * 
	 * @param fieldOrderBy
	 * @param order
	 */
	public void orderBy(Map<String, Object> caseContext, ORDER order);
}
