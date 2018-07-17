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
 * A representation of the model object '<em><b>ILab Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ILabMapping#getItemName <em>Item Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabMapping#getItem <em>Item</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabMapping#getOrigin <em>Origin</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabMapping#isCharge <em>Charge</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabMapping()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ILabMapping extends Deleteable, Identifiable {

	/**
	 * Returns the value of the '<em><b>Item Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Item Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Item Name</em>' attribute.
	 * @see #setItemName(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabMapping_ItemName()
	 * @model
	 * @generated
	 */
	String getItemName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabMapping#getItemName <em>Item Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Item Name</em>' attribute.
	 * @see #getItemName()
	 * @generated
	 */
	void setItemName(String value);

	/**
	 * Returns the value of the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Item</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Item</em>' reference.
	 * @see #setItem(ILabItem)
	 * @see ch.elexis.core.model.ModelPackage#getILabMapping_Item()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='labitem'"
	 * @generated
	 */
	ILabItem getItem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabMapping#getItem <em>Item</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Item</em>' reference.
	 * @see #getItem()
	 * @generated
	 */
	void setItem(ILabItem value);

	/**
	 * Returns the value of the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Origin</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Origin</em>' reference.
	 * @see #setOrigin(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getILabMapping_Origin()
	 * @model
	 * @generated
	 */
	IContact getOrigin();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabMapping#getOrigin <em>Origin</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Origin</em>' reference.
	 * @see #getOrigin()
	 * @generated
	 */
	void setOrigin(IContact value);

	/**
	 * Returns the value of the '<em><b>Charge</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Charge</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Charge</em>' attribute.
	 * @see #setCharge(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getILabMapping_Charge()
	 * @model
	 * @generated
	 */
	boolean isCharge();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabMapping#isCharge <em>Charge</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Charge</em>' attribute.
	 * @see #isCharge()
	 * @generated
	 */
	void setCharge(boolean value);
} // ILabMapping
