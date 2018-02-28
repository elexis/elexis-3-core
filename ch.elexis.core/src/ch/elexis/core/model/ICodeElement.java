/**
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
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
 * A representation of the model object '<em><b>ICode Element</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getICodeElement()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ICodeElement {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getCodeSystemName();
	
	/**
	 * @return the code system code (e.g. used as code for billing against tarmed)
	 * @since 3.4 extracted from VerrechenbarAdapter as default value
	 */
	default String getCodeSystemCode() {
		return "999";
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getId();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getCode();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getText();

	/**
	 * Contribute actions to the user interface
	 * @param context
	 * @return 
	 * @since 3.4 set interface default method returning <code>null</code>
	 */
	default List<Object> getActions(Object context) {
		return null;
	}

} // ICodeElement
