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
 * A representation of the model object '<em><b>IAppointment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IAppointment#getReason <em>Reason</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getState <em>State</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getType <em>Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getDurationMinutes <em>Duration Minutes</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getSchedule <em>Schedule</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getSubjectOrPatient <em>Subject Or Patient</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getPriority <em>Priority</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getTreatmentReason <em>Treatment Reason</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getCaseType <em>Case Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getInsuranceType <em>Insurance Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getLinkgroup <em>Linkgroup</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getExtension <em>Extension</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getCreated <em>Created</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getLastEdit <em>Last Edit</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getStateHistory <em>State History</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#isRecurring <em>Recurring</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getCreatedBy <em>Created By</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#isLocked <em>Locked</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIAppointment()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IAppointment extends IPeriod {
	/**
	 * Returns the value of the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Reason</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reason</em>' attribute.
	 * @see #setReason(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Reason()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='grund'"
	 * @generated
	 */
	String getReason();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getReason <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reason</em>' attribute.
	 * @see #getReason()
	 * @generated
	 */
	void setReason(String value);

	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' attribute.
	 * @see #setState(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_State()
	 * @model
	 * @generated
	 */
	String getState();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getState <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' attribute.
	 * @see #getState()
	 * @generated
	 */
	void setState(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Type()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='termintyp'"
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Duration Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Duration Minutes</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Duration Minutes</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_DurationMinutes()
	 * @model changeable="false"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='dauer'"
	 * @generated
	 */
	Integer getDurationMinutes();

	/**
	 * Returns the value of the '<em><b>Schedule</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Schedule</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Schedule</em>' attribute.
	 * @see #setSchedule(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Schedule()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='bereich'"
	 * @generated
	 */
	String getSchedule();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getSchedule <em>Schedule</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Schedule</em>' attribute.
	 * @see #getSchedule()
	 * @generated
	 */
	void setSchedule(String value);

	/**
	 * Returns the value of the '<em><b>Created By</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Created By</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created By</em>' attribute.
	 * @see #setCreatedBy(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_CreatedBy()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='erstelltvon'"
	 * @generated
	 */
	String getCreatedBy();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getCreatedBy <em>Created By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created By</em>' attribute.
	 * @see #getCreatedBy()
	 * @generated
	 */
	void setCreatedBy(String value);

	/**
	 * Returns the value of the '<em><b>Locked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Locked</em>' attribute.
	 * @see #setLocked(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Locked()
	 * @model
	 * @generated
	 */
	boolean isLocked();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#isLocked <em>Locked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Locked</em>' attribute.
	 * @see #isLocked()
	 * @generated
	 */
	void setLocked(boolean value);

	/**
	 * Returns the value of the '<em><b>Subject Or Patient</b></em>' attribute.
	 * <!-- begin-user-doc
	 * -->
	 * <p>
	 * Get the subject text, or the label of the patient.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Subject Or Patient</em>' attribute.
	 * @see #setSubjectOrPatient(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_SubjectOrPatient()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='patid'"
	 * @generated
	 */
	String getSubjectOrPatient();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getSubjectOrPatient <em>Subject Or Patient</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Set the subject text, or the id of the patient.
	 * </p>
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Subject Or Patient</em>' attribute.
	 * @see #getSubjectOrPatient()
	 * @generated
	 */
	void setSubjectOrPatient(String value);

	/**
	 * Returns the value of the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Priority</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Priority</em>' attribute.
	 * @see #setPriority(int)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Priority()
	 * @model
	 * @generated
	 */
	int getPriority();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getPriority <em>Priority</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Priority</em>' attribute.
	 * @see #getPriority()
	 * @generated
	 */
	void setPriority(int value);

	/**
	 * Returns the value of the '<em><b>Treatment Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Treatment Reason</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Enumerated reason why this appointment was created. Currently used by RH connector
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Treatment Reason</em>' attribute.
	 * @see #setTreatmentReason(int)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_TreatmentReason()
	 * @model
	 * @generated
	 */
	int getTreatmentReason();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getTreatmentReason <em>Treatment Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Treatment Reason</em>' attribute.
	 * @see #getTreatmentReason()
	 * @generated
	 */
	void setTreatmentReason(int value);

	/**
	 * Returns the value of the '<em><b>Case Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Enumerated case type of this appointment. Currently used by RH connector
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Case Type</em>' attribute.
	 * @see #setCaseType(int)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_CaseType()
	 * @model
	 * @generated
	 */
	int getCaseType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getCaseType <em>Case Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Case Type</em>' attribute.
	 * @see #getCaseType()
	 * @generated
	 */
	void setCaseType(int value);

	/**
	 * Returns the value of the '<em><b>Insurance Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Enumerated insurance type of this appointment. Currently used by RH connector
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Insurance Type</em>' attribute.
	 * @see #setInsuranceType(int)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_InsuranceType()
	 * @model
	 * @generated
	 */
	int getInsuranceType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getInsuranceType <em>Insurance Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Insurance Type</em>' attribute.
	 * @see #getInsuranceType()
	 * @generated
	 */
	void setInsuranceType(int value);

	/**
	 * Returns the value of the '<em><b>Linkgroup</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Linkgroup</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Linkgroup</em>' attribute.
	 * @see #setLinkgroup(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Linkgroup()
	 * @model
	 * @generated
	 */
	String getLinkgroup();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getLinkgroup <em>Linkgroup</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Linkgroup</em>' attribute.
	 * @see #getLinkgroup()
	 * @generated
	 */
	void setLinkgroup(String value);

	/**
	 * Returns the value of the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extension</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extension</em>' attribute.
	 * @see #setExtension(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Extension()
	 * @model
	 * @generated
	 */
	String getExtension();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getExtension <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extension</em>' attribute.
	 * @see #getExtension()
	 * @generated
	 */
	void setExtension(String value);

	/**
	 * Returns the value of the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created</em>' attribute.
	 * @see #setCreated(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Created()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='angelegt'"
	 * @generated
	 */
	String getCreated();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getCreated <em>Created</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created</em>' attribute.
	 * @see #getCreated()
	 * @generated
	 */
	void setCreated(String value);

	/**
	 * Returns the value of the '<em><b>Last Edit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Edit</em>' attribute.
	 * @see #setLastEdit(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_LastEdit()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='lastedit'"
	 * @generated
	 */
	String getLastEdit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getLastEdit <em>Last Edit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Edit</em>' attribute.
	 * @see #getLastEdit()
	 * @generated
	 */
	void setLastEdit(String value);

	/**
	 * Returns the value of the '<em><b>State History</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State History</em>' attribute.
	 * @see #setStateHistory(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_StateHistory()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='statusHistory'"
	 * @generated
	 */
	String getStateHistory();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getStateHistory <em>State History</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State History</em>' attribute.
	 * @see #getStateHistory()
	 * @generated
	 */
	void setStateHistory(String value);

	/**
	 * Returns the value of the '<em><b>Recurring</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Recurring</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Recurring()
	 * @model changeable="false"
	 * @generated
	 */
	boolean isRecurring();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	IContact getContact();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	String getStateHistoryFormatted(String formatPattern);

} // IAppointment
