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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IDocument Template</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IDocumentTemplate#getTemplateTyp <em>Template Typ</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocumentTemplate#getMandator <em>Mandator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocumentTemplate#isAskForAddressee <em>Ask For Addressee</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIDocumentTemplate()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IDocumentTemplate extends IDocument {
	/**
	 * Returns the value of the '<em><b>Template Typ</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Template Typ</em>' attribute.
	 * @see #setTemplateTyp(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocumentTemplate_TemplateTyp()
	 * @model
	 * @generated
	 */
	String getTemplateTyp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocumentTemplate#getTemplateTyp <em>Template Typ</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Template Typ</em>' attribute.
	 * @see #getTemplateTyp()
	 * @generated
	 */
	void setTemplateTyp(String value);

	/**
	 * Returns the value of the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mandator</em>' reference.
	 * @see #setMandator(IMandator)
	 * @see ch.elexis.core.model.ModelPackage#getIDocumentTemplate_Mandator()
	 * @model
	 * @generated
	 */
	IMandator getMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocumentTemplate#getMandator <em>Mandator</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mandator</em>' reference.
	 * @see #getMandator()
	 * @generated
	 */
	void setMandator(IMandator value);

	/**
	 * Returns the value of the '<em><b>Ask For Addressee</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ask For Addressee</em>' attribute.
	 * @see #setAskForAddressee(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIDocumentTemplate_AskForAddressee()
	 * @model
	 * @generated
	 */
	boolean isAskForAddressee();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocumentTemplate#isAskForAddressee <em>Ask For Addressee</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ask For Addressee</em>' attribute.
	 * @see #isAskForAddressee()
	 * @generated
	 */
	void setAskForAddressee(boolean value);

} // IDocumentTemplate
