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

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;
import ch.elexis.core.types.Gender;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IPerson</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IPerson#getDateOfBirth <em>Date Of Birth</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getGender <em>Gender</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getTitel <em>Titel</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getTitelSuffix <em>Titel Suffix</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getFirstName <em>First Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getLastName <em>Last Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getMaritalStatus <em>Marital Status</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getLegalGuardian <em>Legal Guardian</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPerson#getDateOfDeath <em>Date Of Death</em>}</li>
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
	 * If the meaning of the '<em>Date Of Birth</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date Of Birth</em>' attribute.
	 * @see #setDateOfBirth(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_DateOfBirth()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='dob'"
	 * @generated
	 */
	LocalDateTime getDateOfBirth();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getDateOfBirth <em>Date Of Birth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date Of Birth</em>' attribute.
	 * @see #getDateOfBirth()
	 * @generated
	 */
	void setDateOfBirth(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Gender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Gender</em>' attribute isn't clear,
	 * there really should be more of a description here...
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
	 * If the meaning of the '<em>Titel</em>' attribute isn't clear,
	 * there really should be more of a description here...
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Titel</em>' attribute.
	 * @see #getTitel()
	 * @generated
	 */
	void setTitel(String value);

	/**
	 * Returns the value of the '<em><b>Titel Suffix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Titel Suffix</em>' attribute isn't clear,
	 * there really should be more of a description here...
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Titel Suffix</em>' attribute.
	 * @see #getTitelSuffix()
	 * @generated
	 */
	void setTitelSuffix(String value);

	/**
	 * Returns the value of the '<em><b>First Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Name</em>' attribute.
	 * @see #setFirstName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_FirstName()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='description2'"
	 * @generated
	 */
	String getFirstName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getFirstName <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Name</em>' attribute.
	 * @see #getFirstName()
	 * @generated
	 */
	void setFirstName(String value);

	/**
	 * Returns the value of the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Name</em>' attribute.
	 * @see #setLastName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_LastName()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='description1'"
	 * @generated
	 */
	String getLastName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getLastName <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Name</em>' attribute.
	 * @see #getLastName()
	 * @generated
	 */
	void setLastName(String value);

	/**
	 * Returns the value of the '<em><b>Marital Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Marital Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Marital Status</em>' attribute.
	 * @see #setMaritalStatus(MaritalStatus)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_MaritalStatus()
	 * @model dataType="ch.elexis.core.types.MaritalStatus"
	 * @generated
	 */
	MaritalStatus getMaritalStatus();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getMaritalStatus <em>Marital Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Marital Status</em>' attribute.
	 * @see #getMaritalStatus()
	 * @generated
	 */
	void setMaritalStatus(MaritalStatus value);

	/**
	 * Returns the value of the '<em><b>Legal Guardian</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Legal Guardian</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Legal Guardian</em>' reference.
	 * @see #setLegalGuardian(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_LegalGuardian()
	 * @model
	 * @generated
	 */
	IContact getLegalGuardian();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getLegalGuardian <em>Legal Guardian</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Legal Guardian</em>' reference.
	 * @see #getLegalGuardian()
	 * @generated
	 */
	void setLegalGuardian(IContact value);

	/**
	 * Returns the value of the '<em><b>Date Of Death</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date Of Death</em>' attribute.
	 * @see #setDateOfDeath(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIPerson_DateOfDeath()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getDateOfDeath();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPerson#getDateOfDeath <em>Date Of Death</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date Of Death</em>' attribute.
	 * @see #getDateOfDeath()
	 * @generated
	 */
	void setDateOfDeath(LocalDateTime value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	int getAgeInYears();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model referenceDataType="ch.elexis.core.types.LocalDateTime" chronoUnitDataType="ch.elexis.core.types.ChronoUnit"
	 * @generated
	 */
	long getAgeAtIn(LocalDateTime reference, ChronoUnit chronoUnit);

} // IPerson
