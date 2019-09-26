/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import java.util.Map;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>With Ext Info</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getWithExtInfo()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface WithExtInfo {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Object getExtInfo(Object key);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setExtInfo(Object key, Object value);

	/**
	 * <!-- begin-user-doc --> <b>Modifications to the returned map are not persisted.</b> Use
	 * {@link #setExtInfo(Object, Object)} to handle persistent sets <!-- end-user-doc -->
	 * @model kind="operation" type="ch.elexis.core.types.Map&lt;org.eclipse.emf.ecore.EJavaObject, org.eclipse.emf.ecore.EJavaObject&gt;"
	 * @generated
	 */
	Map<Object, Object> getMap();

} // WithExtInfo
