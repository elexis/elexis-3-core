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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>ILab Item</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link ch.elexis.core.model.ILabItem#getTyp <em>Typ</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getReferenceMale <em>Reference Male</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getReferenceFemale <em>Reference Female</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getGroup <em>Group</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getPriority <em>Priority</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getUnit <em>Unit</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getKuerzel <em>Kuerzel</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getName <em>Name</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#getDigits <em>Digits</em>}</li>
 * <li>{@link ch.elexis.core.model.ILabItem#isVisible <em>Visible</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabItem()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ILabItem extends Identifiable {
	
	/**
	 * Returns the value of the '<em><b>Typ</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Typ</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Typ</em>' attribute.
	 * @see #setTyp(LabItemTyp)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Typ()
	 * @model dataType="ch.elexis.core.types.LabItemTyp"
	 * @generated
	 */
	LabItemTyp getTyp();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getTyp <em>Typ</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Typ</em>' attribute.
	 * @see #getTyp()
	 * @generated
	 */
	void setTyp(LabItemTyp value);
	
	/**
	 * Returns the value of the '<em><b>Reference Male</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reference Male</em>' attribute isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Reference Male</em>' attribute.
	 * @see #setReferenceMale(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_ReferenceMale()
	 * @model
	 * @generated
	 */
	String getReferenceMale();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getReferenceMale <em>Reference
	 * Male</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Reference Male</em>' attribute.
	 * @see #getReferenceMale()
	 * @generated
	 */
	void setReferenceMale(String value);
	
	/**
	 * Returns the value of the '<em><b>Reference Female</b></em>' attribute. <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Reference Female</em>' attribute isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Reference Female</em>' attribute.
	 * @see #setReferenceFemale(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_ReferenceFemale()
	 * @model
	 * @generated
	 */
	String getReferenceFemale();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getReferenceFemale <em>Reference
	 * Female</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Reference Female</em>' attribute.
	 * @see #getReferenceFemale()
	 * @generated
	 */
	void setReferenceFemale(String value);
	
	/**
	 * Returns the value of the '<em><b>Group</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group</em>' attribute isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Group</em>' attribute.
	 * @see #setGroup(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Group()
	 * @model
	 * @generated
	 */
	String getGroup();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getGroup <em>Group</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Group</em>' attribute.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(String value);
	
	/**
	 * Returns the value of the '<em><b>Priority</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Priority</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Priority</em>' attribute.
	 * @see #setPriority(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Priority()
	 * @model
	 * @generated
	 */
	String getPriority();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getPriority <em>Priority</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Priority</em>' attribute.
	 * @see #getPriority()
	 * @generated
	 */
	void setPriority(String value);
	
	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unit</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Unit()
	 * @model
	 * @generated
	 */
	String getUnit();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getUnit <em>Unit</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(String value);
	
	/**
	 * Returns the value of the '<em><b>Kuerzel</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Kuerzel</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Kuerzel</em>' attribute.
	 * @see #setKuerzel(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Kuerzel()
	 * @model
	 * @generated
	 */
	String getKuerzel();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getKuerzel <em>Kuerzel</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Kuerzel</em>' attribute.
	 * @see #getKuerzel()
	 * @generated
	 */
	void setKuerzel(String value);
	
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Name()
	 * @model
	 * @generated
	 */
	String getName();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);
	
	/**
	 * Returns the value of the '<em><b>Digits</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Digits</em>' attribute isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Digits</em>' attribute.
	 * @see #setDigits(int)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Digits()
	 * @model
	 * @generated
	 */
	int getDigits();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#getDigits <em>Digits</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Digits</em>' attribute.
	 * @see #getDigits()
	 * @generated
	 */
	void setDigits(int value);
	
	/**
	 * Returns the value of the '<em><b>Visible</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visible</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Visible</em>' attribute.
	 * @see #setVisible(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getILabItem_Visible()
	 * @model
	 * @generated
	 */
	boolean isVisible();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabItem#isVisible <em>Visible</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Visible</em>' attribute.
	 * @see #isVisible()
	 * @generated
	 */
	void setVisible(boolean value);
	
	/**
	 * 
	 * @return <code>true</code> if this {@link ILabItem} is not evaluatable to pathologic, non-pathologic
	 *         and hence by definition is always non-pathologic
	 */
	boolean isNoReferenceValueItem();
} // ILabItem
