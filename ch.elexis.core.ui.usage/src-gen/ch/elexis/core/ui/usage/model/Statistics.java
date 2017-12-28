/**
 */
package ch.elexis.core.ui.usage.model;

import java.util.Date;
import org.eclipse.emf.common.util.EList;
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
 *   <li>{@link ch.elexis.core.ui.usage.model.Statistics#getFrom <em>From</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.Statistics#getTo <em>To</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics()
 * @model
 * @generated
 */
public interface Statistics extends EObject {
	/**
	 * Returns the value of the '<em><b>Statistics</b></em>' containment reference list.
	 * The list contents are of type {@link ch.elexis.core.ui.usage.model.IStatistic}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Statistics</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Statistics</em>' containment reference list.
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics_Statistics()
	 * @model containment="true"
	 * @generated
	 */
	EList<IStatistic> getStatistics();

	/**
	 * Returns the value of the '<em><b>From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>From</em>' attribute.
	 * @see #setFrom(Date)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics_From()
	 * @model
	 * @generated
	 */
	Date getFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.Statistics#getFrom <em>From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>From</em>' attribute.
	 * @see #getFrom()
	 * @generated
	 */
	void setFrom(Date value);

	/**
	 * Returns the value of the '<em><b>To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>To</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>To</em>' attribute.
	 * @see #setTo(Date)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getStatistics_To()
	 * @model
	 * @generated
	 */
	Date getTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.Statistics#getTo <em>To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>To</em>' attribute.
	 * @see #getTo()
	 * @generated
	 */
	void setTo(Date value);

} // Statistics
