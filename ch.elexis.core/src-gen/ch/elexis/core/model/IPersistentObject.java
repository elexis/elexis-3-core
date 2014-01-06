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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IPersistent Object</b></em>
 * '. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link ch.elexis.core.model.IPersistentObject#getXid <em>Xid</em>}</li>
 * <li>{@link ch.elexis.core.model.IPersistentObject#getXids <em>Xids</em>}</li>
 * </ul>
 * </p>
 * 
 * @see ch.elexis.core.model.ModelPackage#getIPersistentObject()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPersistentObject extends Identifiable {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	long getLastUpdate();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	boolean isValid();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	String storeToString();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	int state();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	boolean exists();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	boolean isAvailable();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	String getXid(String domain);
	
	/**
	 * Returns the value of the '<em><b>Xid</b></em>' reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Xid</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIPersistentObject_Xid()
	 * @model changeable="false"
	 * @generated
	 */
	IXid getXid();
	
	/**
	 * Returns the value of the '<em><b>Xids</b></em>' reference list. The list contents are of type
	 * {@link ch.elexis.core.model.IXid}. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Xids</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIPersistentObject_Xids()
	 * @model
	 * @generated
	 */
	List<IXid> getXids();
	
	/**
	 * <!-- begin-user-doc --> Assign a Xid to this object.
	 * 
	 * @param domain
	 *            the domain whose ID will be assigned
	 * @param domain_id
	 *            the id out of the given domain fot this object
	 * @param updateIfExists
	 *            if true update values if Xid with same domain and domain_id exists. Otherwise the
	 *            method will fail if a collision occurs.
	 * @return true on success, false on failure <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean addXid(String domain, String domain_id, boolean updateIfExists);
	
	public boolean isDragOK();
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model fieldsDataType="ch.elexis.core.model.StringArray"
	 *        valuesDataType="ch.elexis.core.model.StringArray"
	 * @generated
	 */
	boolean get(String[] fields, String[] values);
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	String get(String field);
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	boolean set(String field, String value);
	
} // IPersistentObject
