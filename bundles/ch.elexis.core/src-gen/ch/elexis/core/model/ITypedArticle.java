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

import ch.elexis.core.types.ArticleTyp;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ITyped Article</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ITypedArticle#getTyp <em>Typ</em>}</li>
 *   <li>{@link ch.elexis.core.model.ITypedArticle#getSubTyp <em>Sub Typ</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getITypedArticle()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ITypedArticle extends IArticle, WithExtInfo {
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
	 * @see ch.elexis.core.model.ModelPackage#getITypedArticle_Typ()
	 * @model dataType="ch.elexis.core.types.ArticleTyp"
	 * @generated
	 */
	ArticleTyp getTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITypedArticle#getTyp <em>Typ</em>}' attribute.
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
	 * @see #setSubTyp(String)
	 * @see ch.elexis.core.model.ModelPackage#getITypedArticle_SubTyp()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='codeclass'"
	 * @generated
	 */
	String getSubTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITypedArticle#getSubTyp <em>Sub Typ</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sub Typ</em>' attribute.
	 * @see #getSubTyp()
	 * @generated
	 */
	void setSubTyp(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setCode(String code);

} // ITypedArticle
