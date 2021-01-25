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

import java.time.LocalDate;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ISick Certificate</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getCoverage <em>Coverage</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getLetter <em>Letter</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getPercent <em>Percent</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getStart <em>Start</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getEnd <em>End</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getReason <em>Reason</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISickCertificate#getNote <em>Note</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getISickCertificate()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ISickCertificate extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' reference.
	 * @see #setPatient(IPatient)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Patient()
	 * @model
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getPatient <em>Patient</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' reference.
	 * @see #getPatient()
	 * @generated
	 */
	void setPatient(IPatient value);

	/**
	 * Returns the value of the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Coverage</em>' reference.
	 * @see #setCoverage(ICoverage)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Coverage()
	 * @model
	 * @generated
	 */
	ICoverage getCoverage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getCoverage <em>Coverage</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Coverage</em>' reference.
	 * @see #getCoverage()
	 * @generated
	 */
	void setCoverage(ICoverage value);

	/**
	 * Returns the value of the '<em><b>Letter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Letter</em>' reference.
	 * @see #setLetter(IDocumentLetter)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Letter()
	 * @model
	 * @generated
	 */
	IDocumentLetter getLetter();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getLetter <em>Letter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Letter</em>' reference.
	 * @see #getLetter()
	 * @generated
	 */
	void setLetter(IDocumentLetter value);

	/**
	 * Returns the value of the '<em><b>Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Percent</em>' attribute.
	 * @see #setPercent(int)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Percent()
	 * @model
	 * @generated
	 */
	int getPercent();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getPercent <em>Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Percent</em>' attribute.
	 * @see #getPercent()
	 * @generated
	 */
	void setPercent(int value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Date()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start</em>' attribute.
	 * @see #setStart(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Start()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getStart();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getStart <em>Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start</em>' attribute.
	 * @see #getStart()
	 * @generated
	 */
	void setStart(LocalDate value);

	/**
	 * Returns the value of the '<em><b>End</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End</em>' attribute.
	 * @see #setEnd(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_End()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getEnd();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getEnd <em>End</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End</em>' attribute.
	 * @see #getEnd()
	 * @generated
	 */
	void setEnd(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reason</em>' attribute.
	 * @see #setReason(String)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Reason()
	 * @model
	 * @generated
	 */
	String getReason();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getReason <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reason</em>' attribute.
	 * @see #getReason()
	 * @generated
	 */
	void setReason(String value);

	/**
	 * Returns the value of the '<em><b>Note</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Note</em>' attribute.
	 * @see #setNote(String)
	 * @see ch.elexis.core.model.ModelPackage#getISickCertificate_Note()
	 * @model
	 * @generated
	 */
	String getNote();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISickCertificate#getNote <em>Note</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Note</em>' attribute.
	 * @see #getNote()
	 * @generated
	 */
	void setNote(String value);

} // ISickCertificate
