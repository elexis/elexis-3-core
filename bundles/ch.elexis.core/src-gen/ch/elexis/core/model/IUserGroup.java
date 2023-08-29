/**
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
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
 * A representation of the model object '<em><b>IUser Group</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IUserGroup#getUsers <em>Users</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUserGroup#getRoles <em>Roles</em>}</li>
 *   <li>{@link ch.elexis.core.model.IUserGroup#getGroupname <em>Groupname</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIUserGroup()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IUserGroup extends Deleteable, Identifiable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Users</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IUser}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Users</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIUserGroup_Users()
	 * @model
	 * @generated
	 */
	List<IUser> getUsers();

	/**
	 * Returns the value of the '<em><b>Roles</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IRole}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Roles</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIUserGroup_Roles()
	 * @model
	 * @generated
	 */
	List<IRole> getRoles();

	/**
	 * Returns the value of the '<em><b>Groupname</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Groupname</em>' attribute.
	 * @see #setGroupname(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUserGroup_Groupname()
	 * @model
	 * @generated
	 */
	String getGroupname();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUserGroup#getGroupname <em>Groupname</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Groupname</em>' attribute.
	 * @see #getGroupname()
	 * @generated
	 */
	void setGroupname(String value);

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

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model userRequired="true"
	 * @generated
	 */
	IUser addUser(IUser user);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model userRequired="true"
	 * @generated
	 */
	void removeUser(IUser user);

} // IUserGroup
