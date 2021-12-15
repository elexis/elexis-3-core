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
 * A representation of the model object '<em><b>IDocument Letter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IDocumentLetter#getEncounter <em>Encounter</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIDocumentLetter()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IDocumentLetter extends IDocument {

	/**
	 * Returns the value of the '<em><b>Encounter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Encounter</em>' reference.
	 * @see #setEncounter(IEncounter)
	 * @see ch.elexis.core.model.ModelPackage#getIDocumentLetter_Encounter()
	 * @model
	 * @generated
	 */
	IEncounter getEncounter();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocumentLetter#getEncounter <em>Encounter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Encounter</em>' reference.
	 * @see #getEncounter()
	 * @generated
	 */
	void setEncounter(IEncounter value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isTemplate();
} // IDocumentLetter
