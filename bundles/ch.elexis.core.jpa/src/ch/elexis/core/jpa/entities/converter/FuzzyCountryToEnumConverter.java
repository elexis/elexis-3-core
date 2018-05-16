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

import ch.elexis.core.types.Country;


/**
 * This converter allows "fuzziness" within the country value of an existing
 * database. The values should be set to {@link Country} but
 * it can't be guaranteed, so in case a value not equal to the defined set is
 * observed it simply returns null instead of an Exception.
 */
public class FuzzyCountryToEnumConverter implements Converter {

	private static final long serialVersionUID = 439835332745734218L;

	@Override
	public String convertObjectValueToDataValue(Object objectValue, Session session) {
		if (objectValue == null) {
			return "";
		}
		Country c = (Country) objectValue;
		return c.name();
	}

	@Override
	public Country convertDataValueToObjectValue(Object dataValue, Session session) {
		try {
			return Country.valueOf((String) dataValue);
		} catch (IllegalArgumentException | NullPointerException e) {
			return Country.NDF;
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
