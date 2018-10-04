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

import ch.elexis.core.model.prescription.EntryType;
import java.time.LocalDateTime;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IPrescription</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IPrescription#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getArticle <em>Article</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getDateFrom <em>Date From</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getDateTo <em>Date To</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getDosageInstruction <em>Dosage Instruction</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getRemark <em>Remark</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getStopReason <em>Stop Reason</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getEntryType <em>Entry Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IPrescription#getDisposalComment <em>Disposal Comment</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIPrescription()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IPrescription extends Identifiable, Deleteable {
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
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_Patient()
	 * @model required="true"
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getPatient <em>Patient</em>}' reference.
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
	 * <p>
	 * If the meaning of the '<em>Article</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Article</em>' reference.
	 * @see #setArticle(IArticle)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_Article()
	 * @model
	 * @generated
	 */
	IArticle getArticle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getArticle <em>Article</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Article</em>' reference.
	 * @see #getArticle()
	 * @generated
	 */
	void setArticle(IArticle value);

	/**
	 * Returns the value of the '<em><b>Date From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date From</em>' attribute.
	 * @see #setDateFrom(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_DateFrom()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getDateFrom();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getDateFrom <em>Date From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date From</em>' attribute.
	 * @see #getDateFrom()
	 * @generated
	 */
	void setDateFrom(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Date To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date To</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date To</em>' attribute.
	 * @see #setDateTo(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_DateTo()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getDateTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getDateTo <em>Date To</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date To</em>' attribute.
	 * @see #getDateTo()
	 * @generated
	 */
	void setDateTo(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Dosage Instruction</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Dosage Instruction</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dosage Instruction</em>' attribute.
	 * @see #setDosageInstruction(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_DosageInstruction()
	 * @model
	 * @generated
	 */
	String getDosageInstruction();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getDosageInstruction <em>Dosage Instruction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dosage Instruction</em>' attribute.
	 * @see #getDosageInstruction()
	 * @generated
	 */
	void setDosageInstruction(String value);

	/**
	 * Returns the value of the '<em><b>Remark</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Remark</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Remark</em>' attribute.
	 * @see #setRemark(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_Remark()
	 * @model
	 * @generated
	 */
	String getRemark();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getRemark <em>Remark</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Remark</em>' attribute.
	 * @see #getRemark()
	 * @generated
	 */
	void setRemark(String value);

	/**
	 * Returns the value of the '<em><b>Stop Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stop Reason</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stop Reason</em>' attribute.
	 * @see #setStopReason(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_StopReason()
	 * @model
	 * @generated
	 */
	String getStopReason();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getStopReason <em>Stop Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stop Reason</em>' attribute.
	 * @see #getStopReason()
	 * @generated
	 */
	void setStopReason(String value);

	/**
	 * Returns the value of the '<em><b>Entry Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Entry Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Entry Type</em>' attribute.
	 * @see #setEntryType(EntryType)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_EntryType()
	 * @model dataType="ch.elexis.core.types.EntryType"
	 * @generated
	 */
	EntryType getEntryType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getEntryType <em>Entry Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Entry Type</em>' attribute.
	 * @see #getEntryType()
	 * @generated
	 */
	void setEntryType(EntryType value);

	/**
	 * Returns the value of the '<em><b>Disposal Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Disposal Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Disposal Comment</em>' attribute.
	 * @see #setDisposalComment(String)
	 * @see ch.elexis.core.model.ModelPackage#getIPrescription_DisposalComment()
	 * @model
	 * @generated
	 */
	String getDisposalComment();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IPrescription#getDisposalComment <em>Disposal Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Disposal Comment</em>' attribute.
	 * @see #getDisposalComment()
	 * @generated
	 */
	void setDisposalComment(String value);

} // IPrescription
