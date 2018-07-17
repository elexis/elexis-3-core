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
 * A representation of the model object '<em><b>IArticle</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IArticle#getGtin <em>Gtin</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getSellingUnit <em>Selling Unit</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getPackageUnit <em>Package Unit</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#isProduct <em>Product</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIArticle()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IArticle extends Identifiable {
	/**
	 * Returns the value of the '<em><b>Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Gtin</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Gtin</em>' attribute.
	 * @see #setGtin(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Gtin()
	 * @model
	 * @generated
	 */
	String getGtin();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getGtin <em>Gtin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Gtin</em>' attribute.
	 * @see #getGtin()
	 * @generated
	 */
	void setGtin(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Selling Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Selling Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Selling Unit</em>' attribute.
	 * @see #setSellingUnit(int)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_SellingUnit()
	 * @model
	 * @generated
	 */
	int getSellingUnit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getSellingUnit <em>Selling Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Selling Unit</em>' attribute.
	 * @see #getSellingUnit()
	 * @generated
	 */
	void setSellingUnit(int value);

	/**
	 * Returns the value of the '<em><b>Package Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Package Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package Unit</em>' attribute.
	 * @see #setPackageUnit(int)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_PackageUnit()
	 * @model
	 * @generated
	 */
	int getPackageUnit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getPackageUnit <em>Package Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package Unit</em>' attribute.
	 * @see #getPackageUnit()
	 * @generated
	 */
	void setPackageUnit(int value);

	/**
	 * Returns the value of the '<em><b>Product</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Product</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Product</em>' attribute.
	 * @see #setProduct(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Product()
	 * @model
	 * @generated
	 */
	boolean isProduct();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#isProduct <em>Product</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Product</em>' attribute.
	 * @see #isProduct()
	 * @generated
	 */
	void setProduct(boolean value);

} // IArticle
