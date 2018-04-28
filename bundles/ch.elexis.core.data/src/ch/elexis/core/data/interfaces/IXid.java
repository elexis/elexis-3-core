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
package ch.elexis.core.data.interfaces;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IXid</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IXid#getDomain <em>Domain</em>}</li>
 *   <li>{@link ch.elexis.core.model.IXid#getDomainId <em>Domain Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IXid#getObject <em>Object</em>}</li>
 *   <li>{@link ch.elexis.core.model.IXid#getQuality <em>Quality</em>}</li>
 *   <li>{@link ch.elexis.core.model.IXid#isGUID <em>GUID</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIXid()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IXid extends IPersistentObject {
	/**
	 * Returns the value of the '<em><b>Domain</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Domain</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Domain</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIXid_Domain()
	 * @model changeable="false"
	 * @generated
	 */
	String getDomain();

	/**
	 * Returns the value of the '<em><b>Domain Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Domain Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Domain Id</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIXid_DomainId()
	 * @model changeable="false"
	 * @generated
	 */
	String getDomainId();

	/**
	 * Returns the value of the '<em><b>Object</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Object</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Object</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIXid_Object()
	 * @model changeable="false"
	 * @generated
	 */
	IPersistentObject getObject();

	/**
	 * Returns the value of the '<em><b>Quality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Quality</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Quality</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIXid_Quality()
	 * @model changeable="false"
	 * @generated
	 */
	int getQuality();

	/**
	 * Returns the value of the '<em><b>GUID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>GUID</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>GUID</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIXid_GUID()
	 * @model changeable="false"
	 * @generated
	 */
	boolean isGUID();

} // IXid
