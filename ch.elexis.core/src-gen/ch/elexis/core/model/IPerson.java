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

import ch.elexis.core.types.ContactGender;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IPerson</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IPerson#getDateOfBirth <em>Date Of Birth</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getGender <em>Gender</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getTitel <em>Titel</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getTitelSuffix <em>Titel Suffix</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIPerson()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPerson extends IContact {
	/**
	 * Returns the value of the '<em><b>Date Of Birth</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date Of Birth</em>' attribute isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date Of Birth</em>' attribute.
	 * @see #setDateOfBirth(TimeTool)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_DateOfBirth()
	 * @model dataType="ch.elexis.core.types.TimeTool"
	 * @generated
	 */
	TimeTool getDateOfBirth();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getDateOfBirth <em>Date Of Birth</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date Of Birth</em>' attribute.
	 * @see #getDateOfBirth()
	 * @generated
	 */
	void setDateOfBirth(TimeTool value);
	
	/**
	 * Returns the value of the '<em><b>Gender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Gender</em>' attribute isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Gender</em>' attribute.
	 * @see #setGender(Gender)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_Gender()
	 * @model dataType="ch.elexis.core.types.Gender"
	 * @generated
	 */
	Gender getGender();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getGender <em>Gender</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Gender</em>' attribute.
	 * @see #getGender()
	 * @generated
	 */
	void setGender(Gender value);

	/**
	 * Returns the value of the '<em><b>Titel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Titel</em>' attribute isn't clear, there really should be more of
	 * a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Titel</em>' attribute.
	 * @see #setTitel(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_Titel()
	 * @model
	 * @generated
	 */
	String getTitel();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getTitel <em>Titel</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Titel</em>' attribute.
	 * @see #getTitel()
	 * @generated
	 */
	void setTitel(String value);
	
	/**
	 * Returns the value of the '<em><b>Titel Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Titel Suffix</em>' attribute isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Titel Suffix</em>' attribute.
	 * @see #setTitelSuffix(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_TitelSuffix()
	 * @model
	 * @generated
	 */
	String getTitelSuffix();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getTitelSuffix <em>Titel Suffix</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Titel Suffix</em>' attribute.
	 * @see #getTitelSuffix()
	 * @generated
	 */
	void setTitelSuffix(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" required="true"
	 * @generated
	 */
	String getFirstName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" required="true"
	 * @generated
	 */
	String getFamilyName();
	
} // IPerson
