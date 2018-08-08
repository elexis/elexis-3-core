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
 * A representation of the model object '<em><b>IDiagnosis Tree</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IDiagnosisTree#getParent <em>Parent</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDiagnosisTree#getChildren <em>Children</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIDiagnosisTree()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IDiagnosisTree extends IDiagnosis {
	/**
	 * Returns the value of the '<em><b>Parent</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' reference.
	 * @see #setParent(IDiagnosisTree)
	 * @see ch.elexis.core.model.ModelPackage#getIDiagnosisTree_Parent()
	 * @model
	 * @generated
	 */
	IDiagnosisTree getParent();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDiagnosisTree#getParent <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(IDiagnosisTree value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IDiagnosisTree}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIDiagnosisTree_Children()
	 * @model
	 * @generated
	 */
	List<IDiagnosisTree> getChildren();

} // IDiagnosisTree
