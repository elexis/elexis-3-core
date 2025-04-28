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

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * This converter allows "fuzziness" within the country value of an existing
 * database. The values should be set to {@link Country} but it can't be
 * guaranteed, so in case a value not equal to the defined set is observed it
 * simply returns null instead of an Exception.
 */
@Converter
public class FuzzyCountryToEnumConverter implements AttributeConverter<Country, String> {

	@Override
	public String convertToDatabaseColumn(Country objectValue) {
		if (objectValue == null) {
			return StringUtils.EMPTY;
		}
		Country c = (Country) objectValue;
		return c.name();
	}

	@Override
	public Country convertToEntityAttribute(String dataValue) {
		try {
			if (dataValue != null) {
				return Country.valueOf(dataValue.trim());
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			// ignore -> NDF
		}
		return Country.NDF;
	}
}
