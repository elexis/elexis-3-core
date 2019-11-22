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
 * A representation of the literals of the enumeration '<em><b>Task State</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.tasks.model.ModelPackage#getTaskState()
 * @model
 * @generated
 */
public enum TaskState implements Enumerator {
	/**
	 * The '<em><b>DRAFT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #DRAFT_VALUE
	 * @generated
	 * @ordered
	 */
	DRAFT(0, "DRAFT", "DRAFT"),

	/**
	 * The '<em><b>READY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #READY_VALUE
	 * @generated
	 * @ordered
	 */
	READY(10, "READY", "READY"),

	/**
	 * The '<em><b>IN PROGRESS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #IN_PROGRESS_VALUE
	 * @generated
	 * @ordered
	 */
	IN_PROGRESS(20, "IN_PROGRESS", "IN_PROGRESS"),

	/**
	 * The '<em><b>CANCELLED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CANCELLED_VALUE
	 * @generated
	 * @ordered
	 */
	CANCELLED(30, "CANCELLED", "CANCELLED"),

	/**
	 * The '<em><b>ON HOLD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ON_HOLD_VALUE
	 * @generated
	 * @ordered
	 */
	ON_HOLD(40, "ON_HOLD", "ON_HOLD"),

	/**
	 * The '<em><b>COMPLETED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COMPLETED_VALUE
	 * @generated
	 * @ordered
	 */
	COMPLETED(50, "COMPLETED", "COMPLETED"), /**
	 * The '<em><b>COMPLETED WARN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The task completed successfully, but not flawless. I.e. there were warnings somebody should consider.
	 * <!-- end-model-doc -->
	 * @see #COMPLETED_WARN_VALUE
	 * @generated
	 * @ordered
	 */
	COMPLETED_WARN(55, "COMPLETED_WARN", "COMPLETED_WARN"), /**
	 * The '<em><b>FAILED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #FAILED_VALUE
	 * @generated
	 * @ordered
	 */
	FAILED(90, "FAILED", "FAILED");

	/**
	 * The '<em><b>DRAFT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>DRAFT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #DRAFT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DRAFT_VALUE = 0;

	/**
	 * The '<em><b>READY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>READY</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #READY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int READY_VALUE = 10;

	/**
	 * The '<em><b>IN PROGRESS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>IN PROGRESS</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #IN_PROGRESS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int IN_PROGRESS_VALUE = 20;

	/**
	 * The '<em><b>CANCELLED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CANCELLED</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CANCELLED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CANCELLED_VALUE = 30;

	/**
	 * The '<em><b>ON HOLD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ON HOLD</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ON_HOLD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ON_HOLD_VALUE = 40;

	/**
	 * The '<em><b>COMPLETED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COMPLETED</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COMPLETED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COMPLETED_VALUE = 50;

	/**
	 * The '<em><b>COMPLETED WARN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The task completed successfully, but not flawless. I.e. there were warnings somebody should consider.
	 * <!-- end-model-doc -->
	 * @see #COMPLETED_WARN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COMPLETED_WARN_VALUE = 55;

	/**
	 * The '<em><b>FAILED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAILED</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FAILED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAILED_VALUE = 90;

	/**
	 * An array of all the '<em><b>Task State</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final TaskState[] VALUES_ARRAY =
		new TaskState[] {
			DRAFT,
			READY,
			IN_PROGRESS,
			CANCELLED,
			ON_HOLD,
			COMPLETED,
			COMPLETED_WARN,
			FAILED,
		};

	/**
	 * A public read-only list of all the '<em><b>Task State</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<TaskState> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Task State</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskState get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TaskState result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Task State</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskState getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TaskState result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Task State</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TaskState get(int value) {
		switch (value) {
			case DRAFT_VALUE: return DRAFT;
			case READY_VALUE: return READY;
			case IN_PROGRESS_VALUE: return IN_PROGRESS;
			case CANCELLED_VALUE: return CANCELLED;
			case ON_HOLD_VALUE: return ON_HOLD;
			case COMPLETED_VALUE: return COMPLETED;
			case COMPLETED_WARN_VALUE: return COMPLETED_WARN;
			case FAILED_VALUE: return FAILED;
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
	private TaskState(int value, String name, String literal) {
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
	
} //TaskState
