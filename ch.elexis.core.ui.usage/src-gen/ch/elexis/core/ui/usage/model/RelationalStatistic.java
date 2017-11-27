/**
 */
package ch.elexis.core.ui.usage.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Relational Statistic</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getFrom <em>From</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getTo <em>To</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.ui.usage.model.ModelPackage#getRelationalStatistic()
 * @model
 * @generated
 */
public interface RelationalStatistic extends IStatistic {
	/**
	 * Returns the value of the '<em><b>From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>From</em>' attribute.
	 * @see #setFrom(String)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getRelationalStatistic_From()
	 * @model
	 * @generated
	 */
	String getFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getFrom <em>From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>From</em>' attribute.
	 * @see #getFrom()
	 * @generated
	 */
	void setFrom(String value);

	/**
	 * Returns the value of the '<em><b>To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>To</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>To</em>' attribute.
	 * @see #setTo(String)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getRelationalStatistic_To()
	 * @model
	 * @generated
	 */
	String getTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getTo <em>To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>To</em>' attribute.
	 * @see #getTo()
	 * @generated
	 */
	void setTo(String value);

} // RelationalStatistic
