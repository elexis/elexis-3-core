/*******************************************************************************
 * Copyright (c) 2014 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.interfaces;

import java.util.List;

import ch.elexis.core.types.CountryCode;

public interface IContactGenoameService {
	
	/**
	 * @return the country this contribution provides information for
	 */
	public CountryCode getProvidesInformationForCountry();
	
	/**
	 * 
	 * @return the dial prefix for the country
	 */
	String getDialPrefix();
	
	/**
	 * @return all available ZIP codes for the country; if the respective country is not available,
	 *         an empty {@link List} will be returned
	 */
	List<String> getZip();
	
	/**
	 * @return a list of values "city (main postal code)" , or an empty {@link List} if information
	 *         is not available
	 */
	List<String[]> getLabeledCities();
	
	/**
	 * Get the name of a city by providing the ZIP code and the {@link CountryCode}
	 * 
	 * @param zip
	 *            the zip code of the city
	 * @return the cities registered for this zip code, or an empty {@link List} if information is
	 *         not available
	 */
	List<String> getCityByZip(String zip);
	
	/**
	 * Get the ZIP code of a city by providing the city name and the {@link CountryCode}
	 * 
	 * @param city
	 *            the name of the city
	 * @return the ZIP codes registered for the city, or an empty {@link List} if information is not
	 *         available
	 */
	List<String> getZipByCity(String city);
	
	/**
	 * Get the streets registered for a ZIP code within a country
	 * 
	 * @param zip
	 *            the ZIP code of the city
	 * @return the streets registered, or an empty {@link List} if information is not available
	 */
	List<String> getStreetByZip(String zip);
	
	/**
	 * Get the streets registered for a citye within a country
	 * 
	 * @param city
	 *            the city name
	 * @return the streets registered in this city, or an empty {@link List} if information is not
	 *         available
	 */
	List<String> getStreetByCity(String city);
	
}
