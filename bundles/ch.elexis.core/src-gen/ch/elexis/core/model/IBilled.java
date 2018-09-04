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
 * A representation of the model object '<em><b>IBilled</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IBilled#getBillable <em>Billable</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getAmount <em>Amount</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIBilled()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBilled extends Identifiable, Deleteable {

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
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Billable()
	 * @model
	 * @generated
	 */
	IBillable getBillable();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getBillable <em>Billable</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billable</em>' reference.
	 * @see #getBillable()
	 * @generated
	 */
	void setBillable(IBillable value);

	/**
	 * Returns the value of the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Amount</em>' attribute.
	 * @see #setAmount(int)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Amount()
	 * @model
	 * @generated
	 */
	int getAmount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getAmount <em>Amount</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Amount</em>' attribute.
	 * @see #getAmount()
	 * @generated
	 */
	void setAmount(int value);
} // IBilled
