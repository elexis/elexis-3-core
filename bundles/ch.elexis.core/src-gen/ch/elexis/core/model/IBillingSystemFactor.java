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

import java.time.LocalDate;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IBilling System Factor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IBillingSystemFactor#getSystem <em>System</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBillingSystemFactor#getFactor <em>Factor</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBillingSystemFactor#getValidFrom <em>Valid From</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBillingSystemFactor#getValidTo <em>Valid To</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIBillingSystemFactor()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBillingSystemFactor extends Identifiable {
	/**
	 * Returns the value of the '<em><b>System</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>System</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' attribute.
	 * @see #setSystem(String)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystemFactor_System()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='typ'"
	 * @generated
	 */
	String getSystem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystemFactor#getSystem <em>System</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' attribute.
	 * @see #getSystem()
	 * @generated
	 */
	void setSystem(String value);

	/**
	 * Returns the value of the '<em><b>Factor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Factor</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Factor</em>' attribute.
	 * @see #setFactor(double)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystemFactor_Factor()
	 * @model
	 * @generated
	 */
	double getFactor();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystemFactor#getFactor <em>Factor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Factor</em>' attribute.
	 * @see #getFactor()
	 * @generated
	 */
	void setFactor(double value);

	/**
	 * Returns the value of the '<em><b>Valid From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Valid From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Valid From</em>' attribute.
	 * @see #setValidFrom(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystemFactor_ValidFrom()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='datum_von'"
	 * @generated
	 */
	LocalDate getValidFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystemFactor#getValidFrom <em>Valid From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Valid From</em>' attribute.
	 * @see #getValidFrom()
	 * @generated
	 */
	void setValidFrom(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Valid To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Valid To</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Valid To</em>' attribute.
	 * @see #setValidTo(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIBillingSystemFactor_ValidTo()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='datum_bis'"
	 * @generated
	 */
	LocalDate getValidTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBillingSystemFactor#getValidTo <em>Valid To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Valid To</em>' attribute.
	 * @see #getValidTo()
	 * @generated
	 */
	void setValidTo(LocalDate value);

} // IBillingSystemFactor
