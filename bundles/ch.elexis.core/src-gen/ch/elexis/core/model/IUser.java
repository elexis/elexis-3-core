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

import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IUser</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IUser#getUsername <em>Username</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getHashedPassword <em>Hashed Password</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getSalt <em>Salt</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getAssignedContact <em>Assigned Contact</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getRoles <em>Roles</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#isActive <em>Active</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#isAllowExternal <em>Allow External</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#isAdministrator <em>Administrator</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIUser()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IUser extends Deleteable, Identifiable {
	/**
	 * Returns the value of the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Username</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Username</em>' attribute.
	 * @see #setUsername(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Username()
	 * @model
	 * @generated
	 */
	String getUsername();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getUsername <em>Username</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Username</em>' attribute.
	 * @see #getUsername()
	 * @generated
	 */
	void setUsername(String value);

	/**
	 * Returns the value of the '<em><b>Hashed Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hashed Password</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Hashed Password</em>' attribute.
	 * @see #setHashedPassword(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_HashedPassword()
	 * @model
	 * @generated
	 */
	String getHashedPassword();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getHashedPassword <em>Hashed Password</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hashed Password</em>' attribute.
	 * @see #getHashedPassword()
	 * @generated
	 */
	void setHashedPassword(String value);

	/**
	 * Returns the value of the '<em><b>Salt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Salt</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Salt</em>' attribute.
	 * @see #setSalt(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Salt()
	 * @model
	 * @generated
	 */
	String getSalt();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getSalt <em>Salt</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Salt</em>' attribute.
	 * @see #getSalt()
	 * @generated
	 */
	void setSalt(String value);

	/**
	 * Returns the value of the '<em><b>Assigned Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Assigned Contact</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assigned Contact</em>' reference.
	 * @see #setAssignedContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_AssignedContact()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='kontakt'"
	 * @generated
	 */
	IContact getAssignedContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getAssignedContact <em>Assigned Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Assigned Contact</em>' reference.
	 * @see #getAssignedContact()
	 * @generated
	 */
	void setAssignedContact(IContact value);

	/**
	 * Returns the value of the '<em><b>Roles</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IRole}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Roles</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Roles</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Roles()
	 * @model
	 * @generated
	 */
	List<IRole> getRoles();

	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Active()
	 * @model required="true"
	 * @generated
	 */
	boolean isActive();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#isActive <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Allow External</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Allow External</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allow External</em>' attribute.
	 * @see #setAllowExternal(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_AllowExternal()
	 * @model
	 * @generated
	 */
	boolean isAllowExternal();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#isAllowExternal <em>Allow External</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow External</em>' attribute.
	 * @see #isAllowExternal()
	 * @generated
	 */
	void setAllowExternal(boolean value);

	/**
	 * Returns the value of the '<em><b>Administrator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Administrator</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Administrator</em>' attribute.
	 * @see #setAdministrator(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Administrator()
	 * @model
	 * @generated
	 */
	boolean isAdministrator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#isAdministrator <em>Administrator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Administrator</em>' attribute.
	 * @see #isAdministrator()
	 * @generated
	 */
	void setAdministrator(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model roleRequired="true"
	 * @generated
	 */
	IRole addRole(IRole role);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model roleRequired="true"
	 * @generated
	 */
	void removeRole(IRole role);

} // IUser
