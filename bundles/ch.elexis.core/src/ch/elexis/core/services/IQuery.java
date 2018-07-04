package ch.elexis.core.services;

import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

public interface IQuery<T> {
	public static enum COMPARATOR {
			LIKE, EQUALS, LESS, LESS_OR_EQUAL, GREATER, NOT_LIKE, NOT_EQUALS, GREATER_OR_EQUAL
	}
	
	/**
	 * Add a where clause based on the value of the {@link EStructuralFeature} to the query. It will
	 * be connected by AND to existing clauses.
	 * 
	 * @param feature
	 * @param equals
	 * @param patientId
	 */
	public void add(EStructuralFeature feature, COMPARATOR comparator, Object value);
	
	/**
	 * Execute the query and return a list with the resulting objects.
	 * 
	 * @return
	 */
	public List<T> execute();
}
