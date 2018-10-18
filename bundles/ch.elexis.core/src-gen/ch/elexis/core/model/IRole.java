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

import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IRole</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IRole#isSystemRole <em>System Role</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRole#getAssignedRights <em>Assigned Rights</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIRole()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IRole extends Identifiable, WithAssignableId {

	/**
	 * Returns the value of the '<em><b>System Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>System Role</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System Role</em>' attribute.
	 * @see #setSystemRole(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIRole_SystemRole()
	 * @model
	 * @generated
	 */
	boolean isSystemRole();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRole#isSystemRole <em>System Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System Role</em>' attribute.
	 * @see #isSystemRole()
	 * @generated
	 */
	void setSystemRole(boolean value);

	/**
	 * Returns the value of the '<em><b>Assigned Rights</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IRight}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Assigned Rights</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assigned Rights</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIRole_AssignedRights()
	 * @model changeable="false"
	 * @generated
	 */
	List<IRight> getAssignedRights();
} // IRole
