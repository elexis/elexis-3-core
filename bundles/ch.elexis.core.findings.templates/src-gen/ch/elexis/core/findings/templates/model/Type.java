/**
 */
package ch.elexis.core.findings.templates.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getType()
 * @model
 * @generated
 */
public enum Type implements Enumerator {
	/**
	 * The '<em><b>OBSERVATION VITAL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OBSERVATION_VITAL_VALUE
	 * @generated
	 * @ordered
	 */
	OBSERVATION_VITAL(1, "OBSERVATION_VITAL", "OBSERVATION_VITAL"), /**
	 * The '<em><b>PROCEDURE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PROCEDURE_VALUE
	 * @generated
	 * @ordered
	 */
	PROCEDURE(100, "PROCEDURE", "PROCEDURE"),

	/**
	 * The '<em><b>CONDITION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CONDITION_VALUE
	 * @generated
	 * @ordered
	 */
	CONDITION(200, "CONDITION", "CONDITION"),

	/**
	 * The '<em><b>EVALUATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EVALUATION_VALUE
	 * @generated
	 * @ordered
	 */
	EVALUATION(300, "EVALUATION", "EVALUATION");

	/**
	 * The '<em><b>OBSERVATION VITAL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>OBSERVATION VITAL</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #OBSERVATION_VITAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OBSERVATION_VITAL_VALUE = 1;

	/**
	 * The '<em><b>PROCEDURE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PROCEDURE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PROCEDURE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PROCEDURE_VALUE = 100;

	/**
	 * The '<em><b>CONDITION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CONDITION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CONDITION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CONDITION_VALUE = 200;

	/**
	 * The '<em><b>EVALUATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>EVALUATION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EVALUATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EVALUATION_VALUE = 300;

	/**
	 * An array of all the '<em><b>Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final Type[] VALUES_ARRAY =
		new Type[] {
			OBSERVATION_VITAL,
			PROCEDURE,
			CONDITION,
			EVALUATION,
		};

	/**
	 * A public read-only list of all the '<em><b>Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<Type> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static Type get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Type result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static Type getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			Type result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static Type get(int value) {
		switch (value) {
			case OBSERVATION_VITAL_VALUE: return OBSERVATION_VITAL;
			case PROCEDURE_VALUE: return PROCEDURE;
			case CONDITION_VALUE: return CONDITION;
			case EVALUATION_VALUE: return EVALUATION;
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
	private Type(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
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
	
} //Type
