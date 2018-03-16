/**
 */
package ch.elexis.core.ui.usage.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Event Statistic</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.ui.usage.model.EventStatistic#getMinDuration <em>Min Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.EventStatistic#getMaxDuration <em>Max Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.EventStatistic#getAvgDuration <em>Avg Duration</em>}</li>
 *   <li>{@link ch.elexis.core.ui.usage.model.EventStatistic#getLastStart <em>Last Start</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.ui.usage.model.ModelPackage#getEventStatistic()
 * @model
 * @generated
 */
public interface EventStatistic extends IStatistic {
	/**
	 * Returns the value of the '<em><b>Min Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Min Duration</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Min Duration</em>' attribute.
	 * @see #setMinDuration(int)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getEventStatistic_MinDuration()
	 * @model
	 * @generated
	 */
	int getMinDuration();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.EventStatistic#getMinDuration <em>Min Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min Duration</em>' attribute.
	 * @see #getMinDuration()
	 * @generated
	 */
	void setMinDuration(int value);

	/**
	 * Returns the value of the '<em><b>Max Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Max Duration</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Duration</em>' attribute.
	 * @see #setMaxDuration(int)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getEventStatistic_MaxDuration()
	 * @model
	 * @generated
	 */
	int getMaxDuration();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.EventStatistic#getMaxDuration <em>Max Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Duration</em>' attribute.
	 * @see #getMaxDuration()
	 * @generated
	 */
	void setMaxDuration(int value);

	/**
	 * Returns the value of the '<em><b>Avg Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Avg Duration</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Avg Duration</em>' attribute.
	 * @see #setAvgDuration(long)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getEventStatistic_AvgDuration()
	 * @model
	 * @generated
	 */
	long getAvgDuration();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.EventStatistic#getAvgDuration <em>Avg Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Avg Duration</em>' attribute.
	 * @see #getAvgDuration()
	 * @generated
	 */
	void setAvgDuration(long value);

	/**
	 * Returns the value of the '<em><b>Last Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last Start</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Start</em>' attribute.
	 * @see #setLastStart(long)
	 * @see ch.elexis.core.ui.usage.model.ModelPackage#getEventStatistic_LastStart()
	 * @model
	 * @generated
	 */
	long getLastStart();

	/**
	 * Sets the value of the '{@link ch.elexis.core.ui.usage.model.EventStatistic#getLastStart <em>Last Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Start</em>' attribute.
	 * @see #getLastStart()
	 * @generated
	 */
	void setLastStart(long value);

} // EventStatistic
