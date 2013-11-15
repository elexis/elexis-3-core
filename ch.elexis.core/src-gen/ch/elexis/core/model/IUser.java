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

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IUser</b></em>'. <!--
 * end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link ch.elexis.core.model.IUser#getUsername <em>Username</em>}</li>
 * <li>{@link ch.elexis.core.model.IUser#getPassword <em>Password</em>}</li>
 * </ul>
 * </p>
 * 
 * @see ch.elexis.core.model.ModelPackage#getIUser()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IUser extends IContact {
	/**
	 * Returns the value of the '<em><b>Username</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Username</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Username</em>' attribute.
	 * @see #setUsername(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Username()
	 * @model
	 * @generated
	 */
	String getUsername();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getUsername <em>Username</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Username</em>' attribute.
	 * @see #getUsername()
	 * @generated
	 */
	void setUsername(String value);
	
	/**
	 * Returns the value of the '<em><b>Password</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Password</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Password</em>' attribute.
	 * @see #setPassword(String)
	 * @see ch.elexis.core.model.ModelPackage#getIUser_Password()
	 * @model
	 * @generated
	 */
	String getPassword();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IUser#getPassword <em>Password</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Password</em>' attribute.
	 * @see #getPassword()
	 * @generated
	 */
	void setPassword(String value);
	
} // IUser
