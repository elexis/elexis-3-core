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

import java.time.LocalDate;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ICoverage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ICoverage#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.ICoverage#getDescription <em>Description</em>}</li>
 *   <li>{@link ch.elexis.core.model.ICoverage#getReason <em>Reason</em>}</li>
 *   <li>{@link ch.elexis.core.model.ICoverage#getDateFrom <em>Date From</em>}</li>
 *   <li>{@link ch.elexis.core.model.ICoverage#getBillingSystem <em>Billing System</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getICoverage()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ICoverage extends Deleteable, Identifiable, WithExtInfo {

	/**
	 * Returns the value of the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' reference.
	 * @see #setPatient(IPatient)
	 * @see ch.elexis.core.model.ModelPackage#getICoverage_Patient()
	 * @model required="true"
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ICoverage#getPatient <em>Patient</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' reference.
	 * @see #getPatient()
	 * @generated
	 */
	void setPatient(IPatient value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see ch.elexis.core.model.ModelPackage#getICoverage_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ICoverage#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reason</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reason</em>' attribute.
	 * @see #setReason(String)
	 * @see ch.elexis.core.model.ModelPackage#getICoverage_Reason()
	 * @model
	 * @generated
	 */
	String getReason();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ICoverage#getReason <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reason</em>' attribute.
	 * @see #getReason()
	 * @generated
	 */
	void setReason(String value);

	/**
	 * Returns the value of the '<em><b>Date From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date From</em>' attribute.
	 * @see #setDateFrom(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getICoverage_DateFrom()
	 * @model dataType="ch.elexis.core.types.LocalDate" required="true"
	 * @generated
	 */
	LocalDate getDateFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ICoverage#getDateFrom <em>Date From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date From</em>' attribute.
	 * @see #getDateFrom()
	 * @generated
	 */
	void setDateFrom(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Billing System</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billing System</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billing System</em>' attribute.
	 * @see #setBillingSystem(String)
	 * @see ch.elexis.core.model.ModelPackage#getICoverage_BillingSystem()
	 * @model required="true"
	 * @generated
	 */
	String getBillingSystem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ICoverage#getBillingSystem <em>Billing System</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billing System</em>' attribute.
	 * @see #getBillingSystem()
	 * @generated
	 */
	void setBillingSystem(String value);
} // ICoverage
