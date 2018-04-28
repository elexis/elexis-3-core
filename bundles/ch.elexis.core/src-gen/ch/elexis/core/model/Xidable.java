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
 * A representation of the model object '<em><b>Xidable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.Xidable#getXid <em>Xid</em>}</li>
 *   <li>{@link ch.elexis.core.model.Xidable#getXids <em>Xids</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getXidable()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Xidable {
	/**
	 * Returns the value of the '<em><b>Xid</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xid</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xid</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getXidable_Xid()
	 * @model changeable="false"
	 * @generated
	 */
	IXid getXid();

	/**
	 * Returns the value of the '<em><b>Xids</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IXid}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xids</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xids</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getXidable_Xids()
	 * @model
	 * @generated
	 */
	List<IXid> getXids();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	String getXid(String domain);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean addXid(String domain, String domain_id, boolean updateIfExists);

} // Xidable
