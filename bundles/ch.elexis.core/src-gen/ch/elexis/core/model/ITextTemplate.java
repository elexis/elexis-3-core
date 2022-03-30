/**
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import ch.elexis.core.types.TextTemplateCategory;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IText
 * Template</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link ch.elexis.core.model.ITextTemplate#getCategory
 * <em>Category</em>}</li>
 * <li>{@link ch.elexis.core.model.ITextTemplate#getMandator
 * <em>Mandator</em>}</li>
 * <li>{@link ch.elexis.core.model.ITextTemplate#getName <em>Name</em>}</li>
 * <li>{@link ch.elexis.core.model.ITextTemplate#getTemplate
 * <em>Template</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getITextTemplate()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ITextTemplate extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Category</b></em>' attribute. The literals
	 * are from the enumeration {@link ch.elexis.core.types.TextTemplateCategory}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Category</em>' attribute.
	 * @see ch.elexis.core.types.TextTemplateCategory
	 * @see #setCategory(TextTemplateCategory)
	 * @see ch.elexis.core.model.ModelPackage#getITextTemplate_Category()
	 * @model
	 * @generated
	 */
	TextTemplateCategory getCategory();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITextTemplate#getCategory
	 * <em>Category</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Category</em>' attribute.
	 * @see ch.elexis.core.types.TextTemplateCategory
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(TextTemplateCategory value);

	/**
	 * Returns the value of the '<em><b>Mandator</b></em>' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Mandator</em>' reference.
	 * @see #setMandator(IMandator)
	 * @see ch.elexis.core.model.ModelPackage#getITextTemplate_Mandator()
	 * @model
	 * @generated
	 */
	IMandator getMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITextTemplate#getMandator
	 * <em>Mandator</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Mandator</em>' reference.
	 * @see #getMandator()
	 * @generated
	 */
	void setMandator(IMandator value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getITextTemplate_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITextTemplate#getName
	 * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Template</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Template</em>' attribute.
	 * @see #setTemplate(String)
	 * @see ch.elexis.core.model.ModelPackage#getITextTemplate_Template()
	 * @model
	 * @generated
	 */
	String getTemplate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ITextTemplate#getTemplate
	 * <em>Template</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Template</em>' attribute.
	 * @see #getTemplate()
	 * @generated
	 */
	void setTemplate(String value);

} // ITextTemplate
