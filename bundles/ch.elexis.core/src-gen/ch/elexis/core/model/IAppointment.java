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
 *   <li>{@link ch.elexis.core.model.IAppointment#getStart <em>Start</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getEnd <em>End</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getDurationMinutes <em>Duration Minutes</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getSchedule <em>Schedule</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getCreatedBy <em>Created By</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointment#getSubjectOrPatient <em>Subject Or Patient</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIAppointment()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IAppointment extends Identifiable, Deleteable {
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
	 * Returns the value of the '<em><b>Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Start</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start</em>' attribute.
	 * @see #setStart(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_Start()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getStart();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getStart <em>Start</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start</em>' attribute.
	 * @see #getStart()
	 * @generated
	 */
	void setStart(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>End</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End</em>' attribute.
	 * @see #setEnd(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_End()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getEnd();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getEnd <em>End</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End</em>' attribute.
	 * @see #getEnd()
	 * @generated
	 */
	void setEnd(LocalDateTime value);

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
	 * Returns the value of the '<em><b>Created By</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Created By</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created By</em>' reference.
	 * @see #setCreatedBy(IUser)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointment_CreatedBy()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='erstelltvon'"
	 * @generated
	 */
	IUser getCreatedBy();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointment#getCreatedBy <em>Created By</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created By</em>' reference.
	 * @see #getCreatedBy()
	 * @generated
	 */
	void setCreatedBy(IUser value);

	/**
	 * Returns the value of the '<em><b>Subject Or Patient</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Subject Or Patient</em>' attribute isn't clear,
	 * there really should be more of a description here...
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
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Subject Or Patient</em>' attribute.
	 * @see #getSubjectOrPatient()
	 * @generated
	 */
	void setSubjectOrPatient(String value);

} // IAppointment
