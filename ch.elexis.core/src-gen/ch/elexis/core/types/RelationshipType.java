/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>Relationship Type</b></em>', and utility methods for working with them. <!-- end-user-doc
 * -->
 * 
 * @see ch.elexis.core.types.TypesPackage#getRelationshipType()
 * @model
 * @generated
 */
public enum RelationshipType implements Enumerator {
	/**
	 * The '<em><b>AGENERIC</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #AGENERIC_VALUE
	 * @generated
	 * @ordered
	 */
	AGENERIC(0, "A_GENERIC", "A_GENERIC"),
	
	/**
	 * The '<em><b>BUSINESS EMPLOYER</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #BUSINESS_EMPLOYER_VALUE
	 * @generated
	 * @ordered
	 */
	BUSINESS_EMPLOYER(100, "BUSINESS_EMPLOYER", "BUSINESS_EMPLOYER"),
	
	/**
	 * The '<em><b>BUSINESS EMPLOYEE</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #BUSINESS_EMPLOYEE_VALUE
	 * @generated
	 * @ordered
	 */
	BUSINESS_EMPLOYEE(101, "BUSINESS_EMPLOYEE", "BUSINESS_EMPLOYEE"),
	
	/**
	 * The '<em><b>FAMILY HUSBAND</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #FAMILY_HUSBAND_VALUE
	 * @generated
	 * @ordered
	 */
	FAMILY_HUSBAND(200, "FAMILY_HUSBAND", "FAMILY_HUSBAND"),
	
	/**
	 * The '<em><b>FAMILY WIFE</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #FAMILY_WIFE_VALUE
	 * @generated
	 * @ordered
	 */
	FAMILY_WIFE(201, "FAMILY_WIFE", ""),
	
	/**
	 * The '<em><b>FAMILY PARENT</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #FAMILY_PARENT_VALUE
	 * @generated
	 * @ordered
	 */
	FAMILY_PARENT(210, "FAMILY_PARENT", "FAMILY_PARENT"),
	
	/**
	 * The '<em><b>FAMILY CHILD</b></em>' literal object. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @see #FAMILY_CHILD_VALUE
	 * @generated
	 * @ordered
	 */
	FAMILY_CHILD(211, "FAMILY_CHILD", "FAMILY_CHILD"),
	
	/**
	 * The '<em><b>WELFARE GENERAL PRACTITIONER</b></em>' literal object. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_GENERAL_PRACTITIONER_VALUE
	 * @generated
	 * @ordered
	 */
	WELFARE_GENERAL_PRACTITIONER(300, "WELFARE_GENERAL_PRACTITIONER",
		"WELFARE_GENERAL_PRACTITIONER"),
	
	/**
	 * The '<em><b>WELFARE PATIENT</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #WELFARE_PATIENT_VALUE
	 * @generated
	 * @ordered
	 */
	WELFARE_PATIENT(301, "WELFARE_PATIENT", "WELFARE_PATIENT"),
	
	/**
	 * The '<em><b>WELFARE CONSULTANT</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #WELFARE_CONSULTANT_VALUE
	 * @generated
	 * @ordered
	 */
	WELFARE_CONSULTANT(310, "WELFARE_CONSULTANT", "WELFARE_CONSULTANT"),
	
	/**
	 * The '<em><b>WELFARE INSURER</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #WELFARE_INSURER_VALUE
	 * @generated
	 * @ordered
	 */
	WELFARE_INSURER(410, "WELFARE_INSURER", "WELFARE_INSURER"),
	
	/**
	 * The '<em><b>WELFARE INSUREE</b></em>' literal object. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #WELFARE_INSUREE_VALUE
	 * @generated
	 * @ordered
	 */
	WELFARE_INSUREE(411, "WELFARE_INSUREE", "WELFARE_INSUREE");
	
	/**
	 * The '<em><b>AGENERIC</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>AGENERIC</b></em>' literal object isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #AGENERIC
	 * @model name="A_GENERIC"
	 * @generated
	 * @ordered
	 */
	public static final int AGENERIC_VALUE = 0;
	
	/**
	 * The '<em><b>BUSINESS EMPLOYER</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>BUSINESS EMPLOYER</b></em>' literal object isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #BUSINESS_EMPLOYER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BUSINESS_EMPLOYER_VALUE = 100;
	
	/**
	 * The '<em><b>BUSINESS EMPLOYEE</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>BUSINESS EMPLOYEE</b></em>' literal object isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #BUSINESS_EMPLOYEE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BUSINESS_EMPLOYEE_VALUE = 101;
	
	/**
	 * The '<em><b>FAMILY HUSBAND</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAMILY HUSBAND</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FAMILY_HUSBAND
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAMILY_HUSBAND_VALUE = 200;
	
	/**
	 * The '<em><b>FAMILY WIFE</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAMILY WIFE</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FAMILY_WIFE
	 * @model literal=""
	 * @generated
	 * @ordered
	 */
	public static final int FAMILY_WIFE_VALUE = 201;
	
