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
 * A representation of the literals of the enumeration '<em><b>Owner Task Notification</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.tasks.model.ModelPackage#getOwnerTaskNotification()
 * @model
 * @generated
 */
public enum OwnerTaskNotification implements Enumerator {
	/**
	 * The '<em><b>NEVER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #NEVER_VALUE
	 * @generated
	 * @ordered
	 */
	NEVER(0, "NEVER", "NEVER"),

	/**
	 * The '<em><b>WHEN FINISHED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #WHEN_FINISHED_VALUE
	 * @generated
	 * @ordered
	 */
	WHEN_FINISHED(1, "WHEN_FINISHED", "WHEN_FINISHED"),

	/**
	 * The '<em><b>WHEN FINISHED FAILED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #WHEN_FINISHED_FAILED_VALUE
	 * @generated
	 * @ordered
	 */
	WHEN_FINISHED_FAILED(2, "WHEN_FINISHED_FAILED", "WHEN_FINISHED_FAILED");

	/**
	 * The '<em><b>NEVER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NEVER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #NEVER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NEVER_VALUE = 0;

	/**
	 * The '<em><b>WHEN FINISHED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WHEN FINISHED</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #WHEN_FINISHED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WHEN_FINISHED_VALUE = 1;

	/**
	 * The '<em><b>WHEN FINISHED FAILED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WHEN FINISHED FAILED</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #WHEN_FINISHED_FAILED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WHEN_FINISHED_FAILED_VALUE = 2;

	/**
	 * An array of all the '<em><b>Owner Task Notification</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final OwnerTaskNotification[] VALUES_ARRAY =
		new OwnerTaskNotification[] {
			NEVER,
			WHEN_FINISHED,
			WHEN_FINISHED_FAILED,
		};

	/**
	 * A public read-only list of all the '<em><b>Owner Task Notification</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<OwnerTaskNotification> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Owner Task Notification</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OwnerTaskNotification get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OwnerTaskNotification result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Owner Task Notification</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OwnerTaskNotification getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OwnerTaskNotification result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Owner Task Notification</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OwnerTaskNotification get(int value) {
		switch (value) {
			case NEVER_VALUE: return NEVER;
			case WHEN_FINISHED_VALUE: return WHEN_FINISHED;
			case WHEN_FINISHED_FAILED_VALUE: return WHEN_FINISHED_FAILED;
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
	private OwnerTaskNotification(int value, String name, String literal) {
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
	
} //OwnerTaskNotification
