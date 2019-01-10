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

import java.time.LocalDateTime;
import java.util.List;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IRecipe</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IRecipe#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRecipe#getMandator <em>Mandator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRecipe#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRecipe#getPrescriptions <em>Prescriptions</em>}</li>
 *   <li>{@link ch.elexis.core.model.IRecipe#getDocument <em>Document</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIRecipe()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IRecipe extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' reference.
	 * @see #setPatient(IPatient)
	 * @see ch.elexis.core.model.ModelPackage#getIRecipe_Patient()
	 * @model required="true"
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRecipe#getPatient <em>Patient</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' reference.
	 * @see #getPatient()
	 * @generated
	 */
	void setPatient(IPatient value);

	/**
	 * Returns the value of the '<em><b>Mandator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mandator</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mandator</em>' reference.
	 * @see #setMandator(IMandator)
	 * @see ch.elexis.core.model.ModelPackage#getIRecipe_Mandator()
	 * @model
	 * @generated
	 */
	IMandator getMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRecipe#getMandator <em>Mandator</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mandator</em>' reference.
	 * @see #getMandator()
	 * @generated
	 */
	void setMandator(IMandator value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIRecipe_Date()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRecipe#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Prescriptions</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IPrescription}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Prescriptions</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prescriptions</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIRecipe_Prescriptions()
	 * @model
	 * @generated
	 */
	List<IPrescription> getPrescriptions();

	/**
	 * Returns the value of the '<em><b>Document</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Document</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Document</em>' reference.
	 * @see #setDocument(IDocumentLetter)
	 * @see ch.elexis.core.model.ModelPackage#getIRecipe_Document()
	 * @model
	 * @generated
	 */
	IDocumentLetter getDocument();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IRecipe#getDocument <em>Document</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Document</em>' reference.
	 * @see #getDocument()
	 * @generated
	 */
	void setDocument(IDocumentLetter value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model prescriptionRequired="true"
	 * @generated
	 */
	void removePrescription(IPrescription prescription);

} // IRecipe