	/**
	 * The '<em><b>FAMILY PARENT</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAMILY PARENT</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FAMILY_PARENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAMILY_PARENT_VALUE = 210;
	
	/**
	 * The '<em><b>FAMILY CHILD</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAMILY CHILD</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #FAMILY_CHILD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAMILY_CHILD_VALUE = 211;
	
	/**
	 * The '<em><b>WELFARE GENERAL PRACTITIONER</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WELFARE GENERAL PRACTITIONER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_GENERAL_PRACTITIONER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WELFARE_GENERAL_PRACTITIONER_VALUE = 300;
	
	/**
	 * The '<em><b>WELFARE PATIENT</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WELFARE PATIENT</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_PATIENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WELFARE_PATIENT_VALUE = 301;
	
	/**
	 * The '<em><b>WELFARE CONSULTANT</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WELFARE CONSULTANT</b></em>' literal object isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_CONSULTANT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WELFARE_CONSULTANT_VALUE = 310;
	
	/**
	 * The '<em><b>WELFARE INSURER</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WELFARE INSURER</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_INSURER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WELFARE_INSURER_VALUE = 410;
	
	/**
	 * The '<em><b>WELFARE INSUREE</b></em>' literal value. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>WELFARE INSUREE</b></em>' literal object isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @see #WELFARE_INSUREE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WELFARE_INSUREE_VALUE = 411;
	
	/**
	 * An array of all the '<em><b>Relationship Type</b></em>' enumerators. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static final RelationshipType[] VALUES_ARRAY = new RelationshipType[] {
		AGENERIC, BUSINESS_EMPLOYER, BUSINESS_EMPLOYEE, FAMILY_HUSBAND, FAMILY_WIFE, FAMILY_PARENT,
		FAMILY_CHILD, WELFARE_GENERAL_PRACTITIONER, WELFARE_PATIENT, WELFARE_CONSULTANT,
		WELFARE_INSURER, WELFARE_INSUREE,
	};
	
	/**
	 * A public read-only list of all the '<em><b>Relationship Type</b></em>' enumerators. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static final List<RelationshipType> VALUES = Collections.unmodifiableList(Arrays
		.asList(VALUES_ARRAY));
	
	/**
	 * Returns the '<em><b>Relationship Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static RelationshipType get(String literal){
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RelationshipType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Returns the '<em><b>Relationship Type</b></em>' literal with the specified name. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static RelationshipType getByName(String name){
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RelationshipType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Returns the '<em><b>Relationship Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public static RelationshipType get(int value){
		switch (value) {
		case AGENERIC_VALUE:
			return AGENERIC;
		case BUSINESS_EMPLOYER_VALUE:
			return BUSINESS_EMPLOYER;
		case BUSINESS_EMPLOYEE_VALUE:
			return BUSINESS_EMPLOYEE;
		case FAMILY_HUSBAND_VALUE:
			return FAMILY_HUSBAND;
		case FAMILY_WIFE_VALUE:
			return FAMILY_WIFE;
		case FAMILY_PARENT_VALUE:
			return FAMILY_PARENT;
		case FAMILY_CHILD_VALUE:
			return FAMILY_CHILD;
		case WELFARE_GENERAL_PRACTITIONER_VALUE:
			return WELFARE_GENERAL_PRACTITIONER;
		case WELFARE_PATIENT_VALUE:
			return WELFARE_PATIENT;
		case WELFARE_CONSULTANT_VALUE:
			return WELFARE_CONSULTANT;
		case WELFARE_INSURER_VALUE:
			return WELFARE_INSURER;
		case WELFARE_INSUREE_VALUE:
			return WELFARE_INSUREE;
		}
		return null;
	}
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final int value;
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String name;
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private final String literal;
	
	/**
	 * Only this class can construct instances. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private RelationshipType(int value, String name, String literal){
		this.value = value;
		this.name = name;
		this.literal = literal;
	}
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public int getValue(){
		return value;
	}
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getLiteral(){
		return literal;
	}
	
	/**
	 * Returns the literal value of the enumerator, which is its string representation. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString(){
		return literal;
	}
	
} // RelationshipType
