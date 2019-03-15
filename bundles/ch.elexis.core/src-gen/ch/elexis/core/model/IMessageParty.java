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
 * A user or station that either sends or receives a message. XOR user or stationId.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IMessageParty#getUser <em>User</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessageParty#getStationId <em>Station Id</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIMessageParty()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IMessageParty {
	/**
	 * Returns the value of the '<em><b>User</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIMessageParty_User()
	 * @model changeable="false"
	 * @generated
	 */
	IUser getUser();

	/**
	 * Returns the value of the '<em><b>Station Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Station Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Station Id</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIMessageParty_StationId()
	 * @model changeable="false"
	 * @generated
	 */
	String getStationId();

} // IMessageParty
