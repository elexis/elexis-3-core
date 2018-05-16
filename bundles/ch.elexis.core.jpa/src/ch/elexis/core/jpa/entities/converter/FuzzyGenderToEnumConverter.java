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

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import ch.elexis.core.types.Gender;

/**
 * This converter allows "fuzziness" within the country value of an existing
 * database. The values should be set to {@link ISO3166_ALPHA_2_CountryCode} but
 * it can't be guaranteed, so in case a value not equal to the defined set is
 * observed it simply returns null instead of an Exception.
 */
public class FuzzyGenderToEnumConverter implements Converter {

	private static final long serialVersionUID = 1L;

	@Override
	public Character convertObjectValueToDataValue(Object objectValue, Session session) {
		Gender g = (Gender) objectValue;
		if (g == null)
			return 'x';
		switch (g) {
		case MALE:
			return 'm';
		case FEMALE:
			return 'w';
		default:
			return 'x';
		}
	}

	@Override
	public Gender convertDataValueToObjectValue(Object dataValue, Session session) {
		if (dataValue == null)
			return Gender.UNKNOWN;
		int in = ((String) dataValue).trim().toLowerCase().hashCode();
		switch (in) {
		case 119:
			return Gender.FEMALE; // 'w'
		case 102:
			return Gender.FEMALE; // 'f'
		case 109:
			return Gender.MALE; // 'm'
		default:
			return Gender.UNKNOWN;
		}
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
	}

}
