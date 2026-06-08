/**
 * Copyright (c) 2024 MEDEVIT <office@medevit.at>.
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
 * A representation of the model object '<em><b>IInvoice Bill Record Info</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInvoice <em>Invoice</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBilled <em>Billed</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBillid <em>Billid</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBillrecordid <em>Billrecordid</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInfo <em>Info</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInfocode <em>Infocode</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IInvoiceBillRecordInfo extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Invoice</em>' reference.
	 * @see #setInvoice(IInvoice)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Invoice()
	 * @model
	 * @generated
	 */
	IInvoice getInvoice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInvoice <em>Invoice</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Invoice</em>' reference.
	 * @see #getInvoice()
	 * @generated
	 */
	void setInvoice(IInvoice value);

	/**
	 * Returns the value of the '<em><b>Billed</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billed</em>' reference.
	 * @see #setBilled(IBilled)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Billed()
	 * @model
	 * @generated
	 */
	IBilled getBilled();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBilled <em>Billed</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billed</em>' reference.
	 * @see #getBilled()
	 * @generated
	 */
	void setBilled(IBilled value);

	/**
	 * Returns the value of the '<em><b>Billid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billid</em>' attribute.
	 * @see #setBillid(String)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Billid()
	 * @model
	 * @generated
	 */
	String getBillid();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBillid <em>Billid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billid</em>' attribute.
	 * @see #getBillid()
	 * @generated
	 */
	void setBillid(String value);

	/**
	 * Returns the value of the '<em><b>Billrecordid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billrecordid</em>' attribute.
	 * @see #setBillrecordid(String)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Billrecordid()
	 * @model
	 * @generated
	 */
	String getBillrecordid();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getBillrecordid <em>Billrecordid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billrecordid</em>' attribute.
	 * @see #getBillrecordid()
	 * @generated
	 */
	void setBillrecordid(String value);

	/**
	 * Returns the value of the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Info</em>' attribute.
	 * @see #setInfo(String)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Info()
	 * @model
	 * @generated
	 */
	String getInfo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInfo <em>Info</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Info</em>' attribute.
	 * @see #getInfo()
	 * @generated
	 */
	void setInfo(String value);

	/**
	 * Returns the value of the '<em><b>Infocode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Infocode</em>' attribute.
	 * @see #setInfocode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBillRecordInfo_Infocode()
	 * @model
	 * @generated
	 */
	String getInfocode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBillRecordInfo#getInfocode <em>Infocode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Infocode</em>' attribute.
	 * @see #getInfocode()
	 * @generated
	 */
	void setInfocode(String value);

} // IInvoiceBillRecordInfo
