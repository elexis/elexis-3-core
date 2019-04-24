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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ch.elexis.core.tasks.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://ch.elexis.core/tasks/model";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ch.elexis.core.tasks";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = ch.elexis.core.tasks.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.ITaskDescriptor <em>ITask Descriptor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITaskDescriptor()
	 * @generated
	 */
	int ITASK_DESCRIPTOR = 0;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__DELETED = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__ID = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Reference Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__REFERENCE_ID = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__OWNER = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Owner Notification</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__OWNER_NOTIFICATION = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__ACTIVE = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Identified Runnable Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__IDENTIFIED_RUNNABLE_ID = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Run Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__RUN_CONTEXT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Trigger Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__TRIGGER_TYPE = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Trigger Parameters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__TRIGGER_PARAMETERS = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Runner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__RUNNER = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Singleton</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR__SINGLETON = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>ITask Descriptor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_DESCRIPTOR_FEATURE_COUNT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 12;

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.ITask <em>ITask</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.ITask
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITask()
	 * @generated
	 */
	int ITASK = 1;

	/**
	 * The feature id for the '<em><b>Deleted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__DELETED = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__ID = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>State</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__STATE = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Trigger Event</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__TRIGGER_EVENT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__RESULT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Progress Monitor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__PROGRESS_MONITOR = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Run Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__RUN_CONTEXT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Finished</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__FINISHED = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Descriptor Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK__DESCRIPTOR_ID = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>ITask</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_FEATURE_COUNT = ch.elexis.core.model.ModelPackage.IDENTIFIABLE_FEATURE_COUNT + 9;

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.ITaskService <em>ITask Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.ITaskService
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITaskService()
	 * @generated
	 */
	int ITASK_SERVICE = 2;

	/**
	 * The number of structural features of the '<em>ITask Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITASK_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.TaskTriggerType <em>Task Trigger Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.TaskTriggerType
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskTriggerType()
	 * @generated
	 */
	int TASK_TRIGGER_TYPE = 3;

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.TaskState <em>Task State</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.TaskState
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskState()
	 * @generated
	 */
	int TASK_STATE = 4;

	/**
	 * The meta object id for the '{@link ch.elexis.core.tasks.model.OwnerTaskNotification <em>Owner Task Notification</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.tasks.model.OwnerTaskNotification
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getOwnerTaskNotification()
	 * @generated
	 */
	int OWNER_TASK_NOTIFICATION = 5;

	/**
	 * The meta object id for the '<em>Task Exception</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.tasks.TaskException
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskException()
	 * @generated
	 */
	int TASK_EXCEPTION = 6;

	/**
	 * The meta object id for the '<em>IIdentified Runnable</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.model.tasks.IIdentifiedRunnable
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getIIdentifiedRunnable()
	 * @generated
	 */
	int IIDENTIFIED_RUNNABLE = 7;

	/**
	 * The meta object id for the '<em>IProgress Monitor</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.core.runtime.IProgressMonitor
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getIProgressMonitor()
	 * @generated
	 */
	int IPROGRESS_MONITOR = 8;

	/**
	 * The meta object id for the '<em>Logger</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.slf4j.Logger
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getLogger()
	 * @generated
	 */
	int LOGGER = 9;

	/**
	 * The meta object id for the '<em>Serializable</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.Serializable
	 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getSerializable()
	 * @generated
	 */
	int SERIALIZABLE = 10;


	/**
	 * Returns the meta object for class '{@link ch.elexis.core.tasks.model.ITaskDescriptor <em>ITask Descriptor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ITask Descriptor</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor
	 * @generated
	 */
	EClass getITaskDescriptor();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getId()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_Id();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getReferenceId <em>Reference Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Id</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getReferenceId()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_ReferenceId();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwner <em>Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Owner</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getOwner()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EReference getITaskDescriptor_Owner();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwnerNotification <em>Owner Notification</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Owner Notification</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getOwnerNotification()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_OwnerNotification();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#isActive()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_Active();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getIdentifiedRunnableId <em>Identified Runnable Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Identified Runnable Id</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getIdentifiedRunnableId()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_IdentifiedRunnableId();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunContext <em>Run Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Run Context</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getRunContext()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_RunContext();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerType <em>Trigger Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Trigger Type</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerType()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_TriggerType();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerParameters <em>Trigger Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Trigger Parameters</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerParameters()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_TriggerParameters();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunner <em>Runner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Runner</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#getRunner()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_Runner();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITaskDescriptor#isSingleton <em>Singleton</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Singleton</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskDescriptor#isSingleton()
	 * @see #getITaskDescriptor()
	 * @generated
	 */
	EAttribute getITaskDescriptor_Singleton();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.tasks.model.ITask <em>ITask</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ITask</em>'.
	 * @see ch.elexis.core.tasks.model.ITask
	 * @generated
	 */
	EClass getITask();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getId()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_Id();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getState <em>State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>State</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getState()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_State();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getTriggerEvent <em>Trigger Event</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Trigger Event</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getTriggerEvent()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_TriggerEvent();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getResult <em>Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Result</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getResult()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_Result();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getProgressMonitor <em>Progress Monitor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Progress Monitor</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getProgressMonitor()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_ProgressMonitor();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getRunContext <em>Run Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Run Context</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getRunContext()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_RunContext();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#isFinished <em>Finished</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Finished</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#isFinished()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_Finished();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.tasks.model.ITask#getDescriptorId <em>Descriptor Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Descriptor Id</em>'.
	 * @see ch.elexis.core.tasks.model.ITask#getDescriptorId()
	 * @see #getITask()
	 * @generated
	 */
	EAttribute getITask_DescriptorId();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.tasks.model.ITaskService <em>ITask Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ITask Service</em>'.
	 * @see ch.elexis.core.tasks.model.ITaskService
	 * @generated
	 */
	EClass getITaskService();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.tasks.model.TaskTriggerType <em>Task Trigger Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Task Trigger Type</em>'.
	 * @see ch.elexis.core.tasks.model.TaskTriggerType
	 * @generated
	 */
	EEnum getTaskTriggerType();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.tasks.model.TaskState <em>Task State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Task State</em>'.
	 * @see ch.elexis.core.tasks.model.TaskState
	 * @generated
	 */
	EEnum getTaskState();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.tasks.model.OwnerTaskNotification <em>Owner Task Notification</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Owner Task Notification</em>'.
	 * @see ch.elexis.core.tasks.model.OwnerTaskNotification
	 * @generated
	 */
	EEnum getOwnerTaskNotification();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.tasks.TaskException <em>Task Exception</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Task Exception</em>'.
	 * @see ch.elexis.core.model.tasks.TaskException
	 * @model instanceClass="ch.elexis.core.model.tasks.TaskException"
	 * @generated
	 */
	EDataType getTaskException();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.model.tasks.IIdentifiedRunnable <em>IIdentified Runnable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>IIdentified Runnable</em>'.
	 * @see ch.elexis.core.model.tasks.IIdentifiedRunnable
	 * @model instanceClass="ch.elexis.core.model.tasks.IIdentifiedRunnable"
	 * @generated
	 */
	EDataType getIIdentifiedRunnable();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.core.runtime.IProgressMonitor <em>IProgress Monitor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>IProgress Monitor</em>'.
	 * @see org.eclipse.core.runtime.IProgressMonitor
	 * @model instanceClass="org.eclipse.core.runtime.IProgressMonitor"
	 * @generated
	 */
	EDataType getIProgressMonitor();

	/**
	 * Returns the meta object for data type '{@link org.slf4j.Logger <em>Logger</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Logger</em>'.
	 * @see org.slf4j.Logger
	 * @model instanceClass="org.slf4j.Logger"
	 * @generated
	 */
	EDataType getLogger();

	/**
	 * Returns the meta object for data type '{@link java.io.Serializable <em>Serializable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Serializable</em>'.
	 * @see java.io.Serializable
	 * @model instanceClass="java.io.Serializable"
	 * @generated
	 */
	EDataType getSerializable();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.ITaskDescriptor <em>ITask Descriptor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.ITaskDescriptor
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITaskDescriptor()
		 * @generated
		 */
		EClass ITASK_DESCRIPTOR = eINSTANCE.getITaskDescriptor();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__ID = eINSTANCE.getITaskDescriptor_Id();

		/**
		 * The meta object literal for the '<em><b>Reference Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__REFERENCE_ID = eINSTANCE.getITaskDescriptor_ReferenceId();

		/**
		 * The meta object literal for the '<em><b>Owner</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ITASK_DESCRIPTOR__OWNER = eINSTANCE.getITaskDescriptor_Owner();

		/**
		 * The meta object literal for the '<em><b>Owner Notification</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__OWNER_NOTIFICATION = eINSTANCE.getITaskDescriptor_OwnerNotification();

		/**
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__ACTIVE = eINSTANCE.getITaskDescriptor_Active();

		/**
		 * The meta object literal for the '<em><b>Identified Runnable Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__IDENTIFIED_RUNNABLE_ID = eINSTANCE.getITaskDescriptor_IdentifiedRunnableId();

		/**
		 * The meta object literal for the '<em><b>Run Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__RUN_CONTEXT = eINSTANCE.getITaskDescriptor_RunContext();

		/**
		 * The meta object literal for the '<em><b>Trigger Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__TRIGGER_TYPE = eINSTANCE.getITaskDescriptor_TriggerType();

		/**
		 * The meta object literal for the '<em><b>Trigger Parameters</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__TRIGGER_PARAMETERS = eINSTANCE.getITaskDescriptor_TriggerParameters();

		/**
		 * The meta object literal for the '<em><b>Runner</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__RUNNER = eINSTANCE.getITaskDescriptor_Runner();

		/**
		 * The meta object literal for the '<em><b>Singleton</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK_DESCRIPTOR__SINGLETON = eINSTANCE.getITaskDescriptor_Singleton();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.ITask <em>ITask</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.ITask
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITask()
		 * @generated
		 */
		EClass ITASK = eINSTANCE.getITask();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__ID = eINSTANCE.getITask_Id();

		/**
		 * The meta object literal for the '<em><b>State</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__STATE = eINSTANCE.getITask_State();

		/**
		 * The meta object literal for the '<em><b>Trigger Event</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__TRIGGER_EVENT = eINSTANCE.getITask_TriggerEvent();

		/**
		 * The meta object literal for the '<em><b>Result</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__RESULT = eINSTANCE.getITask_Result();

		/**
		 * The meta object literal for the '<em><b>Progress Monitor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__PROGRESS_MONITOR = eINSTANCE.getITask_ProgressMonitor();

		/**
		 * The meta object literal for the '<em><b>Run Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__RUN_CONTEXT = eINSTANCE.getITask_RunContext();

		/**
		 * The meta object literal for the '<em><b>Finished</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__FINISHED = eINSTANCE.getITask_Finished();

		/**
		 * The meta object literal for the '<em><b>Descriptor Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITASK__DESCRIPTOR_ID = eINSTANCE.getITask_DescriptorId();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.ITaskService <em>ITask Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.ITaskService
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getITaskService()
		 * @generated
		 */
		EClass ITASK_SERVICE = eINSTANCE.getITaskService();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.TaskTriggerType <em>Task Trigger Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.TaskTriggerType
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskTriggerType()
		 * @generated
		 */
		EEnum TASK_TRIGGER_TYPE = eINSTANCE.getTaskTriggerType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.TaskState <em>Task State</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.TaskState
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskState()
		 * @generated
		 */
		EEnum TASK_STATE = eINSTANCE.getTaskState();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.tasks.model.OwnerTaskNotification <em>Owner Task Notification</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.tasks.model.OwnerTaskNotification
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getOwnerTaskNotification()
		 * @generated
		 */
		EEnum OWNER_TASK_NOTIFICATION = eINSTANCE.getOwnerTaskNotification();

		/**
		 * The meta object literal for the '<em>Task Exception</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.tasks.TaskException
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getTaskException()
		 * @generated
		 */
		EDataType TASK_EXCEPTION = eINSTANCE.getTaskException();

		/**
		 * The meta object literal for the '<em>IIdentified Runnable</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.model.tasks.IIdentifiedRunnable
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getIIdentifiedRunnable()
		 * @generated
		 */
		EDataType IIDENTIFIED_RUNNABLE = eINSTANCE.getIIdentifiedRunnable();

		/**
		 * The meta object literal for the '<em>IProgress Monitor</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.core.runtime.IProgressMonitor
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getIProgressMonitor()
		 * @generated
		 */
		EDataType IPROGRESS_MONITOR = eINSTANCE.getIProgressMonitor();

		/**
		 * The meta object literal for the '<em>Logger</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.slf4j.Logger
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getLogger()
		 * @generated
		 */
		EDataType LOGGER = eINSTANCE.getLogger();

		/**
		 * The meta object literal for the '<em>Serializable</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.Serializable
		 * @see ch.elexis.core.tasks.model.impl.ModelPackageImpl#getSerializable()
		 * @generated
		 */
		EDataType SERIALIZABLE = eINSTANCE.getSerializable();

	}

} //ModelPackage
