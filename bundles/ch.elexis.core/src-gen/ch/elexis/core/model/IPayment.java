/**
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import ch.rgw.tools.Money;
import java.time.LocalDate;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IPayment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IPayment#getInvoice <em>Invoice</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPayment#getAmount <em>Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPayment#getRemark <em>Remark</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPayment#getDate <em>Date</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIPayment()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPayment extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Invoice</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Invoice</em>' reference.
	 * @see #setInvoice(IInvoice)
	 * @see ch.elexis.core.model.ModelPackage#getIPayment_Invoice()
	 * @model
	 * @generated
	 */
	IInvoice getInvoice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPayment#getInvoice <em>Invoice</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Invoice</em>' reference.
	 * @see #getInvoice()
	 * @generated
	 */
	void setInvoice(IInvoice value);

	/**
	 * Returns the value of the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Amount</em>' attribute.
	 * @see #setAmount(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIPayment_Amount()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getAmount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPayment#getAmount <em>Amount</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Amount</em>' attribute.
	 * @see #getAmount()
	 * @generated
	 */
	void setAmount(Money value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIPayment_Remark()
	 * @model
	 * @generated
	 */
	String getRemark();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPayment#getRemark <em>Remark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Remark</em>' attribute.
	 * @see #getRemark()
	 * @generated
	 */
	void setRemark(String value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIPayment_Date()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPayment#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

} // IPayment
