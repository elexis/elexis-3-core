/**
 * Copyright (c) 2024 MEDEVIT <office@medevit.at>.
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
 * A representation of the model object '<em><b>IVaccination</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IVaccination#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getArticle <em>Article</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getArticleName <em>Article Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getArticleGtin <em>Article Gtin</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getArticleAtc <em>Article Atc</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getLotNumber <em>Lot Number</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getDateOfAdministration <em>Date Of Administration</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getIngredientsAtc <em>Ingredients Atc</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getPerformer <em>Performer</em>}</li>
 *   <li>{@link ch.elexis.core.model.IVaccination#getSide <em>Side</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIVaccination()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IVaccination extends Identifiable, Deleteable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' reference.
	 * @see #setPatient(IPatient)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_Patient()
	 * @model required="true"
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getPatient <em>Patient</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' reference.
	 * @see #getPatient()
	 * @generated
	 */
	void setPatient(IPatient value);

	/**
	 * Returns the value of the '<em><b>Article</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article</em>' reference.
	 * @see #setArticle(IArticle)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_Article()
	 * @model
	 * @generated
	 */
	IArticle getArticle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getArticle <em>Article</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article</em>' reference.
	 * @see #getArticle()
	 * @generated
	 */
	void setArticle(IArticle value);

	/**
	 * Returns the value of the '<em><b>Article Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article Name</em>' attribute.
	 * @see #setArticleName(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_ArticleName()
	 * @model
	 * @generated
	 */
	String getArticleName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getArticleName <em>Article Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article Name</em>' attribute.
	 * @see #getArticleName()
	 * @generated
	 */
	void setArticleName(String value);

	/**
	 * Returns the value of the '<em><b>Article Gtin</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article Gtin</em>' attribute.
	 * @see #setArticleGtin(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_ArticleGtin()
	 * @model
	 * @generated
	 */
	String getArticleGtin();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getArticleGtin <em>Article Gtin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article Gtin</em>' attribute.
	 * @see #getArticleGtin()
	 * @generated
	 */
	void setArticleGtin(String value);

	/**
	 * Returns the value of the '<em><b>Article Atc</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article Atc</em>' attribute.
	 * @see #setArticleAtc(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_ArticleAtc()
	 * @model
	 * @generated
	 */
	String getArticleAtc();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getArticleAtc <em>Article Atc</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article Atc</em>' attribute.
	 * @see #getArticleAtc()
	 * @generated
	 */
	void setArticleAtc(String value);

	/**
	 * Returns the value of the '<em><b>Lot Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lot Number</em>' attribute.
	 * @see #setLotNumber(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_LotNumber()
	 * @model
	 * @generated
	 */
	String getLotNumber();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getLotNumber <em>Lot Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lot Number</em>' attribute.
	 * @see #getLotNumber()
	 * @generated
	 */
	void setLotNumber(String value);

	/**
	 * Returns the value of the '<em><b>Date Of Administration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date Of Administration</em>' attribute.
	 * @see #setDateOfAdministration(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_DateOfAdministration()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDateOfAdministration();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getDateOfAdministration <em>Date Of Administration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date Of Administration</em>' attribute.
	 * @see #getDateOfAdministration()
	 * @generated
	 */
	void setDateOfAdministration(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Ingredients Atc</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ingredients Atc</em>' attribute.
	 * @see #setIngredientsAtc(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_IngredientsAtc()
	 * @model
	 * @generated
	 */
	String getIngredientsAtc();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getIngredientsAtc <em>Ingredients Atc</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ingredients Atc</em>' attribute.
	 * @see #getIngredientsAtc()
	 * @generated
	 */
	void setIngredientsAtc(String value);

	/**
	 * Returns the value of the '<em><b>Performer</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Performer</em>' reference.
	 * @see #setPerformer(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_Performer()
	 * @model
	 * @generated
	 */
	IContact getPerformer();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getPerformer <em>Performer</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Performer</em>' reference.
	 * @see #getPerformer()
	 * @generated
	 */
	void setPerformer(IContact value);

	/**
	 * Returns the value of the '<em><b>Side</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Side</em>' attribute.
	 * @see #setSide(String)
	 * @see ch.elexis.core.model.ModelPackage#getIVaccination_Side()
	 * @model
	 * @generated
	 */
	String getSide();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IVaccination#getSide <em>Side</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Side</em>' attribute.
	 * @see #getSide()
	 * @generated
	 */
	void setSide(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getPerformerLabel();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setPerformerLabel(String label);

} // IVaccination
