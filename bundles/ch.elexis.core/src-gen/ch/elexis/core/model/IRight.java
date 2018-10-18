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
 * A representation of the model object '<em><b>IRight</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IRight#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRight#getLocalizedName <em>Localized Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRight#getParent <em>Parent</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIRight()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IRight extends Identifiable, Deleteable, WithAssignableId {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIRight_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRight#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Localized Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Localized Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Name</em>' attribute.
	 * @see #setLocalizedName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIRight_LocalizedName()
	 * @model
	 * @generated
	 */
	String getLocalizedName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRight#getLocalizedName <em>Localized Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Localized Name</em>' attribute.
	 * @see #getLocalizedName()
	 * @generated
	 */
	void setLocalizedName(String value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' reference.
	 * @see #setParent(IRight)
	 * @see ch.elexis.core.model.ModelPackage#getIRight_Parent()
	 * @model
	 * @generated
	 */
	IRight getParent();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRight#getParent <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(IRight value);

} // IRight
