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

import ch.rgw.tools.Money;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IService</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IService#getPrice <em>Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IService#getNetPrice <em>Net Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IService#getMinutes <em>Minutes</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIService()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IService extends IBillable, Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Price</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Price</em>' attribute.
	 * @see #setPrice(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIService_Price()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getPrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IService#getPrice <em>Price</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Price</em>' attribute.
	 * @see #getPrice()
	 * @generated
	 */
	void setPrice(Money value);

	/**
	 * Returns the value of the '<em><b>Net Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Net Price</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Net Price</em>' attribute.
	 * @see #setNetPrice(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIService_NetPrice()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getNetPrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IService#getNetPrice <em>Net Price</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Net Price</em>' attribute.
	 * @see #getNetPrice()
	 * @generated
	 */
	void setNetPrice(Money value);

	/**
	 * Returns the value of the '<em><b>Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Minutes</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Minutes</em>' attribute.
	 * @see #setMinutes(int)
	 * @see ch.elexis.core.model.ModelPackage#getIService_Minutes()
	 * @model
	 * @generated
	 */
	int getMinutes();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IService#getMinutes <em>Minutes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Minutes</em>' attribute.
	 * @see #getMinutes()
	 * @generated
	 */
	void setMinutes(int value);

} // IService
