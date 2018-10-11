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

import ch.elexis.core.types.ArticleSubTyp;
import ch.elexis.core.types.ArticleTyp;
import ch.rgw.tools.Money;
import java.util.List;


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
 *   <li>{@link ch.elexis.core.model.IArticle#getAtcCode <em>Atc Code</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getSellingSize <em>Selling Size</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getPackageSize <em>Package Size</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getPackageUnit <em>Package Unit</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getProduct <em>Product</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getPurchasePrice <em>Purchase Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getSellingPrice <em>Selling Price</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#isObligation <em>Obligation</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getTyp <em>Typ</em>}</li>
 *   <li>{@link ch.elexis.core.model.IArticle#getSubTyp <em>Sub Typ</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIArticle()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IArticle extends Identifiable, IBillable, Deleteable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Global Trade Index Number
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Gtin</em>' attribute.
	 * @see #setGtin(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Gtin()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='ean'"
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
	 * Returns the value of the '<em><b>Atc Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Anatomical Therapeutic Chemical Classification System Code
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Atc Code</em>' attribute.
	 * @see #setAtcCode(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_AtcCode()
	 * @model
	 * @generated
	 */
	String getAtcCode();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getAtcCode <em>Atc Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Atc Code</em>' attribute.
	 * @see #getAtcCode()
	 * @generated
	 */
	void setAtcCode(String value);

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
	 * Returns the value of the '<em><b>Selling Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Selling Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Selling Size</em>' attribute.
	 * @see #setSellingSize(int)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_SellingSize()
	 * @model
	 * @generated
	 */
	int getSellingSize();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getSellingSize <em>Selling Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Selling Size</em>' attribute.
	 * @see #getSellingSize()
	 * @generated
	 */
	void setSellingSize(int value);

	/**
	 * Returns the value of the '<em><b>Package Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Package Size</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package Size</em>' attribute.
	 * @see #setPackageSize(int)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_PackageSize()
	 * @model
	 * @generated
	 */
	int getPackageSize();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getPackageSize <em>Package Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package Size</em>' attribute.
	 * @see #getPackageSize()
	 * @generated
	 */
	void setPackageSize(int value);

	/**
	 * Returns the value of the '<em><b>Package Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Package Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package Unit</em>' attribute.
	 * @see #setPackageUnit(String)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_PackageUnit()
	 * @model
	 * @generated
	 */
	String getPackageUnit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getPackageUnit <em>Package Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package Unit</em>' attribute.
	 * @see #getPackageUnit()
	 * @generated
	 */
	void setPackageUnit(String value);

	/**
	 * Returns the value of the '<em><b>Product</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Product</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Product</em>' reference.
	 * @see #setProduct(IArticle)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Product()
	 * @model
	 * @generated
	 */
	IArticle getProduct();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getProduct <em>Product</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Product</em>' reference.
	 * @see #getProduct()
	 * @generated
	 */
	void setProduct(IArticle value);

	/**
	 * Returns the value of the '<em><b>Purchase Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Purchase Price</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Purchase Price</em>' attribute.
	 * @see #setPurchasePrice(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_PurchasePrice()
	 * @model dataType="ch.elexis.core.types.Money"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='ekPreis'"
	 * @generated
	 */
	Money getPurchasePrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getPurchasePrice <em>Purchase Price</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Purchase Price</em>' attribute.
	 * @see #getPurchasePrice()
	 * @generated
	 */
	void setPurchasePrice(Money value);

	/**
	 * Returns the value of the '<em><b>Selling Price</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Selling Price</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Selling Price</em>' attribute.
	 * @see #setSellingPrice(Money)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_SellingPrice()
	 * @model dataType="ch.elexis.core.types.Money"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='vkPreis'"
	 * @generated
	 */
	Money getSellingPrice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getSellingPrice <em>Selling Price</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Selling Price</em>' attribute.
	 * @see #getSellingPrice()
	 * @generated
	 */
	void setSellingPrice(Money value);

	/**
	 * Returns the value of the '<em><b>Obligation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Obligation</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Obligation</em>' attribute.
	 * @see #setObligation(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Obligation()
	 * @model
	 * @generated
	 */
	boolean isObligation();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#isObligation <em>Obligation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Obligation</em>' attribute.
	 * @see #isObligation()
	 * @generated
	 */
	void setObligation(boolean value);

	/**
	 * Returns the value of the '<em><b>Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Typ</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Typ</em>' attribute.
	 * @see #setTyp(ArticleTyp)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_Typ()
	 * @model dataType="ch.elexis.core.types.ArticleTyp"
	 * @generated
	 */
	ArticleTyp getTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getTyp <em>Typ</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Typ</em>' attribute.
	 * @see #getTyp()
	 * @generated
	 */
	void setTyp(ArticleTyp value);

	/**
	 * Returns the value of the '<em><b>Sub Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sub Typ</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Typ</em>' attribute.
	 * @see #setSubTyp(ArticleSubTyp)
	 * @see ch.elexis.core.model.ModelPackage#getIArticle_SubTyp()
	 * @model dataType="ch.elexis.core.types.ArticleSubTyp"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='codeclass'"
	 * @generated
	 */
	ArticleSubTyp getSubTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IArticle#getSubTyp <em>Sub Typ</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sub Typ</em>' attribute.
	 * @see #getSubTyp()
	 * @generated
	 */
	void setSubTyp(ArticleSubTyp value);

	/**
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Product</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isProduct();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" type="ch.elexis.core.types.List&lt;ch.elexis.core.model.IArticle&gt;" many="false"
	 * @generated
	 */
	List<IArticle> getPackages();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setCode(String code);

} // IArticle
