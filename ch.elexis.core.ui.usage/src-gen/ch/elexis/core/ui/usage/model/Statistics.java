/**
 */
package ch.elexis.core.ui.usage.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Statistics</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.ui.usage.model.Statistics#getStatistics <em>Statistics</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics()
 * @model
 * @generated
 */
public interface Statistics extends EObject {
	/**
	 * Returns the value of the '<em><b>Statistics</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Statistics</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Statistics</em>' reference.
	 * @see #setStatistics(IStatistic)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics_Statistics()
	 * @model
	 * @generated
	 */
	IStatistic getStatistics();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.Statistics#getStatistics <em>Statistics</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Statistics</em>' reference.
	 * @see #getStatistics()
	 * @generated
	 */
	void setStatistics(IStatistic value);

} // Statistics
