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
 *   <li>{@link ch.elexis.core.model.ILabItem#getGroup <em>Group</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getPrio <em>Prio</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getEinheit <em>Einheit</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getKuerzel <em>Kuerzel</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#getDigits <em>Digits</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabItem#isVisible <em>Visible</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabItem()
 * @model interface="true" abstract="true"
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

	/**
	 * Returns the value of the '<em><b>Group</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group</em>' attribute.
	 * @see #setGroup(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Group()
	 * @model
	 * @generated
	 */
	String getGroup();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getGroup <em>Group</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group</em>' attribute.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(String value);

	/**
	 * Returns the value of the '<em><b>Prio</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Prio</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prio</em>' attribute.
	 * @see #setPrio(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Prio()
	 * @model
	 * @generated
	 */
	String getPrio();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getPrio <em>Prio</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prio</em>' attribute.
	 * @see #getPrio()
	 * @generated
	 */
	void setPrio(String value);

	/**
	 * Returns the value of the '<em><b>Einheit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Einheit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Einheit</em>' attribute.
	 * @see #setEinheit(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Einheit()
	 * @model
	 * @generated
	 */
	String getEinheit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getEinheit <em>Einheit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Einheit</em>' attribute.
	 * @see #getEinheit()
	 * @generated
	 */
	void setEinheit(String value);

	/**
	 * Returns the value of the '<em><b>Kuerzel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Kuerzel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Kuerzel</em>' attribute.
	 * @see #setKuerzel(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Kuerzel()
	 * @model
	 * @generated
	 */
	String getKuerzel();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getKuerzel <em>Kuerzel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kuerzel</em>' attribute.
	 * @see #getKuerzel()
	 * @generated
	 */
	void setKuerzel(String value);

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
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Digits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Digits</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Digits</em>' attribute.
	 * @see #setDigits(int)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Digits()
	 * @model
	 * @generated
	 */
	int getDigits();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getDigits <em>Digits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Digits</em>' attribute.
	 * @see #getDigits()
	 * @generated
	 */
	void setDigits(int value);

	/**
	 * Returns the value of the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visible</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visible</em>' attribute.
	 * @see #setVisible(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Visible()
	 * @model
	 * @generated
	 */
	boolean isVisible();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#isVisible <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visible</em>' attribute.
	 * @see #isVisible()
	 * @generated
	 */
	void setVisible(boolean value);
} // ILabItem
