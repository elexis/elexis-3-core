/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.types;

public enum Gender {
	MALE("M"), FEMALE("F"), UNKNOWN("U"), UNDEFINED("X");
	
	private final String value;
	
	private Gender(String value){
		this.value = value;
	}
	
	public String value() {
        return value;
    }

    public static Gender fromValue(String v) {
        switch (v.toUpperCase()) {
		case "M":
			return Gender.MALE;
		case "W":
		case "F":
			return Gender.FEMALE;
		case "X":
			return Gender.UNDEFINED;
		default:
			return Gender.UNKNOWN;
		}
    }
}
