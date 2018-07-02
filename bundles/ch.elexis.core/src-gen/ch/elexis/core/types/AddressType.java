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
 * A representation of the literals of the enumeration '<em><b>Address Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.types.TypesPackage#getAddressType()
 * @model
 * @generated
 */
public enum AddressType implements INumericEnum, ILocalizedEnum {
	//@formatter:off
	PRINCIPAL_RESIDENCE(0),
	SECONDARY_RESIDENCE(1),
	HOLIDAY_HOME(2),
	PLACE_OF_RESIDENCE(3),
	EMPLOYER(4),
	FAMILY_FRIENDS(5),
	ATTACHMENT_FIGURE(6),
	PRISON(7),
	NURSING_HOME(8),
	OTHER(9);
	//@formatter:on

	private int state;

	private AddressType(int state){
		this.state = state;
	}
	
	public int getState(){
		return state;
	}
	
	public int getValue() {
		return getState();
	}
	
	@Override
	public int numericValue(){
		return state;
	}
	
	
	public static AddressType get(int value) {
		for (AddressType rType : AddressType.values()) {
			 if (rType.state == value) {
				 return rType;
			}
		}
		return null;
	}
	
	public static AddressType get(String initialValue) {
		for (AddressType rType : AddressType.values()) {
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
					.getString(AddressType.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

} //AddressType
