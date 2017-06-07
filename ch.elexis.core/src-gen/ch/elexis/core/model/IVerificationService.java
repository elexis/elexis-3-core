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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IVerification Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IVerificationService#getValidatorId <em>Validator Id</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIVerificationService()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVerificationService<T> {
	/**
	 * Returns the value of the '<em><b>Validator Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Validator Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Validator Id</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIVerificationService_ValidatorId()
	 * @model changeable="false"
	 * @generated
	 */
	String getValidatorId();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	T validate(IVerificationContext<T> iVerificationContext, T verification);

} // IVerificationService
