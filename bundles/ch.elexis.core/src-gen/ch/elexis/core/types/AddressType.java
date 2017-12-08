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
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Address Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.types.TypesPackage#getAddressType()
 * @model
 * @generated
 */
public enum AddressType implements Enumerator {
	/**
	 * The '<em><b>PRINCIPAL RESIDENCE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PRINCIPAL_RESIDENCE_VALUE
	 * @generated
	 * @ordered
	 */
	PRINCIPAL_RESIDENCE(0, "PRINCIPAL_RESIDENCE", "PRINCIPAL_RESIDENCE"),

	/**
	 * The '<em><b>SECONDARY RESIDENCE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #SECONDARY_RESIDENCE_VALUE
	 * @generated
	 * @ordered
	 */
	SECONDARY_RESIDENCE(1, "SECONDARY_RESIDENCE", "SECONDARY_RESIDENCE"),

	/**
	 * The '<em><b>HOLIDAY HOME</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #HOLIDAY_HOME_VALUE
	 * @generated
	 * @ordered
	 */
	HOLIDAY_HOME(2, "HOLIDAY_HOME", "HOLIDAY_HOME"), /**
	 * The '<em><b>PLACE OF RESIDENCE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PLACE_OF_RESIDENCE_VALUE
	 * @generated
	 * @ordered
	 */
	PLACE_OF_RESIDENCE(3, "PLACE_OF_RESIDENCE", "PLACE_OF_RESIDENCE"), /**
	 * The '<em><b>EMPLOYER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EMPLOYER_VALUE
	 * @generated
	 * @ordered
	 */
	EMPLOYER(4, "EMPLOYER", "EMPLOYER"), /**
	 * The '<em><b>FAMILY FRIENDS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #FAMILY_FRIENDS_VALUE
	 * @generated
	 * @ordered
	 */
	FAMILY_FRIENDS(5, "FAMILY_FRIENDS", "FAMILY_FRIENDS"), /**
	 * The '<em><b>ATTACHMENT FIGURE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ATTACHMENT_FIGURE_VALUE
	 * @generated
	 * @ordered
	 */
	ATTACHMENT_FIGURE(6, "ATTACHMENT_FIGURE", "ATTACHMENT_FIGURE"), /**
	 * The '<em><b>PRISON</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PRISON_VALUE
	 * @generated
	 * @ordered
	 */
	PRISON(7, "PRISON", "PRISON"), /**
	 * The '<em><b>NURSING HOME</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #NURSING_HOME_VALUE
	 * @generated
	 * @ordered
	 */
	NURSING_HOME(8, "NURSING_HOME", "NURSING_HOME"),

	/**
	 * The '<em><b>OTHER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OTHER_VALUE
	 * @generated
	 * @ordered
	 */
	OTHER(9, "OTHER", "OTHER");

	/**
	 * The '<em><b>PRINCIPAL RESIDENCE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PRINCIPAL RESIDENCE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PRINCIPAL_RESIDENCE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PRINCIPAL_RESIDENCE_VALUE = 0;

	/**
	 * The '<em><b>SECONDARY RESIDENCE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>SECONDARY RESIDENCE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #SECONDARY_RESIDENCE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SECONDARY_RESIDENCE_VALUE = 1;

	/**
	 * The '<em><b>HOLIDAY HOME</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>HOLIDAY HOME</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #HOLIDAY_HOME
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int HOLIDAY_HOME_VALUE = 2;

	/**
	 * The '<em><b>PLACE OF RESIDENCE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PLACE OF RESIDENCE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PLACE_OF_RESIDENCE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PLACE_OF_RESIDENCE_VALUE = 3;

	/**
	 * The '<em><b>EMPLOYER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>EMPLOYER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EMPLOYER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EMPLOYER_VALUE = 4;

	/**
	 * The '<em><b>FAMILY FRIENDS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>FAMILY FRIENDS</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #FAMILY_FRIENDS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAMILY_FRIENDS_VALUE = 5;

	/**
	 * The '<em><b>ATTACHMENT FIGURE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ATTACHMENT FIGURE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ATTACHMENT_FIGURE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ATTACHMENT_FIGURE_VALUE = 6;

	/**
	 * The '<em><b>PRISON</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PRISON</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PRISON
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PRISON_VALUE = 7;

	/**
	 * The '<em><b>NURSING HOME</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NURSING HOME</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #NURSING_HOME
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NURSING_HOME_VALUE = 8;

	/**
	 * The '<em><b>OTHER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>OTHER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #OTHER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OTHER_VALUE = 9;

	/**
	 * An array of all the '<em><b>Address Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final AddressType[] VALUES_ARRAY =
		new AddressType[] {
			PRINCIPAL_RESIDENCE,
			SECONDARY_RESIDENCE,
			HOLIDAY_HOME,
			PLACE_OF_RESIDENCE,
			EMPLOYER,
			FAMILY_FRIENDS,
			ATTACHMENT_FIGURE,
			PRISON,
			NURSING_HOME,
			OTHER,
		};

	/**
	 * A public read-only list of all the '<em><b>Address Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<AddressType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Address Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AddressType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AddressType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Address Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AddressType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AddressType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Address Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AddressType get(int value) {
		switch (value) {
			case PRINCIPAL_RESIDENCE_VALUE: return PRINCIPAL_RESIDENCE;
			case SECONDARY_RESIDENCE_VALUE: return SECONDARY_RESIDENCE;
			case HOLIDAY_HOME_VALUE: return HOLIDAY_HOME;
			case PLACE_OF_RESIDENCE_VALUE: return PLACE_OF_RESIDENCE;
			case EMPLOYER_VALUE: return EMPLOYER;
			case FAMILY_FRIENDS_VALUE: return FAMILY_FRIENDS;
			case ATTACHMENT_FIGURE_VALUE: return ATTACHMENT_FIGURE;
			case PRISON_VALUE: return PRISON;
			case NURSING_HOME_VALUE: return NURSING_HOME;
			case OTHER_VALUE: return OTHER;
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
	private AddressType(int value, String name, String literal) {
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
	
} //AddressType
