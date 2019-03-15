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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Task Trigger Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.tasks.model.ModelPackage#getTaskTriggerType()
 * @model
 * @generated
 */
public enum TaskTriggerType implements Enumerator {
	/**
	 * The '<em><b>MANUAL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #MANUAL_VALUE
	 * @generated
	 * @ordered
	 */
	MANUAL(0, "MANUAL", "MANUAL"),

	/**
	 * The '<em><b>FILESYSTEM CHANGE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #FILESYSTEM_CHANGE_VALUE
	 * @generated
	 * @ordered
	 */
	FILESYSTEM_CHANGE(1, "FILESYSTEM_CHANGE", "FILESYSTEM_CHANGE"),

	/**
	 * The '<em><b>CRON</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CRON_VALUE
	 * @generated
	 * @ordered
	 */
	CRON(2, "CRON", "CRON"),

	/**
	 * The '<em><b>SYSTEM EVENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #SYSTEM_EVENT_VALUE
	 * @generated
	 * @ordered
	 */
	SYSTEM_EVENT(3, "SYSTEM_EVENT", "SYSTEM_EVENT"),

	/**
	 * The '<em><b>OTHER TASK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OTHER_TASK_VALUE
	 * @generated
	 * @ordered
	 */
	OTHER_TASK(4, "OTHER_TASK", "OTHER_TASK");

	/**
	 * The '<em><b>MANUAL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>MANUAL</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #MANUAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MANUAL_VALUE = 0;

	/**
	 * The '<em><b>FILESYSTEM CHANGE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FILESYSTEM CHANGE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FILESYSTEM_CHANGE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FILESYSTEM_CHANGE_VALUE = 1;

	/**
	 * The '<em><b>CRON</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CRON</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CRON
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CRON_VALUE = 2;

	/**
	 * The '<em><b>SYSTEM EVENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>SYSTEM EVENT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #SYSTEM_EVENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SYSTEM_EVENT_VALUE = 3;

	/**
	 * The '<em><b>OTHER TASK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>OTHER TASK</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #OTHER_TASK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OTHER_TASK_VALUE = 4;

	/**
	 * An array of all the '<em><b>Task Trigger Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final TaskTriggerType[] VALUES_ARRAY =
		new TaskTriggerType[] {
			MANUAL,
			FILESYSTEM_CHANGE,
			CRON,
			SYSTEM_EVENT,
			OTHER_TASK,
		};

	/**
	 * A public read-only list of all the '<em><b>Task Trigger Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<TaskTriggerType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Task Trigger Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskTriggerType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TaskTriggerType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Task Trigger Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskTriggerType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TaskTriggerType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Task Trigger Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskTriggerType get(int value) {
		switch (value) {
			case MANUAL_VALUE: return MANUAL;
			case FILESYSTEM_CHANGE_VALUE: return FILESYSTEM_CHANGE;
			case CRON_VALUE: return CRON;
			case SYSTEM_EVENT_VALUE: return SYSTEM_EVENT;
			case OTHER_TASK_VALUE: return OTHER_TASK;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private TaskTriggerType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //TaskTriggerType
