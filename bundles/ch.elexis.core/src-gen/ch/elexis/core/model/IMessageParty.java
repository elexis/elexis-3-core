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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IMessage Party</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A station, user or other type if message party.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IMessageParty#getIdentifier <em>Identifier</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessageParty#getType <em>Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIMessageParty()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IMessageParty {
	/**
	 * Returns the value of the '<em><b>Identifier</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Identifier</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIMessageParty_Identifier()
	 * @model changeable="false"
	 * @generated
	 */
	String getIdentifier();

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The message party type: 0=user (default), 1=station; more to follow on requirement
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIMessageParty_Type()
	 * @model default="0" required="true" changeable="false"
	 * @generated
	 */
	int getType();

} // IMessageParty
