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

import ch.elexis.core.types.VatInfo;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IBillable</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getIBillable()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBillable extends ICodeElement, Identifiable {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" dataType="ch.elexis.core.types.VatInfo"
	 * @generated
	 */
	VatInfo getVatInfo();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	IBillableOptifier getOptifier();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	IBillableVerifier getVerifier();
} // IBillable
