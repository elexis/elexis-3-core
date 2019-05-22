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
package ch.elexis.core.tasks.model.impl;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.ModelFactory;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

import ch.elexis.core.types.TypesPackage;

import java.io.Serializable;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.slf4j.Logger;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelPackageImpl extends EPackageImpl implements ModelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iTaskDescriptorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iTaskEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iTaskServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum taskTriggerTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum taskStateEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum ownerTaskNotificationEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType taskExceptionEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType iIdentifiedRunnableEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType iProgressMonitorEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType loggerEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType serializableEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see ch.elexis.core.tasks.model.ModelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ModelPackageImpl() {
		super(eNS_URI, ModelFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link ModelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ModelPackage init() {
		if (isInited) return (ModelPackage)EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredModelPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ModelPackageImpl theModelPackage = registeredModelPackage instanceof ModelPackageImpl ? (ModelPackageImpl)registeredModelPackage : new ModelPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ch.elexis.core.model.ModelPackage.eINSTANCE.eClass();
		TypesPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theModelPackage.createPackageContents();

		// Initialize created meta-data
		theModelPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theModelPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ModelPackage.eNS_URI, theModelPackage);
		return theModelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getITaskDescriptor() {
		return iTaskDescriptorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_Id() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_ReferenceId() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getITaskDescriptor_Owner() {
		return (EReference)iTaskDescriptorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_OwnerNotification() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_Active() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_IdentifiedRunnableId() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_RunContext() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_TriggerType() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_TriggerParameters() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_Runner() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITaskDescriptor_Singleton() {
		return (EAttribute)iTaskDescriptorEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getITask() {
		return iTaskEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_Id() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_State() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_TriggerEvent() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_Result() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_ProgressMonitor() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_RunContext() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_Finished() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITask_DescriptorId() {
		return (EAttribute)iTaskEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getITaskService() {
		return iTaskServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getTaskTriggerType() {
		return taskTriggerTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getTaskState() {
		return taskStateEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getOwnerTaskNotification() {
		return ownerTaskNotificationEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getTaskException() {
		return taskExceptionEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getIIdentifiedRunnable() {
		return iIdentifiedRunnableEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getIProgressMonitor() {
		return iProgressMonitorEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getLogger() {
		return loggerEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getSerializable() {
		return serializableEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ModelFactory getModelFactory() {
		return (ModelFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		iTaskDescriptorEClass = createEClass(ITASK_DESCRIPTOR);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__ID);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__REFERENCE_ID);
		createEReference(iTaskDescriptorEClass, ITASK_DESCRIPTOR__OWNER);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__OWNER_NOTIFICATION);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__ACTIVE);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__IDENTIFIED_RUNNABLE_ID);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__RUN_CONTEXT);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__TRIGGER_TYPE);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__TRIGGER_PARAMETERS);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__RUNNER);
		createEAttribute(iTaskDescriptorEClass, ITASK_DESCRIPTOR__SINGLETON);

		iTaskEClass = createEClass(ITASK);
		createEAttribute(iTaskEClass, ITASK__ID);
		createEAttribute(iTaskEClass, ITASK__STATE);
		createEAttribute(iTaskEClass, ITASK__TRIGGER_EVENT);
		createEAttribute(iTaskEClass, ITASK__RESULT);
		createEAttribute(iTaskEClass, ITASK__PROGRESS_MONITOR);
		createEAttribute(iTaskEClass, ITASK__RUN_CONTEXT);
		createEAttribute(iTaskEClass, ITASK__FINISHED);
		createEAttribute(iTaskEClass, ITASK__DESCRIPTOR_ID);

		iTaskServiceEClass = createEClass(ITASK_SERVICE);

		// Create enums
		taskTriggerTypeEEnum = createEEnum(TASK_TRIGGER_TYPE);
		taskStateEEnum = createEEnum(TASK_STATE);
		ownerTaskNotificationEEnum = createEEnum(OWNER_TASK_NOTIFICATION);

		// Create data types
		taskExceptionEDataType = createEDataType(TASK_EXCEPTION);
		iIdentifiedRunnableEDataType = createEDataType(IIDENTIFIED_RUNNABLE);
		iProgressMonitorEDataType = createEDataType(IPROGRESS_MONITOR);
		loggerEDataType = createEDataType(LOGGER);
		serializableEDataType = createEDataType(SERIALIZABLE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ch.elexis.core.model.ModelPackage theModelPackage_1 = (ch.elexis.core.model.ModelPackage)EPackage.Registry.INSTANCE.getEPackage(ch.elexis.core.model.ModelPackage.eNS_URI);
		TypesPackage theTypesPackage = (TypesPackage)EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		iTaskDescriptorEClass.getESuperTypes().add(theModelPackage_1.getIdentifiable());
		iTaskDescriptorEClass.getESuperTypes().add(theModelPackage_1.getDeleteable());
		iTaskEClass.getESuperTypes().add(theModelPackage_1.getIdentifiable());
		iTaskEClass.getESuperTypes().add(theModelPackage_1.getDeleteable());

		// Initialize classes and features; add operations and parameters
		initEClass(iTaskDescriptorEClass, ITaskDescriptor.class, "ITaskDescriptor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getITaskDescriptor_Id(), ecorePackage.getEString(), "id", null, 1, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_ReferenceId(), ecorePackage.getEString(), "referenceId", null, 0, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getITaskDescriptor_Owner(), theModelPackage_1.getIUser(), null, "owner", null, 1, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_OwnerNotification(), this.getOwnerTaskNotification(), "ownerNotification", "0", 1, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_Active(), ecorePackage.getEBoolean(), "active", "false", 1, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_IdentifiedRunnableId(), ecorePackage.getEString(), "identifiedRunnableId", null, 1, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		EGenericType g1 = createEGenericType(ecorePackage.getEMap());
		EGenericType g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(this.getSerializable());
		g1.getETypeArguments().add(g2);
		initEAttribute(getITaskDescriptor_RunContext(), g1, "runContext", null, 0, 1, ITaskDescriptor.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_TriggerType(), this.getTaskTriggerType(), "triggerType", "MANUAL", 0, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEAttribute(getITaskDescriptor_TriggerParameters(), g1, "triggerParameters", null, 0, 1, ITaskDescriptor.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_Runner(), ecorePackage.getEString(), "runner", null, 0, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITaskDescriptor_Singleton(), ecorePackage.getEBoolean(), "singleton", "false", 0, 1, ITaskDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = addEOperation(iTaskDescriptorEClass, null, "setTriggerParameter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iTaskDescriptorEClass, null, "setRunContextParameter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getSerializable(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iTaskEClass, ITask.class, "ITask", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getITask_Id(), ecorePackage.getEString(), "id", null, 1, 1, ITask.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITask_State(), this.getTaskState(), "state", null, 1, 1, ITask.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITask_TriggerEvent(), this.getTaskTriggerType(), "triggerEvent", null, 1, 1, ITask.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEAttribute(getITask_Result(), g1, "result", null, 0, 1, ITask.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITask_ProgressMonitor(), this.getIProgressMonitor(), "progressMonitor", null, 0, 1, ITask.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(this.getSerializable());
		g1.getETypeArguments().add(g2);
		initEAttribute(getITask_RunContext(), g1, "runContext", null, 0, 1, ITask.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITask_Finished(), ecorePackage.getEBoolean(), "finished", "false", 1, 1, ITask.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITask_DescriptorId(), ecorePackage.getEString(), "descriptorId", null, 1, 1, ITask.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iTaskServiceEClass, ITaskService.class, "ITaskService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iTaskServiceEClass, this.getITaskDescriptor(), "createTaskDescriptor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theModelPackage_1.getIUser(), "owner", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIIdentifiedRunnable(), "runnable", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, this.getITask(), "trigger", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getITaskDescriptor(), "taskDescriptor", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIProgressMonitor(), "progressMonitor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getTaskTriggerType(), "trigger", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "runContext", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, this.getIIdentifiedRunnable(), "instantiateRunnableById", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "runnableId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, null, "saveTaskDescriptor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getITaskDescriptor(), "taskDescriptor", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, null, "setActive", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getITaskDescriptor(), "taskDescriptor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "active", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, this.getITask(), "trigger", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "taskDescriptorReferenceId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIProgressMonitor(), "progressMonitor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getTaskTriggerType(), "trigger", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "runContext", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEException(op, this.getTaskException());

		op = addEOperation(iTaskServiceEClass, null, "getIdentifiedRunnables", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIIdentifiedRunnable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iTaskServiceEClass, null, "findTaskDescriptorByIdOrReferenceId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "idOrReferenceId", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getOptional());
		g2 = createEGenericType(this.getITaskDescriptor());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iTaskServiceEClass, null, "findExecutions", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getITaskDescriptor(), "taskDescriptor", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getITask());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		// Initialize enums and add enum literals
		initEEnum(taskTriggerTypeEEnum, TaskTriggerType.class, "TaskTriggerType");
		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.MANUAL);
		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.FILESYSTEM_CHANGE);
		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.CRON);
		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.SYSTEM_EVENT);
		addEEnumLiteral(taskTriggerTypeEEnum, TaskTriggerType.OTHER_TASK);

		initEEnum(taskStateEEnum, TaskState.class, "TaskState");
		addEEnumLiteral(taskStateEEnum, TaskState.DRAFT);
		addEEnumLiteral(taskStateEEnum, TaskState.READY);
		addEEnumLiteral(taskStateEEnum, TaskState.IN_PROGRESS);
		addEEnumLiteral(taskStateEEnum, TaskState.CANCELLED);
		addEEnumLiteral(taskStateEEnum, TaskState.ON_HOLD);
		addEEnumLiteral(taskStateEEnum, TaskState.FAILED);
		addEEnumLiteral(taskStateEEnum, TaskState.COMPLETED);

		initEEnum(ownerTaskNotificationEEnum, OwnerTaskNotification.class, "OwnerTaskNotification");
		addEEnumLiteral(ownerTaskNotificationEEnum, OwnerTaskNotification.NEVER);
		addEEnumLiteral(ownerTaskNotificationEEnum, OwnerTaskNotification.WHEN_FINISHED);
		addEEnumLiteral(ownerTaskNotificationEEnum, OwnerTaskNotification.WHEN_FINISHED_FAILED);

		// Initialize data types
		initEDataType(taskExceptionEDataType, TaskException.class, "TaskException", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(iIdentifiedRunnableEDataType, IIdentifiedRunnable.class, "IIdentifiedRunnable", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(iProgressMonitorEDataType, IProgressMonitor.class, "IProgressMonitor", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(loggerEDataType, Logger.class, "Logger", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(serializableEDataType, Serializable.class, "Serializable", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //ModelPackageImpl
