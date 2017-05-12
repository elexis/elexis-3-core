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
import java.util.Map;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IVerification Context</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IVerificationContext#getItems <em>Items</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerificationContext#getErrors <em>Errors</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerificationContext#getInfo <em>Info</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIVerificationContext()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVerificationContext<T> {
	/**
	 * Returns the value of the '<em><b>Items</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Items</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Items</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIVerificationContext_Items()
	 * @model kind="reference" changeable="false"
	 * @generated
	 */
	List<T> getItems();

	/**
	 * Returns the value of the '<em><b>Errors</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Errors</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Errors</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIVerificationContext_Errors()
	 * @model kind="reference" changeable="false"
	 * @generated
	 */
	List<T> getErrors();

	/**
	 * Returns the value of the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Info</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Info</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIVerificationContext_Info()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	Map<String, String> getInfo();

} // IVerificationContext
