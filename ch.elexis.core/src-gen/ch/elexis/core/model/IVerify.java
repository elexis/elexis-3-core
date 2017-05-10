/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import ch.elexis.core.types.VerifyType;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IVerify</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IVerify#getBillable <em>Billable</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerify#getCount <em>Count</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerify#getStatus <em>Status</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerify#getInfo <em>Info</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerify#getVerifyType <em>Verify Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerify#getValidatorId <em>Validator Id</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIVerify()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVerify {
	/**
	 * Returns the value of the '<em><b>Billable</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billable</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billable</em>' reference.
	 * @see #setBillable(IBillable)
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_Billable()
	 * @model
	 * @generated
	 */
	IBillable getBillable();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerify#getBillable <em>Billable</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billable</em>' reference.
	 * @see #getBillable()
	 * @generated
	 */
	void setBillable(IBillable value);

	/**
	 * Returns the value of the '<em><b>Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Count</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Count</em>' attribute.
	 * @see #setCount(double)
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_Count()
	 * @model
	 * @generated
	 */
	double getCount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerify#getCount <em>Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Count</em>' attribute.
	 * @see #getCount()
	 * @generated
	 */
	void setCount(double value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see #setStatus(IStatus)
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_Status()
	 * @model dataType="ch.elexis.core.types.Status"
	 * @generated
	 */
	IStatus getStatus();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerify#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(IStatus value);

	/**
	 * Returns the value of the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Info</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Info</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_Info()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	Map<String, String> getInfo();

	/**
	 * Returns the value of the '<em><b>Verify Type</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.types.VerifyType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Verify Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Verify Type</em>' attribute.
	 * @see ch.elexis.core.types.VerifyType
	 * @see #setVerifyType(VerifyType)
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_VerifyType()
	 * @model
	 * @generated
	 */
	VerifyType getVerifyType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerify#getVerifyType <em>Verify Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Verify Type</em>' attribute.
	 * @see ch.elexis.core.types.VerifyType
	 * @see #getVerifyType()
	 * @generated
	 */
	void setVerifyType(VerifyType value);

	/**
	 * Returns the value of the '<em><b>Validator Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Validator Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Validator Id</em>' attribute.
	 * @see #setValidatorId(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVerify_ValidatorId()
	 * @model
	 * @generated
	 */
	String getValidatorId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerify#getValidatorId <em>Validator Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Validator Id</em>' attribute.
	 * @see #getValidatorId()
	 * @generated
	 */
	void setValidatorId(String value);

} // IVerify
