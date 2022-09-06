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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IReminder Responsible Link</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IReminderResponsibleLink#getReminder <em>Reminder</em>}</li>
 *   <li>{@link ch.elexis.core.model.IReminderResponsibleLink#getResponsible <em>Responsible</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIReminderResponsibleLink()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IReminderResponsibleLink extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Reminder</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reminder</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIReminderResponsibleLink_Reminder()
	 * @model changeable="false"
	 * @generated
	 */
	IReminder getReminder();

	/**
	 * Returns the value of the '<em><b>Responsible</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Responsible</em>' reference.
	 * @see ch.elexis.core.model.ModelPackage#getIReminderResponsibleLink_Responsible()
	 * @model changeable="false"
	 * @generated
	 */
	IContact getResponsible();

} // IReminderResponsibleLink
