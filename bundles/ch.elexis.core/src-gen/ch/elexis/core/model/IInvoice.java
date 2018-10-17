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
 * A representation of the model object '<em><b>IInvoice</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IInvoice#getState <em>State</em>}</li>
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
} // IInvoice
