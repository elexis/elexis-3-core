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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IInvoice Billed</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IInvoiceBilled#getInvoice <em>Invoice</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBilled()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IInvoiceBilled extends IBilled {
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
	 * @see ch.elexis.core.model.ModelPackage#getIInvoiceBilled_Invoice()
	 * @model
	 * @generated
	 */
	IInvoice getInvoice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IInvoiceBilled#getInvoice <em>Invoice</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Invoice</em>' reference.
	 * @see #getInvoice()
	 * @generated
	 */
	void setInvoice(IInvoice value);

} // IInvoiceBilled
