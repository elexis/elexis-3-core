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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IUser</b></em>'. <!--
 * end-user-doc -->
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
 *   <li>{@link ch.elexis.core.model.IUser#getAssociatedContactId <em>Associated Contact Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getRoleIds <em>Role Ids</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUser#getExecutiveDoctorsWorkingForIds <em>Executive Doctors Working For Ids</em>}</li>
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
	 * If the meaning of the '<em>Username</em>' attribute isn't clear, there really should be more
	 * of a description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Username</em>' attribute.
	 * @see #getUsername()
	 * @generated
	 */
	void setUsername(String value);
	
	/**
	 * Returns the value of the '<em><b>Hashed Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Hashed Password</em>' attribute isn't clear, there really should
	 * be more of a description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Hashed Password</em>' attribute.
	 * @see #getHashedPassword()
	 * @generated
	 */
	void setHashedPassword(String value);
	
	/**
	 * Returns the value of the '<em><b>Salt</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Salt</em>' attribute isn't clear, there really should be more of a
	 * description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Salt</em>' attribute.
	 * @see #getSalt()
	 * @generated
	 */
	void setSalt(String value);
	
	/**
	 * Returns the value of the '<em><b>Assigned Contact</b></em>' reference.
	 * <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Assigned Contact</em>' reference isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assigned Contact</em>' reference.
	 * @see #setAssignedContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_AssignedContact()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='kontakt'"
	 * @generated
	 */
	@Deprecated(since = "3.13")
	IContact getAssignedContact();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getAssignedContact <em>Assigned Contact</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
	 * This list is read only. Changes to it will not be stored. Use {@link #addRole(IRole)} to add
	 * a role to this user.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Roles</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Roles()
	 * @model
	 * @generated
	 */
	@Deprecated(since = "3.13")
	List<IRole> getRoles();
	
	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active</em>' attribute isn't clear, there really should be more of
	 * a description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);
	
	/**
	 * Returns the value of the '<em><b>Allow External</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Allow External</em>' attribute isn't clear, there really should be
	 * more of a description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow External</em>' attribute.
	 * @see #isAllowExternal()
	 * @generated
	 */
	void setAllowExternal(boolean value);
	
	/**
	 * Returns the value of the '<em><b>Administrator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Administrator</em>' attribute isn't clear, there really should be
	 * more of a description here...
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
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Administrator</em>' attribute.
	 * @see #isAdministrator()
	 * @generated
	 */
	void setAdministrator(boolean value);
	
	/**
	 * Returns the value of the '<em><b>Associated Contact Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Associated Contact Id</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIUser_AssociatedContactId()
	 * @model required="true" changeable="false"
	 * @generated
	 */
	String getAssociatedContactId();

	/**
	 * Returns the value of the '<em><b>Role Ids</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Role Ids</em>' attribute list.
	 * @see ch.elexis.core.model.ModelPackage#getIUser_RoleIds()
	 * @model changeable="false"
	 * @generated
	 */
	List<String> getRoleIds();

	/**
	 * Returns the value of the '<em><b>Executive Doctors Working For Ids</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Executive Doctors Working For Ids</em>' attribute list.
	 * @see ch.elexis.core.model.ModelPackage#getIUser_ExecutiveDoctorsWorkingForIds()
	 * @model changeable="false"
	 * @generated
	 */
	List<String> getExecutiveDoctorsWorkingForIds();

	/**
	 * <!-- begin-user-doc --> Add a role to the user. Do not use
	 * {@link #getRoles()#addRole(IRole)}, as it will not be stored. <!-- end-user-doc -->
	 * @model roleRequired="true"
	 * @generated
	 */
	IRole addRole(IRole role);
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @model roleRequired="true"
	 * @generated
	 */
	void removeRole(IRole role);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model passwordDataType="ch.elexis.core.types.charArray"
	 * @generated
	 */
	IUser login(String username, char[] password);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isInternal();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model rolesType="ch.elexis.core.types.List&lt;ch.elexis.core.model.IRole&gt;" rolesMany="false"
	 * @generated
	 */
	void setRoles(List<IRole> roles);
	
} // IUser
