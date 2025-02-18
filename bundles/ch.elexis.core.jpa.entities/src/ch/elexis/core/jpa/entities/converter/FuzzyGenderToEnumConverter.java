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
package ch.elexis.core.jpa.entities.converter;

import ch.elexis.core.types.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * This converter allows "fuzziness" within the country value of an existing
 * database. The values should be set to {@link ISO3166_ALPHA_2_CountryCode} but
 * it can't be guaranteed, so in case a value not equal to the defined set is
 * observed it simply returns null instead of an Exception.
 */
@Converter
public class FuzzyGenderToEnumConverter implements AttributeConverter<Gender, String> {

	@Override
	public String convertToDatabaseColumn(Gender objectValue) {
		if (objectValue == null)
			return "x";
		switch (objectValue) {
		case MALE:
			return "m";
		case FEMALE:
			return "w";
		default:
			return "x";
		}
	}

	@Override
	public Gender convertToEntityAttribute(String dataValue) {
		if (dataValue == null)
			return Gender.UNKNOWN;
		switch (dataValue) {
		case "w":
		case "W":
		case "f":
		case "F":
			return Gender.FEMALE;
		case "m":
		case "M":
			return Gender.MALE;
		default:
			return Gender.UNKNOWN;
		}
	}
}
