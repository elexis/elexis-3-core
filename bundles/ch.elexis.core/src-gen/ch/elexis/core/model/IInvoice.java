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

import ch.elexis.core.model.InvoiceState.REJECTCODE;
import ch.rgw.tools.Money;
import java.time.LocalDate;
import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IInvoice</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IInvoice#getState <em>State</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getNumber <em>Number</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getMandator <em>Mandator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getCoverage <em>Coverage</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getEncounters <em>Encounters</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getBilled <em>Billed</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getDateFrom <em>Date From</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getDateTo <em>Date To</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getTotalAmount <em>Total Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getOpenAmount <em>Open Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getPayedAmount <em>Payed Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getDemandAmount <em>Demand Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getRemark <em>Remark</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getStateDate <em>State Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getPayments <em>Payments</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getTransactions <em>Transactions</em>}</li>
 *   <li>{@link ch.elexis.core.model.IInvoice#getAttachments <em>Attachments</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIInvoice()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IInvoice extends Identifiable, Deleteable, WithExtInfo {

	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' attribute.
	 * @see #setState(InvoiceState)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_State()
	 * @model dataType="ch.elexis.core.types.InvoiceState"
	 * @generated
	 */
	InvoiceState getState();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getState <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' attribute.
	 * @see #getState()
	 * @generated
	 */
	void setState(InvoiceState value);

	/**
	 * Returns the value of the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Number()
	 * @model changeable="false"
	 * @generated
	 */
	String getNumber();

	/**
	 * Returns the value of the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mandator</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mandator</em>' reference.
	 * @see #setMandator(IMandator)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Mandator()
	 * @model
	 * @generated
	 */
	IMandator getMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getMandator <em>Mandator</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mandator</em>' reference.
	 * @see #getMandator()
	 * @generated
	 */
	void setMandator(IMandator value);

	/**
	 * Returns the value of the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Coverage</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Coverage</em>' reference.
	 * @see #setCoverage(ICoverage)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Coverage()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='fall'"
	 * @generated
	 */
	ICoverage getCoverage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getCoverage <em>Coverage</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Coverage</em>' reference.
	 * @see #getCoverage()
	 * @generated
	 */
	void setCoverage(ICoverage value);

	/**
	 * Returns the value of the '<em><b>Encounters</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IEncounter}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Encounters</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Encounters</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Encounters()
	 * @model
	 * @generated
	 */
	List<IEncounter> getEncounters();

	/**
	 * Returns the value of the '<em><b>Billed</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IBilled}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billed</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billed</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Billed()
	 * @model
	 * @generated
	 */
	List<IBilled> getBilled();

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Date()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_DateFrom()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDateFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getDateFrom <em>Date From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date From</em>' attribute.
	 * @see #getDateFrom()
	 * @generated
	 */
	void setDateFrom(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Date To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date To</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date To</em>' attribute.
	 * @see #setDateTo(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_DateTo()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDateTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getDateTo <em>Date To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date To</em>' attribute.
	 * @see #getDateTo()
	 * @generated
	 */
	void setDateTo(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Total Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total Amount</em>' attribute.
	 * @see #setTotalAmount(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_TotalAmount()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getTotalAmount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getTotalAmount <em>Total Amount</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Amount</em>' attribute.
	 * @see #getTotalAmount()
	 * @generated
	 */
	void setTotalAmount(Money value);

	/**
	 * Returns the value of the '<em><b>Open Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Open Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Open Amount</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_OpenAmount()
	 * @model dataType="ch.elexis.core.types.Money" changeable="false"
	 * @generated
	 */
	Money getOpenAmount();

	/**
	 * Returns the value of the '<em><b>Payed Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Payed Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Payed Amount</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_PayedAmount()
	 * @model dataType="ch.elexis.core.types.Money" changeable="false"
	 * @generated
	 */
	Money getPayedAmount();

	/**
	 * Returns the value of the '<em><b>Demand Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Demand Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Demand Amount</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_DemandAmount()
	 * @model dataType="ch.elexis.core.types.Money" changeable="false"
	 * @generated
	 */
	Money getDemandAmount();

	/**
	 * Returns the value of the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Remark</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Remark</em>' attribute.
	 * @see #setRemark(String)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Remark()
	 * @model
	 * @generated
	 */
	String getRemark();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getRemark <em>Remark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Remark</em>' attribute.
	 * @see #getRemark()
	 * @generated
	 */
	void setRemark(String value);

	/**
	 * Returns the value of the '<em><b>Payments</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IPayment}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Payments</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Payments</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Payments()
	 * @model changeable="false"
	 * @generated
	 */
	List<IPayment> getPayments();

	/**
	 * Returns the value of the '<em><b>Transactions</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IAccountTransaction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transactions</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Transactions()
	 * @model changeable="false"
	 * @generated
	 */
	List<IAccountTransaction> getTransactions();

	/**
	 * Returns the value of the '<em><b>Attachments</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IDocument}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Attachments</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_Attachments()
	 * @model
	 * @generated
	 */
	List<IDocument> getAttachments();

	/**
	 * Returns the value of the '<em><b>State Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State Date</em>' attribute.
	 * @see #setStateDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIInvoice_StateDate()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getStateDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoice#getStateDate <em>State Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State Date</em>' attribute.
	 * @see #getStateDate()
	 * @generated
	 */
	void setStateDate(LocalDate value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addTrace(String name, String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model type="ch.elexis.core.types.List&lt;org.eclipse.emf.ecore.EString&gt;" many="false"
	 * @generated
	 */
	List<String> getTrace(String name);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model valueDataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	boolean adjustAmount(Money value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model rejectCodeDataType="ch.elexis.core.types.InvoiceRejectCode"
	 * @generated
	 */
	void reject(REJECTCODE rejectCode, String message);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model attachmentRequired="true"
	 * @generated
	 */
	void addAttachment(IDocument attachment);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model attachmentRequired="true"
	 * @generated
	 */
	void removeAttachment(IDocument attachment);
} // IInvoice
