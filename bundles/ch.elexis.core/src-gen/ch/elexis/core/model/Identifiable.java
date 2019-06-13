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
 * A representation of the model object '<em><b>Identifiable</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getIdentifiable()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Identifiable {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Return an identifier for this object that is guaranteed to be globally unique.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getId();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * return a human readable identifier (not necessarily unique) for this Object
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLabel();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean addXid(String domain, String id, boolean updateIfExists);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	IXid getXid(String domain);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	Long getLastupdate();

	/**
	 * <!-- begin-user-doc -->Get a list of {@link Identifiable} which were changed due to changes
	 * of this {@link Identifiable}. Used for example when setting opposite references. Returns null
	 * if no changes happend.<!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 *        type="ch.elexis.core.types.List&lt;ch.elexis.core.model.Identifiable&gt;" many="false"
	 * @generated NOT
	 */
	default List<Identifiable> getChanged(){
		return null;
	}

	/**
	 * <!-- begin-user-doc -->Add {@link Identifiable} to the list of {@link Identifiable} which
	 * were changed due to changes of this {@link Identifiable}. Used for example when setting
	 * opposite references.<!-- end-user-doc -->
	 * 
	 * @model
	 * @generated NOT
	 */
	default void addChanged(Identifiable changed){
		
	}

	/**
	 * <!-- begin-user-doc -->Clear the list of changed {@link Identifiable} <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated NOT
	 */
	default void clearChanged(){
		
	}
} // Identifiable
