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

import ch.elexis.core.model.ch.BillingLaw;

/**
 * <!-- begin-user-doc -->
 * <p>
 * A BilingSystem defines a way to invoice services. Each {@link ICoverage} adheres to one
 * {@link IBillingSystem} configured for it.
 * </p>
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IBillingSystem#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBillingSystem#getLaw <em>Law</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIBillingSystem()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBillingSystem {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystem_Name()
	 * @model
	 * @generated
	 */
	String getName();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystem#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);
	
	/**
	 * Returns the value of the '<em><b>Law</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Law</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Law</em>' attribute.
	 * @see #setLaw(BillingLaw)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystem_Law()
	 * @model dataType="ch.elexis.core.types.BillingLaw"
	 * @generated
	 */
	BillingLaw getLaw();
	
	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystem#getLaw <em>Law</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Law</em>' attribute.
	 * @see #getLaw()
	 * @generated
	 */
	void setLaw(BillingLaw value);
	
} // IBillingSystem
