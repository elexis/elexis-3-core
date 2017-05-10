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
 * A representation of the model object '<em><b>IVerify Context</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IVerifyContext#getItems <em>Items</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerifyContext#getErrors <em>Errors</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerifyContext#getIVerifyConverter <em>IVerify Converter</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVerifyContext#getInfo <em>Info</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIVerifyContext()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVerifyContext {
	
	/**
	 * Returns the value of the '<em><b>Items</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IVerify}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Items</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Items</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIVerifyContext_Items()
	 * @model changeable="false"
	 * @generated
	 */
	List<IVerify> getItems();

	/**
	 * Returns the value of the '<em><b>Errors</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IVerify}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Errors</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Errors</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIVerifyContext_Errors()
	 * @model changeable="false"
	 * @generated
	 */
	List<IVerify> getErrors();

	/**
	 * Returns the value of the '<em><b>IVerify Converter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>IVerify Converter</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>IVerify Converter</em>' reference.
	 * @see #setIVerifyConverter(IVerifyConverter)
	 * @see ch.elexis.core.model.ModelPackage#getIVerifyContext_IVerifyConverter()
	 * @model
	 * @generated
	 */
	IVerifyConverter getIVerifyConverter();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVerifyContext#getIVerifyConverter <em>IVerify Converter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>IVerify Converter</em>' reference.
	 * @see #getIVerifyConverter()
	 * @generated
	 */
	void setIVerifyConverter(IVerifyConverter value);

	/**
	 * Returns the value of the '<em><b>Info</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Info</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Info</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIVerifyContext_Info()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	Map<String, String> getInfo();


} // IVerifyContext
