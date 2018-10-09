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
 * A representation of the model object '<em><b>IBilled</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IBilled#getBillable <em>Billable</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getEncounter <em>Encounter</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getAmount <em>Amount</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getPrice <em>Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getNetPrice <em>Net Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getText <em>Text</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getPoints <em>Points</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getFactor <em>Factor</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getPrimaryScale <em>Primary Scale</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getSecondaryScale <em>Secondary Scale</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getCode <em>Code</em>}</li>
 *   <li>{@link ch.elexis.core.model.IBilled#getTotal <em>Total</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIBilled()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBilled extends Identifiable, Deleteable, WithExtInfo {

	/**
	 * Returns the value of the '<em><b>Billable</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billable</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billable</em>' reference.
	 * @see #setBillable(IBillable)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Billable()
	 * @model
	 * @generated
	 */
	IBillable getBillable();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getBillable <em>Billable</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billable</em>' reference.
	 * @see #getBillable()
	 * @generated
	 */
	void setBillable(IBillable value);

	/**
	 * Returns the value of the '<em><b>Encounter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Encounter</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Encounter</em>' reference.
	 * @see #setEncounter(IEncounter)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Encounter()
	 * @model
	 * @generated
	 */
	IEncounter getEncounter();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getEncounter <em>Encounter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Encounter</em>' reference.
	 * @see #getEncounter()
	 * @generated
	 */
	void setEncounter(IEncounter value);

	/**
	 * Returns the value of the '<em><b>Amount</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Amount</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Amount</em>' attribute.
	 * @see #setAmount(double)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Amount()
	 * @model
	 * @generated
	 */
	double getAmount();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getAmount <em>Amount</em>}'
	 * attribute. <!-- begin-user-doc -->
	 * <p>
	 * Set the billed amount. Setting to a non integer value is only possible if the price was not
	 * changed manually, and if set to a non integer it is not possible to manually change the
	 * price.
	 * </p>
	 * <p>
	 * Throws an IllegalStateException if the price was changed manually.
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Amount</em>' attribute.
	 * @see #getAmount()
	 * @generated
	 */
	void setAmount(double value);

	/**
	 * Returns the value of the '<em><b>Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Get the price of the billed without considering scaling or amount.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Price</em>' attribute.
	 * @see #setPrice(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Price()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getPrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getPrice <em>Price</em>}'
	 * attribute. <!-- begin-user-doc -->
	 * <p>
	 * Set the price of the billed without considering scaling or amount. After setting the price
	 * using this method {@link IBilled#isChangedPrice()} will return true. Afterwards it is not
	 * possible to change the amount to a non integer value.
	 * </p>
	 * <p>
	 * Throws an IllegalStateException if the amount is a non integer.
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Price</em>' attribute.
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
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_NetPrice()
	 * @model dataType="ch.elexis.core.types.Money"
	 * @generated
	 */
	Money getNetPrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getNetPrice <em>Net Price</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Net Price</em>' attribute.
	 * @see #getNetPrice()
	 * @generated
	 */
	void setNetPrice(Money value);

	/**
	 * Returns the value of the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Text</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Text</em>' attribute.
	 * @see #setText(String)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Text()
	 * @model
	 * @generated
	 */
	String getText();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getText <em>Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Text</em>' attribute.
	 * @see #getText()
	 * @generated
	 */
	void setText(String value);

	/**
	 * Returns the value of the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Points</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Points</em>' attribute.
	 * @see #setPoints(int)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Points()
	 * @model
	 * @generated
	 */
	int getPoints();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getPoints <em>Points</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Points</em>' attribute.
	 * @see #getPoints()
	 * @generated
	 */
	void setPoints(int value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Factor()
	 * @model
	 * @generated
	 */
	double getFactor();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getFactor <em>Factor</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Factor</em>' attribute.
	 * @see #getFactor()
	 * @generated
	 */
	void setFactor(double value);

	/**
	 * Returns the value of the '<em><b>Primary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Primary Scale</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Primary Scale</em>' attribute.
	 * @see #setPrimaryScale(int)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_PrimaryScale()
	 * @model
	 * @generated
	 */
	int getPrimaryScale();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getPrimaryScale <em>Primary Scale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Primary Scale</em>' attribute.
	 * @see #getPrimaryScale()
	 * @generated
	 */
	void setPrimaryScale(int value);

	/**
	 * Returns the value of the '<em><b>Secondary Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Can represent a manual changed price, or a non integer amount. Use
	 * {@link IBilled#isChangedPrice()} and {@link IBilled#isNonIntegerAmount()} to check. As both
	 * use the secondary scale only one can be true.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Secondary Scale</em>' attribute.
	 * @see #setSecondaryScale(int)
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_SecondaryScale()
	 * @model
	 * @generated
	 */
	int getSecondaryScale();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IBilled#getSecondaryScale <em>Secondary Scale</em>}' attribute.
	 * <!-- begin-user-doc --> Can represent a manual changed price, or a
	 * non integer amount. Use {@link IBilled#isChangedPrice()} and
	 * {@link IBilled#isNonIntegerAmount()} to check if the value is already used. <b>If set without
	 * checking usage first, this can lead to inconsistent data</b> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Secondary Scale</em>' attribute.
	 * @see #getSecondaryScale()
	 * @generated
	 */
	void setSecondaryScale(int value);

	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Code</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Code()
	 * @model changeable="false"
	 * @generated
	 */
	String getCode();

	/**
	 * Returns the value of the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Get the total price of the billed including scaling and amount.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIBilled_Total()
	 * @model dataType="ch.elexis.core.types.Money" changeable="false"
	 * @generated
	 */
	Money getTotal();

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Test if the price was changed manually
	 * </p>
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isChangedPrice();

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * Test if the amount is a non integer value
	 * </p>
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isNonIntegerAmount();
} // IBilled
