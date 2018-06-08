/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ILab Order</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ILabOrder#getLabResult <em>Lab Result</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabOrder#getLabItem <em>Lab Item</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabOrder#getPatientContact <em>Patient Contact</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabOrder()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ILabOrder extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Lab Result</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lab Result</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lab Result</em>' reference.
	 * @see #setLabResult(ILabResult)
	 * @see ch.elexis.core.model.ModelPackage#getILabOrder_LabResult()
	 * @model
	 * @generated
	 */
	ILabResult getLabResult();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabOrder#getLabResult <em>Lab Result</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lab Result</em>' reference.
	 * @see #getLabResult()
	 * @generated
	 */
	void setLabResult(ILabResult value);

	/**
	 * Returns the value of the '<em><b>Lab Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lab Item</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lab Item</em>' reference.
	 * @see #setLabItem(ILabItem)
	 * @see ch.elexis.core.model.ModelPackage#getILabOrder_LabItem()
	 * @model
	 * @generated
	 */
	ILabItem getLabItem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabOrder#getLabItem <em>Lab Item</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lab Item</em>' reference.
	 * @see #getLabItem()
	 * @generated
	 */
	void setLabItem(ILabItem value);

	/**
	 * Returns the value of the '<em><b>Patient Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient Contact</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient Contact</em>' reference.
	 * @see #setPatientContact(IPatient)
	 * @see ch.elexis.core.model.ModelPackage#getILabOrder_PatientContact()
	 * @model
	 * @generated
	 */
	IPatient getPatientContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabOrder#getPatientContact <em>Patient Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient Contact</em>' reference.
	 * @see #getPatientContact()
	 * @generated
	 */
	void setPatientContact(IPatient value);

} // ILabOrder
