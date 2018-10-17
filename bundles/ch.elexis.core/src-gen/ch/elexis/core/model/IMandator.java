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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IMandator</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IMandator#getBiller <em>Biller</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIMandator()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IMandator extends IContact {

	/**
	 * Returns the value of the '<em><b>Biller</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Biller</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Biller</em>' reference.
	 * @see #setBiller(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIMandator_Biller()
	 * @model
	 * @generated
	 */
	IContact getBiller();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMandator#getBiller <em>Biller</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Biller</em>' reference.
	 * @see #getBiller()
	 * @generated
	 */
	void setBiller(IContact value);
} // IMandator
