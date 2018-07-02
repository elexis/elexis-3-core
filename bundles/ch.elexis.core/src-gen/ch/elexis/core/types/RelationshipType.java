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

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Relationship Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.types.TypesPackage#getRelationshipType()
 * @model
 * @generated
 */
public enum RelationshipType implements INumericEnum, ILocalizedEnum {
	//@formatter:off
	AGENERIC(0),
	BUSINESS_EMPLOYEE(101),
	BUSINESS_EMPLOYER(100),
	FAMILY_ALTERNATIVE(290),
	FAMILY_CHILD(210),
	FAMILY_GUARDIAN(220),
	FAMILY_ICE(230),
	FAMILY_PARENT(200),
	WELFARE_CONSULTANT(310),
	WELFARE_GENERAL_PRACTITIONER(300),
	WELFARE_INSUREE(411),
	WELFARE_INSURER(410),
	WELFARE_PATIENT(301);
	//@formatter:on
	private int state;

	private RelationshipType(int state){
		this.state = state;
	}
	
	public int getState(){
		return state;
	}
	
	@Override
	public int numericValue(){
		return state;
	}

	/**
	/**
	 * Returns the '<em><b>Relationship Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RelationshipType get(int value) {
		for (RelationshipType rType : RelationshipType.values()) {
			 if (rType.state == value) {
				 return rType;
			}
		}
		return null;
	}
	
	public static RelationshipType get(String initialValue) {
		for (RelationshipType rType : RelationshipType.values()) {
			 if (rType.getName().equals(initialValue)) {
				 return rType;
			}
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
	  return this.getLocaleText();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLiteral() {
	  return this.name();
	}


	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(RelationshipType.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public int getValue(){
		return this.numericValue();
	}


} //RelationshipType
