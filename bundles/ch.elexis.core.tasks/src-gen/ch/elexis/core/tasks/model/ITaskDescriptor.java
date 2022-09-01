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

import java.io.Serializable;
import java.util.Map;

import com.cronutils.model.Cron;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ITask Descriptor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Describes an runnable to be started with a given runContext under the rigths of a user when triggerEvent occurs.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getId <em>Id</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getReferenceId <em>Reference Id</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwner <em>Owner</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwnerNotification <em>Owner Notification</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#isActive <em>Active</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getIdentifiedRunnableId <em>Identified Runnable Id</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunContext <em>Run Context</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerType <em>Trigger Type</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerParameters <em>Trigger Parameters</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunner <em>Runner</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#isSingleton <em>Singleton</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#isSystem <em>System</em>}</li>
 *   <li>{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTransientData <em>Transient Data</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ITaskDescriptor extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_Id()
	 * @model id="true" required="true" changeable="false"
	 * @generated
	 */
	@Override
	String getId();

	/**
	 * Returns the value of the '<em><b>Reference Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Used to reference this task in an other_task_state trigger event of a secondary task
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Reference Id</em>' attribute.
	 * @see #setReferenceId(String)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_ReferenceId()
	 * @model
	 * @generated
	 */
	String getReferenceId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getReferenceId <em>Reference Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reference Id</em>' attribute.
	 * @see #getReferenceId()
	 * @generated
	 */
	void setReferenceId(String value);

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owner</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owner</em>' reference.
	 * @see #setOwner(IUser)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_Owner()
	 * @model required="true"
	 * @generated
	 */
	IUser getOwner();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwner <em>Owner</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' reference.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(IUser value);

	/**
	 * Returns the value of the '<em><b>Owner Notification</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.tasks.model.OwnerTaskNotification}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Owner Notification</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owner Notification</em>' attribute.
	 * @see ch.elexis.core.tasks.model.OwnerTaskNotification
	 * @see #setOwnerNotification(OwnerTaskNotification)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_OwnerNotification()
	 * @model default="0" required="true"
	 * @generated
	 */
	OwnerTaskNotification getOwnerNotification();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getOwnerNotification <em>Owner Notification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner Notification</em>' attribute.
	 * @see ch.elexis.core.tasks.model.OwnerTaskNotification
	 * @see #getOwnerNotification()
	 * @generated
	 */
	void setOwnerNotification(OwnerTaskNotification value);

	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * if true run if triggerEvent happens
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_Active()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isActive();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#isActive <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Identified Runnable Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Identified Runnable Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Identified Runnable Id</em>' attribute.
	 * @see #setIdentifiedRunnableId(String)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_IdentifiedRunnableId()
	 * @model required="true"
	 *        annotation="http://elexis.info/jpa/entity/attribute/mapping attributeName='runnableId'"
	 * @generated
	 */
	String getIdentifiedRunnableId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getIdentifiedRunnableId <em>Identified Runnable Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Identified Runnable Id</em>' attribute.
	 * @see #getIdentifiedRunnableId()
	 * @generated
	 */
	void setIdentifiedRunnableId(String value);

	/**
	 * Returns the value of the '<em><b>Run Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The context parameters to pass to ITask on being instantiated to a Job
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Run Context</em>' attribute.
	 * @see #setRunContext(Map)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_RunContext()
	 * @model transient="true"
	 * @generated
	 */
	Map<String, Serializable> getRunContext();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunContext <em>Run Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Run Context</em>' attribute.
	 * @see #getRunContext()
	 * @generated
	 */
	void setRunContext(Map<String, Serializable> value);

	/**
	 * Returns the value of the '<em><b>Trigger Type</b></em>' attribute.
	 * The default value is <code>"MANUAL"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.tasks.model.TaskTriggerType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The event type that will, in combination with the triggerEventCondition, lead to execution
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trigger Type</em>' attribute.
	 * @see ch.elexis.core.tasks.model.TaskTriggerType
	 * @see #setTriggerType(TaskTriggerType)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_TriggerType()
	 * @model default="MANUAL"
	 * @generated
	 */
	TaskTriggerType getTriggerType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerType <em>Trigger Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Trigger Type</em>' attribute.
	 * @see ch.elexis.core.tasks.model.TaskTriggerType
	 * @see #getTriggerType()
	 * @generated
	 */
	void setTriggerType(TaskTriggerType value);

	/**
	 * Returns the value of the '<em><b>Trigger Parameters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Trigger Parameters</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Trigger Parameters</em>' attribute.
	 * @see #setTriggerParameters(Map)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_TriggerParameters()
	 * @model transient="true"
	 * @generated
	 */
	Map<String, String> getTriggerParameters();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getTriggerParameters <em>Trigger Parameters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Trigger Parameters</em>' attribute.
	 * @see #getTriggerParameters()
	 * @generated
	 */
	void setTriggerParameters(Map<String, String> value);

	/**
	 * Returns the value of the '<em><b>Runner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The station that will run this task. Valid values are empty, stationId or ELEXIS-SERVER. Empty defines that any station may run this task.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Runner</em>' attribute.
	 * @see #setRunner(String)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_Runner()
	 * @model
	 * @generated
	 */
	String getRunner();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#getRunner <em>Runner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Runner</em>' attribute.
	 * @see #getRunner()
	 * @generated
	 */
	void setRunner(String value);

	/**
	 * Returns the value of the '<em><b>Singleton</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether concurrent executions of an instantiation of this ITaskDescriptor are allowed. Even if this is set to false, if the reference IIdentifiedRunnable is singleton, it will be enforced to be true on execution.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Singleton</em>' attribute.
	 * @see #setSingleton(boolean)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_Singleton()
	 * @model default="false"
	 * @generated
	 */
	boolean isSingleton();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#isSingleton <em>Singleton</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Singleton</em>' attribute.
	 * @see #isSingleton()
	 * @generated
	 */
	void setSingleton(boolean value);

	/**
	 * Returns the value of the '<em><b>System</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * If true this task descriptor was programmatically generated and is relevant to the operational state of Elexis. Modification or deletion of this task descriptor and its derived task log entries leads to change of productive data.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>System</em>' attribute.
	 * @see #setSystem(boolean)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_System()
	 * @model
	 * @generated
	 */
	boolean isSystem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITaskDescriptor#isSystem <em>System</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' attribute.
	 * @see #isSystem()
	 * @generated
	 */
	void setSystem(boolean value);

	/**
	 * Returns the value of the '<em><b>Transient Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Used to transport internal data, not stored into table. (e.g. next execution time of cron triggered task descriptor)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Transient Data</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITaskDescriptor_TransientData()
	 * @model required="true" transient="true" changeable="false"
	 * @generated
	 */
	Map<String, Object> getTransientData();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setTriggerParameter(String key, String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Convenience method to set a single value of the runContext. 
	 * <!-- end-model-doc -->
	 * @model valueDataType="ch.elexis.core.tasks.model.Serializable"
	 * @generated
	 */
	void setRunContextParameter(String key, Serializable value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * If TaskTriggerType.CRON this returns the cron configuration string as a parsed Cron object, else null
	 * <!-- end-model-doc -->
	 * @model kind="operation" dataType="ch.elexis.core.tasks.model.Cron"
	 * @generated
	 */
	Cron getCronTriggerTypeConfiguration();

} // ITaskDescriptor
