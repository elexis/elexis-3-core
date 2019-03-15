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
package ch.elexis.core.tasks.model;

import ch.elexis.core.model.IUser;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ITask Service</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskService()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ITaskService {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model exceptions="ch.elexis.core.tasks.model.TaskException" runnableDataType="ch.elexis.core.tasks.model.IIdentifiedRunnable"
	 * @generated
	 */
	ITaskDescriptor createTaskDescriptor(IUser owner, IIdentifiedRunnable runnable) throws TaskException;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @param runContext key value pairs to add to the task run context already populated out of ITaskDescriptor#runContext (e.g. the file system trigger adds the file triggering the event)
	 * <!-- end-model-doc -->
	 * @model exceptions="ch.elexis.core.tasks.model.TaskException" taskDescriptorRequired="true" progressMonitorDataType="ch.elexis.core.tasks.model.IProgressMonitor"
	 * @generated
	 */
	ITask trigger(ITaskDescriptor taskDescriptor, IProgressMonitor progressMonitor, TaskTriggerType trigger, Map<String, String> runContext) throws TaskException;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model dataType="ch.elexis.core.tasks.model.IIdentifiedRunnable" exceptions="ch.elexis.core.tasks.model.TaskException" runnableIdRequired="true"
	 * @generated
	 */
	IIdentifiedRunnable instantiateRunnableById(String runnableId) throws TaskException;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model exceptions="ch.elexis.core.tasks.model.TaskException" taskDescriptorRequired="true"
	 * @generated
	 */
	void saveTaskDescriptor(ITaskDescriptor taskDescriptor) throws TaskException;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * activates / deactivates triggering the provided ITaskDescriptor
	 * <!-- end-model-doc -->
	 * @model exceptions="ch.elexis.core.tasks.model.TaskException"
	 * @generated
	 */
	void setActive(ITaskDescriptor taskDescriptor, boolean active) throws TaskException;

} // ITaskService
