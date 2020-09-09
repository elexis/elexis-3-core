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
import java.time.LocalTime;

import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.services.IAppointmentService;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IAppointment
 * Series</b></em>'. <br/>
 * <b>It is a non persistent adapter for at least one persistent {@link IAppointment} instance.</b>
 * <br/>
 * It provides access to properties defining a whole series of {@link IAppointment} instances.
 * Creation methods are provided by the {@link IAppointmentService}.<!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesType <em>Series Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getEndingType <em>Ending Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartDate <em>Series Start Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartTime <em>Series Start Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndDate <em>Series End Date</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndTime <em>Series End Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getSeriesPatternString <em>Series Pattern String</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getEndingPatternString <em>Ending Pattern String</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#isPersistent <em>Persistent</em>}</li>
 *   <li>{@link ch.elexis.core.model.IAppointmentSeries#getRootAppointment <em>Root Appointment</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IAppointmentSeries extends IAppointment {
	/**
	 * Returns the value of the '<em><b>Series Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series Type</em>' attribute.
	 * @see #setSeriesType(SeriesType)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesType()
	 * @model dataType="ch.elexis.core.types.SeriesType"
	 * @generated
	 */
	SeriesType getSeriesType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesType <em>Series Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series Type</em>' attribute.
	 * @see #getSeriesType()
	 * @generated
	 */
	void setSeriesType(SeriesType value);

	/**
	 * Returns the value of the '<em><b>Ending Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ending Type</em>' attribute.
	 * @see #setEndingType(EndingType)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_EndingType()
	 * @model dataType="ch.elexis.core.types.EndingType"
	 * @generated
	 */
	EndingType getEndingType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getEndingType <em>Ending Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ending Type</em>' attribute.
	 * @see #getEndingType()
	 * @generated
	 */
	void setEndingType(EndingType value);

	/**
	 * Returns the value of the '<em><b>Series Start Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series Start Date</em>' attribute.
	 * @see #setSeriesStartDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesStartDate()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getSeriesStartDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartDate <em>Series Start Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series Start Date</em>' attribute.
	 * @see #getSeriesStartDate()
	 * @generated
	 */
	void setSeriesStartDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Series Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series Start Time</em>' attribute.
	 * @see #setSeriesStartTime(LocalTime)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesStartTime()
	 * @model dataType="ch.elexis.core.types.LocalTime"
	 * @generated
	 */
	LocalTime getSeriesStartTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesStartTime <em>Series Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series Start Time</em>' attribute.
	 * @see #getSeriesStartTime()
	 * @generated
	 */
	void setSeriesStartTime(LocalTime value);

	/**
	 * Returns the value of the '<em><b>Series End Date</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series End Date</em>' attribute.
	 * @see #setSeriesEndDate(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesEndDate()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 * @generated
	 */
	LocalDate getSeriesEndDate();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndDate <em>Series End Date</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series End Date</em>' attribute.
	 * @see #getSeriesEndDate()
	 * @generated
	 */
	void setSeriesEndDate(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Series End Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series End Time</em>' attribute.
	 * @see #setSeriesEndTime(LocalTime)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesEndTime()
	 * @model dataType="ch.elexis.core.types.LocalTime"
	 * @generated
	 */
	LocalTime getSeriesEndTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesEndTime <em>Series End Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series End Time</em>' attribute.
	 * @see #getSeriesEndTime()
	 * @generated
	 */
	void setSeriesEndTime(LocalTime value);

	/**
	 * Returns the value of the '<em><b>Series Pattern String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Series Pattern String</em>' attribute.
	 * @see #setSeriesPatternString(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_SeriesPatternString()
	 * @model
	 * @generated
	 */
	String getSeriesPatternString();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getSeriesPatternString <em>Series Pattern String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Series Pattern String</em>' attribute.
	 * @see #getSeriesPatternString()
	 * @generated
	 */
	void setSeriesPatternString(String value);

	/**
	 * Returns the value of the '<em><b>Ending Pattern String</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ending Pattern String</em>' attribute.
	 * @see #setEndingPatternString(String)
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_EndingPatternString()
	 * @model
	 * @generated
	 */
	String getEndingPatternString();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IAppointmentSeries#getEndingPatternString <em>Ending Pattern String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ending Pattern String</em>' attribute.
	 * @see #getEndingPatternString()
	 * @generated
	 */
	void setEndingPatternString(String value);

	/**
	 * Returns the value of the '<em><b>Persistent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Persistent</em>' attribute.
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_Persistent()
	 * @model changeable="false"
	 * @generated
	 */
	boolean isPersistent();

	/**
	 * Returns the value of the '<em><b>Root Appointment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Root Appointment</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIAppointmentSeries_RootAppointment()
	 * @model changeable="false"
	 * @generated
	 */
	IAppointment getRootAppointment();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getAsSeriesExtension();

} // IAppointmentSeries
