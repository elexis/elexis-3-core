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
 * A representation of the model object '<em><b>IStock Entry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getMinimumStock <em>Minimum Stock</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getCurrentStock <em>Current Stock</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getMaximumStock <em>Maximum Stock</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getFractionUnits <em>Fraction Units</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getStock <em>Stock</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getArticle <em>Article</em>}</li>
 *   <li>{@link ch.elexis.core.model.IStockEntry#getProvider <em>Provider</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIStockEntry()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IStockEntry extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Minimum Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Minimum Stock</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Minimum Stock</em>' attribute.
	 * @see #setMinimumStock(int)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_MinimumStock()
	 * @model
	 * @generated
	 */
	int getMinimumStock();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getMinimumStock <em>Minimum Stock</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Minimum Stock</em>' attribute.
	 * @see #getMinimumStock()
	 * @generated
	 */
	void setMinimumStock(int value);

	/**
	 * Returns the value of the '<em><b>Current Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Current Stock</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Current Stock</em>' attribute.
	 * @see #setCurrentStock(int)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_CurrentStock()
	 * @model
	 * @generated
	 */
	int getCurrentStock();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getCurrentStock <em>Current Stock</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Current Stock</em>' attribute.
	 * @see #getCurrentStock()
	 * @generated
	 */
	void setCurrentStock(int value);

	/**
	 * Returns the value of the '<em><b>Maximum Stock</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Maximum Stock</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Maximum Stock</em>' attribute.
	 * @see #setMaximumStock(int)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_MaximumStock()
	 * @model
	 * @generated
	 */
	int getMaximumStock();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getMaximumStock <em>Maximum Stock</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Maximum Stock</em>' attribute.
	 * @see #getMaximumStock()
	 * @generated
	 */
	void setMaximumStock(int value);

	/**
	 * Returns the value of the '<em><b>Fraction Units</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Fraction Units</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fraction Units</em>' attribute.
	 * @see #setFractionUnits(int)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_FractionUnits()
	 * @model
	 * @generated
	 */
	int getFractionUnits();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getFractionUnits <em>Fraction Units</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fraction Units</em>' attribute.
	 * @see #getFractionUnits()
	 * @generated
	 */
	void setFractionUnits(int value);

	/**
	 * Returns the value of the '<em><b>Stock</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stock</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stock</em>' reference.
	 * @see #setStock(IStock)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_Stock()
	 * @model
	 * @generated
	 */
	IStock getStock();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getStock <em>Stock</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stock</em>' reference.
	 * @see #getStock()
	 * @generated
	 */
	void setStock(IStock value);

	/**
	 * Returns the value of the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Article</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article</em>' reference.
	 * @see #setArticle(IArticle)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_Article()
	 * @model
	 * @generated
	 */
	IArticle getArticle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getArticle <em>Article</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article</em>' reference.
	 * @see #getArticle()
	 * @generated
	 */
	void setArticle(IArticle value);

	/**
	 * Returns the value of the '<em><b>Provider</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Provider</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Provider</em>' reference.
	 * @see #setProvider(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIStockEntry_Provider()
	 * @model
	 * @generated
	 */
	IContact getProvider();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IStockEntry#getProvider <em>Provider</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Provider</em>' reference.
	 * @see #getProvider()
	 * @generated
	 */
	void setProvider(IContact value);

	boolean getMediorder();

	void setMediorder(boolean Value);


} // IStockEntry
