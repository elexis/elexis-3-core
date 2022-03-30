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

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>ITask</b></em>'. <!-- end-user-doc -->
 *
 * <!-- begin-model-doc --> A running instance of an ITaskDescriptor. <!--
 * end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getId <em>Id</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getState <em>State</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getTriggerEvent <em>Trigger
 * Event</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getCreatedAt <em>Created
 * At</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getRunAt <em>Run At</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getFinishedAt <em>Finished
 * At</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getResult <em>Result</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getProgressMonitor <em>Progress
 * Monitor</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getRunContext <em>Run
 * Context</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#isFinished
 * <em>Finished</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getTaskDescriptor <em>Task
 * Descriptor</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#getRunner <em>Runner</em>}</li>
 * <li>{@link ch.elexis.core.tasks.model.ITask#isSystem <em>System</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.tasks.model.ModelPackage#getITask()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ITask extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute. <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_Id()
	 * @model id="true" required="true" changeable="false"
	 * @generated
	 */
	String getId();

	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute. The literals are
	 * from the enumeration {@link ch.elexis.core.tasks.model.TaskState}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>State</em>' attribute isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>State</em>' attribute.
	 * @see ch.elexis.core.tasks.model.TaskState
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_State()
	 * @model required="true" changeable="false"
	 * @generated
	 */
	TaskState getState();

	/**
	 * Returns the value of the '<em><b>Trigger Event</b></em>' attribute. The
	 * literals are from the enumeration
	 * {@link ch.elexis.core.tasks.model.TaskTriggerType}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Trigger Event</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Trigger Event</em>' attribute.
	 * @see ch.elexis.core.tasks.model.TaskTriggerType
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_TriggerEvent()
	 * @model required="true" changeable="false"
	 * @generated
	 */
	TaskTriggerType getTriggerEvent();

	/**
	 * Returns the value of the '<em><b>Created At</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> the
	 * creation time <!-- end-model-doc -->
	 * 
	 * @return the value of the '<em>Created At</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_CreatedAt()
	 * @model dataType="ch.elexis.core.types.LocalDateTime" required="true"
	 *        changeable="false"
	 * @generated
	 */
	LocalDateTime getCreatedAt();

	/**
	 * Returns the value of the '<em><b>Run At</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> the time
	 * execution was started (i.e. the run method was called) <!-- end-model-doc -->
	 * 
	 * @return the value of the '<em>Run At</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_RunAt()
	 * @model dataType="ch.elexis.core.types.LocalDateTime" changeable="false"
	 * @generated
	 */
	LocalDateTime getRunAt();

	/**
	 * Returns the value of the '<em><b>Finished At</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Finished At</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_FinishedAt()
	 * @model dataType="ch.elexis.core.types.LocalDateTime" changeable="false"
	 * @generated
	 */
	LocalDateTime getFinishedAt();

	/**
	 * Returns the value of the '<em><b>Result</b></em>' attribute. <!--
	 * begin-user-doc --> Result is de-serialized out of a json string. Numeric
	 * entries are always returned as doubles. <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Result</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_Result()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	Map<String, ?> getResult();

	/**
	 * Returns the value of the '<em><b>Progress Monitor</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Progress Monitor</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Progress Monitor</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_ProgressMonitor()
	 * @model dataType="ch.elexis.core.tasks.model.IProgressMonitor"
	 *        transient="true" changeable="false"
	 * @generated
	 */
	IProgressMonitor getProgressMonitor();

	/**
	 * Returns the value of the '<em><b>Run Context</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Run Context</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Run Context</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_RunContext()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	Map<String, Serializable> getRunContext();

	/**
	 * Returns the value of the '<em><b>Finished</b></em>' attribute. The default
	 * value is <code>"false"</code>. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * <!-- begin-model-doc --> The task has finished its execution, the resulting
	 * states may be TaskState.FINISHED, TaskState.COMPLETED and
	 * TaskState.COMPLETED_WARN <!-- end-model-doc -->
	 * 
	 * @return the value of the '<em>Finished</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_Finished()
	 * @model default="false" required="true" changeable="false"
	 * @generated
	 */
	boolean isFinished();

	/**
	 * Returns the value of the '<em><b>Task Descriptor</b></em>' reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Task Descriptor</em>' reference.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_TaskDescriptor()
	 * @model required="true" changeable="false"
	 * @generated
	 */
	ITaskDescriptor getTaskDescriptor();

	/**
	 * Returns the value of the '<em><b>Runner</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> The station
	 * this task was run on <!-- end-model-doc -->
	 * 
	 * @return the value of the '<em>Runner</em>' attribute.
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_Runner()
	 * @model required="true" changeable="false"
	 * @generated
	 */
	String getRunner();

	/**
	 * Returns the value of the '<em><b>System</b></em>' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>System</em>' attribute.
	 * @see #setSystem(boolean)
	 * @see ch.elexis.core.tasks.model.ModelPackage#getITask_System()
	 * @model
	 * @generated
	 */
	boolean isSystem();

	/**
	 * Sets the value of the '{@link ch.elexis.core.tasks.model.ITask#isSystem
	 * <em>System</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>System</em>' attribute.
	 * @see #isSystem()
	 * @generated
	 */
	void setSystem(boolean value);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model type="ch.elexis.core.types.List&lt;T&gt;" many="false"
	 * @generated
	 */
	<T> List<T> getResultEntryAsTypedList(String key, Class<T> clazz);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	<T> T getResultEntryTyped(String key, Class<T> clazz);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	<T> T getRunContextEntryTyped(String key, Class<T> clazz);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> The
	 * task is successfully and purposefully finished. <!-- end-model-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	boolean isSucceeded();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc --> <!-- begin-model-doc --> The
	 * task is successfully and purposefully finished. <!-- end-model-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	boolean isFailed();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	void setStateCompletedManual(String remark);

} // ITask
