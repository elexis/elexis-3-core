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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IOrder
 * Entry</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getOrder <em>Order</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getStock <em>Stock</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getAmount <em>Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getArticle <em>Article</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getProvider <em>Provider</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getState <em>State</em>}</li>
 *   <li>{@link ch.elexis.core.model.IOrderEntry#getDelivered <em>Delivered</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IOrderEntry extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Order</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Order</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Order</em>' reference.
	 * @see #setOrder(IOrder)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Order()
	 * @model
	 * @generated
	 */
	IOrder getOrder();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getOrder <em>Order</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Order</em>' reference.
	 * @see #getOrder()
	 * @generated
	 */
	void setOrder(IOrder value);

	/**
	 * Returns the value of the '<em><b>Stock</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stock</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stock</em>' reference.
	 * @see #setStock(IStock)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Stock()
	 * @model
	 * @generated
	 */
	IStock getStock();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getStock <em>Stock</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stock</em>' reference.
	 * @see #getStock()
	 * @generated
	 */
	void setStock(IStock value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Amount()
	 * @model
	 * @generated
	 */
	int getAmount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getAmount <em>Amount</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Amount</em>' attribute.
	 * @see #getAmount()
	 * @generated
	 */
	void setAmount(int value);

	/**
	 * Returns the value of the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Article</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article</em>' reference.
	 * @see #setArticle(IArticle)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Article()
	 * @model
	 * @generated
	 */
	IArticle getArticle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getArticle <em>Article</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article</em>' reference.
	 * @see #getArticle()
	 * @generated
	 */
	void setArticle(IArticle value);

	/**
	 * Returns the value of the '<em><b>Provider</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Provider</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Provider</em>' reference.
	 * @see #setProvider(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Provider()
	 * @model
	 * @generated
	 */
	IContact getProvider();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getProvider <em>Provider</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Provider</em>' reference.
	 * @see #getProvider()
	 * @generated
	 */
	void setProvider(IContact value);

	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' attribute.
	 * @see #setState(OrderEntryState)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_State()
	 * @model dataType="ch.elexis.core.types.OrderEntryState"
	 * @generated
	 */
	OrderEntryState getState();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IOrderEntry#getState <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' attribute.
	 * @see #getState()
	 * @generated
	 */
	void setState(OrderEntryState value);

	/**
	 * Returns the value of the '<em><b>Delivered</b></em>' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @return the value of the '<em>Delivered</em>' attribute.
	 * @see #setDelivered(int)
	 * @see ch.elexis.core.model.ModelPackage#getIOrderEntry_Delivered()
	 * @model
	 * @generated
	 */
	int getDelivered();

	/**
	 * Sets the value of the '<em><b>Delivered</b></em>' attribute.
	 * 
	 * @param value the new delivered count
	 */

	void setDelivered(int value);

} // IOrderEntry
