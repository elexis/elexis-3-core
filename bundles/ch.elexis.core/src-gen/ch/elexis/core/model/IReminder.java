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

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;

import java.time.LocalDate;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IReminder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IReminder#getCreator <em>Creator</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getResponsible <em>Responsible</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getContact <em>Contact</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getDue <em>Due</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getStatus <em>Status</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getVisibility <em>Visibility</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getSubject <em>Subject</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getMessage <em>Message</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getPriority <em>Priority</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getType <em>Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#isResponsibleAll <em>Responsible All</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminder#getGroup <em>Group</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIReminder()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IReminder extends Deleteable, Identifiable, WithExtInfo {
	/**
	 * Returns the value of the '<em><b>Creator</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Creator</em>' reference.
	 * @see #setCreator(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Creator()
	 * @model
	 * @generated
	 */
	IContact getCreator();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getCreator <em>Creator</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Creator</em>' reference.
	 * @see #getCreator()
	 * @generated
	 */
	void setCreator(IContact value);

	/**
	 * Returns the value of the '<em><b>Responsible</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IContact}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Responsible</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Responsible()
	 * @model
	 * @generated
	 */
	List<IContact> getResponsible();

	/**
	 * Returns the value of the '<em><b>Contact</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contact</em>' reference.
	 * @see #setContact(IContact)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Contact()
	 * @model annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='kontakt'"
	 * @generated
	 */
	IContact getContact();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getContact <em>Contact</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contact</em>' reference.
	 * @see #getContact()
	 * @generated
	 */
	void setContact(IContact value);

	/**
	 * Returns the value of the '<em><b>Due</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Due</em>' attribute.
	 * @see #setDue(LocalDate)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Due()
	 * @model dataType="ch.elexis.core.types.LocalDate"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='dateDue'"
	 * @generated
	 */
	LocalDate getDue();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getDue <em>Due</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Due</em>' attribute.
	 * @see #getDue()
	 * @generated
	 */
	void setDue(LocalDate value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see #setStatus(ProcessStatus)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Status()
	 * @model dataType="ch.elexis.core.types.ProcessStatus"
	 * @generated
	 */
	ProcessStatus getStatus();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ProcessStatus value);

	/**
	 * Returns the value of the '<em><b>Visibility</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visibility</em>' attribute.
	 * @see #setVisibility(Visibility)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Visibility()
	 * @model dataType="ch.elexis.core.types.Visibility"
	 * @generated
	 */
	Visibility getVisibility();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getVisibility <em>Visibility</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visibility</em>' attribute.
	 * @see #getVisibility()
	 * @generated
	 */
	void setVisibility(Visibility value);

	/**
	 * Returns the value of the '<em><b>Subject</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Subject</em>' attribute.
	 * @see #setSubject(String)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Subject()
	 * @model
	 * @generated
	 */
	String getSubject();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getSubject <em>Subject</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Subject</em>' attribute.
	 * @see #getSubject()
	 * @generated
	 */
	void setSubject(String value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Priority</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Priority</em>' attribute.
	 * @see #setPriority(Priority)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Priority()
	 * @model dataType="ch.elexis.core.types.Priority"
	 * @generated
	 */
	Priority getPriority();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getPriority <em>Priority</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Priority</em>' attribute.
	 * @see #getPriority()
	 * @generated
	 */
	void setPriority(Priority value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(Type)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Type()
	 * @model dataType="ch.elexis.core.types.Type"
	 * @generated
	 */
	Type getType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(Type value);

	/**
	 * Returns the value of the '<em><b>Responsible All</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Responsible All</em>' attribute.
	 * @see #setResponsibleAll(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_ResponsibleAll()
	 * @model
	 * @generated
	 */
	boolean isResponsibleAll();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#isResponsibleAll <em>Responsible All</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Responsible All</em>' attribute.
	 * @see #isResponsibleAll()
	 * @generated
	 */
	void setResponsibleAll(boolean value);

	/**
	 * Returns the value of the '<em><b>Group</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group</em>' reference.
	 * @see #setGroup(IUserGroup)
	 * @see ch.elexis.core.model.ModelPackage#getIReminder_Group()
	 * @model
	 * @generated
	 */
	IUserGroup getGroup();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IReminder#getGroup <em>Group</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group</em>' reference.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(IUserGroup value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model responsibleRequired="true"
	 * @generated
	 */
	void addResponsible(IContact responsible);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model responsibleRequired="true"
	 * @generated
	 */
	void removeResponsible(IContact responsible);

} // IReminder
