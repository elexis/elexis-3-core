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

import ch.elexis.core.types.LabItemTyp;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ILab Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ILabItem#getTyp <em>Typ</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getRefM <em>Ref M</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getRefW <em>Ref W</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabItem()
 * @model abstract="true"
 * @generated
 */
public interface ILabItem extends Identifiable {

	/**
	 * Returns the value of the '<em><b>Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Typ</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Typ</em>' attribute.
	 * @see #setTyp(LabItemTyp)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Typ()
	 * @model dataType="ch.elexis.core.types.LabItemTyp"
	 * @generated
	 */
	LabItemTyp getTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getTyp <em>Typ</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Typ</em>' attribute.
	 * @see #getTyp()
	 * @generated
	 */
	void setTyp(LabItemTyp value);

	/**
	 * Returns the value of the '<em><b>Ref M</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ref M</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ref M</em>' attribute.
	 * @see #setRefM(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_RefM()
	 * @model
	 * @generated
	 */
	String getRefM();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getRefM <em>Ref M</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ref M</em>' attribute.
	 * @see #getRefM()
	 * @generated
	 */
	void setRefM(String value);

	/**
	 * Returns the value of the '<em><b>Ref W</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ref W</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ref W</em>' attribute.
	 * @see #setRefW(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_RefW()
	 * @model
	 * @generated
	 */
	String getRefW();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getRefW <em>Ref W</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ref W</em>' attribute.
	 * @see #getRefW()
	 * @generated
	 */
	void setRefW(String value);
} // ILabItem
