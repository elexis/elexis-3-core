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

import ch.rgw.tools.VersionedResource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IEncounter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IEncounter#getTimeStamp <em>Time Stamp</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getDate <em>Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#isBillable <em>Billable</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getPatient <em>Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getMandator <em>Mandator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getBilled <em>Billed</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getDiagnoses <em>Diagnoses</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getCoverage <em>Coverage</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getVersionedEntry <em>Versioned Entry</em>}</li>
 *   <li>{@link ch.elexis.core.model.IEncounter#getInvoice <em>Invoice</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIEncounter()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IEncounter extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Time Stamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time Stamp</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time Stamp</em>' attribute.
	 * @see #setTimeStamp(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_TimeStamp()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getTimeStamp();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getTimeStamp <em>Time Stamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time Stamp</em>' attribute.
	 * @see #getTimeStamp()
	 * @generated
	 */
	void setTimeStamp(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Patient</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Patient()
	 * @model changeable="false"
	 * @generated
	 */
	IPatient getPatient();

	/**
	 * Returns the value of the '<em><b>Coverage</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Coverage</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Coverage</em>' reference.
	 * @see #setCoverage(ICoverage)
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Coverage()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='fall'"
	 * @generated
	 */
	ICoverage getCoverage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getCoverage <em>Coverage</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Coverage</em>' reference.
	 * @see #getCoverage()
	 * @generated
	 */
	void setCoverage(ICoverage value);

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
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Mandator()
	 * @model
	 * @generated
	 */
	IMandator getMandator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getMandator <em>Mandator</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mandator</em>' reference.
	 * @see #getMandator()
	 * @generated
	 */
	void setMandator(IMandator value);

	/**
	 * Returns the value of the '<em><b>Billed</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IBilled}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billed</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billed</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Billed()
	 * @model
	 * @generated
	 */
	List<IBilled> getBilled();

	/**
	 * Returns the value of the '<em><b>Diagnoses</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IDiagnosisReference}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Diagnoses</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Diagnoses</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Diagnoses()
	 * @model
	 * @generated
	 */
	List<IDiagnosisReference> getDiagnoses();

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
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Date()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getDate <em>Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Date</em>' attribute.
	 * @see #getDate()
	 * @generated
	 */
	void setDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Billable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Billable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Billable</em>' attribute.
	 * @see #setBillable(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Billable()
	 * @model
	 * @generated
	 */
	boolean isBillable();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#isBillable <em>Billable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Billable</em>' attribute.
	 * @see #isBillable()
	 * @generated
	 */
	void setBillable(boolean value);

	/**
	 * Returns the value of the '<em><b>Versioned Entry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Versioned Entry</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Versioned Entry</em>' attribute.
	 * @see #setVersionedEntry(VersionedResource)
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_VersionedEntry()
	 * @model dataType="ch.elexis.core.types.VersionedResource"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='eintrag'"
	 * @generated
	 */
	VersionedResource getVersionedEntry();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getVersionedEntry <em>Versioned Entry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Versioned Entry</em>' attribute.
	 * @see #getVersionedEntry()
	 * @generated
	 */
	void setVersionedEntry(VersionedResource value);

	/**
	 * Returns the value of the '<em><b>Invoice</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Invoice</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Invoice</em>' reference.
	 * @see #setInvoice(IInvoice)
	 * @see ch.elexis.core.model.ModelPackage#getIEncounter_Invoice()
	 * @model
	 * @generated
	 */
	IInvoice getInvoice();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IEncounter#getInvoice <em>Invoice</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Invoice</em>' reference.
	 * @see #getInvoice()
	 * @generated
	 */
	void setInvoice(IInvoice value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model diagnosisRequired="true"
	 * @generated
	 */
	void addDiagnosis(IDiagnosis diagnosis);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model diagnosisRequired="true"
	 * @generated
	 */
	void removeDiagnosis(IDiagnosis diagnosis);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model billedRequired="true"
	 * @generated
	 */
	void removeBilled(IBilled billed);

} // IEncounter
