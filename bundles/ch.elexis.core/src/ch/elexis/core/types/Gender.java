/*******************************************************************************
 * Copyright (c) 2022 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.types;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.l10n.Messages;

public enum Gender implements ILocalizedEnum {
	MALE("M"), FEMALE("F"), UNKNOWN("U"), UNDEFINED("X");

	private final String value;

	private Gender(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static Gender fromValue(String string) {
		if (string != null) {
			switch (string.toUpperCase()) {
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
		return Gender.UNKNOWN;
	}

	@Override
	public String getLocaleText() {
		switch (this) {
		case FEMALE:
			return Messages.Patient_female_short;
		case MALE:
			return Messages.Patient_male_short;
		default:
			return "?";
		}
	}
}
