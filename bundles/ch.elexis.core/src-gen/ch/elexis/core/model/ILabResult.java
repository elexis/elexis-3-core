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

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.elexis.core.types.PathologicDescription;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ILab Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ILabResult#getResult <em>Result</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getComment <em>Comment</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getReferenceMale <em>Reference Male</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getReferenceFemale <em>Reference Female</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getUnit <em>Unit</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getObservationTime <em>Observation Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getAnalyseTime <em>Analyse Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getTransmissionTime <em>Transmission Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#isPathologic <em>Pathologic</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getPathologicDescription <em>Pathologic Description</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getOrigin <em>Origin</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getItem <em>Item</em>}</li>
 *   <li>{@link ch.elexis.core.model.ILabResult#getPatient <em>Patient</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getILabResult()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ILabResult extends Deleteable, Identifiable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Unit()
	 * @model
	 * @generated
	 */
	String getUnit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getUnit <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(String value);

	/**
	 * Returns the value of the '<em><b>Analyse Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Analyse Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Analyse Time</em>' attribute.
	 * @see #setAnalyseTime(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_AnalyseTime()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getAnalyseTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getAnalyseTime <em>Analyse Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Analyse Time</em>' attribute.
	 * @see #getAnalyseTime()
	 * @generated
	 */
	void setAnalyseTime(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Observation Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Observation Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Time</em>' attribute.
	 * @see #setObservationTime(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_ObservationTime()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getObservationTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getObservationTime <em>Observation Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Time</em>' attribute.
	 * @see #getObservationTime()
	 * @generated
	 */
	void setObservationTime(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Transmission Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Transmission Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transmission Time</em>' attribute.
	 * @see #setTransmissionTime(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_TransmissionTime()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getTransmissionTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getTransmissionTime <em>Transmission Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Transmission Time</em>' attribute.
	 * @see #getTransmissionTime()
	 * @generated
	 */
	void setTransmissionTime(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Pathologic</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pathologic</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pathologic</em>' attribute.
	 * @see #setPathologic(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Pathologic()
	 * @model
	 * @generated
	 */
	boolean isPathologic();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#isPathologic <em>Pathologic</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pathologic</em>' attribute.
	 * @see #isPathologic()
	 * @generated
	 */
	void setPathologic(boolean value);

	/**
	 * Returns the value of the '<em><b>Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Result</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Result</em>' attribute.
	 * @see #setResult(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Result()
	 * @model
	 * @generated
	 */
	String getResult();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getResult <em>Result</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Result</em>' attribute.
	 * @see #getResult()
	 * @generated
	 */
	void setResult(String value);

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

	/**
	 * Returns the value of the '<em><b>Reference Male</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reference Male</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reference Male</em>' attribute.
	 * @see #setReferenceMale(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_ReferenceMale()
	 * @model
	 * @generated
	 */
	String getReferenceMale();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getReferenceMale <em>Reference Male</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reference Male</em>' attribute.
	 * @see #getReferenceMale()
	 * @generated
	 */
	void setReferenceMale(String value);

	/**
	 * Returns the value of the '<em><b>Reference Female</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reference Female</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reference Female</em>' attribute.
	 * @see #setReferenceFemale(String)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_ReferenceFemale()
	 * @model
	 * @generated
	 */
	String getReferenceFemale();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getReferenceFemale <em>Reference Female</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reference Female</em>' attribute.
	 * @see #getReferenceFemale()
	 * @generated
	 */
	void setReferenceFemale(String value);

	/**
	 * Returns the value of the '<em><b>Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Date</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Date</em>' attribute.
	 * @see #setDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Date()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Item</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Item</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Item</em>' reference.
	 * @see #setItem(ILabItem)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Item()
	 * @model
	 * @generated
	 */
	ILabItem getItem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getItem <em>Item</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Item</em>' reference.
	 * @see #getItem()
	 * @generated
	 */
	void setItem(ILabItem value);

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
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Patient()
	 * @model
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getPatient <em>Patient</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient</em>' reference.
	 * @see #getPatient()
	 * @generated
	 */
	void setPatient(IPatient value);

	/**
	 * Returns the value of the '<em><b>Pathologic Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pathologic Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pathologic Description</em>' attribute.
	 * @see #setPathologicDescription(PathologicDescription)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_PathologicDescription()
	 * @model dataType="ch.elexis.core.types.PathologicDescription"
	 * @generated
	 */
	PathologicDescription getPathologicDescription();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getPathologicDescription <em>Pathologic Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pathologic Description</em>' attribute.
	 * @see #getPathologicDescription()
	 * @generated
	 */
	void setPathologicDescription(PathologicDescription value);

	/**
	 * Returns the value of the '<em><b>Origin</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Origin</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Origin</em>' reference.
	 * @see #setOrigin(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getILabResult_Origin()
	 * @model
	 * @generated
	 */
	IContact getOrigin();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ILabResult#getOrigin <em>Origin</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Origin</em>' reference.
	 * @see #getOrigin()
	 * @generated
	 */
	void setOrigin(IContact value);

	/**
	 * @return the {@link ILabOrder} linked to this {@link ILabResult} or <code>null</code>
	 * @since 3.5
	 */
	ILabOrder getLabOrder();
	

} // ILabResult
