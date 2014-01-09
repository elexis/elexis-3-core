/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package at.medevit.elexis.geonames.interfaces;

import java.util.List;

import ch.elexis.core.types.CountryCode;


/**
 * The Geoname-service as consumed by other plug-ins, combines the different
 * country information provided by other plug-ins using the
 * {@link ICountryGeonameService} interface. This is the single interface for
 * these consumer service to query general geoname information.
 * 
 * @deprecated SHORT TIME SOLUTION TO ALLOW COMPILATION
 */
public interface IGeonameService {

	/**
	 * 
	 * @param country
	 *            the {@link CountryCode} to query the information for
	 * @return the dial prefix for the respective country, empty string if
	 *         information is not available
	 */
	String getDialPrefixByCountry(CountryCode country);

	/**
	 * @param country
	 *            the {@link CountryCode} to query the information for
	 * @return all available ZIP codes for the respective country; if the
	 *         respective country is not available, an empty {@link List} will
	 *         be returned
	 */
	List<String> getZipByCountry(CountryCode country);

	/**
	 * @param country
	 *            the {@link CountryCode} to query the information for
	 * @return a list of values "city (main postal code)", or an empty
	 *         {@link List} if information is not available
	 */
	List<String[]> getLabeledCitiesByCountry(CountryCode country);

	/**
	 * Get the name of a city by providing the ZIP code and the
	 * {@link CountryCode}
	 * 
	 * @param zip
	 *            the zip code of the city
	 * @param country
	 *            the {@link CountryCode}
	 * @return the cities registered for this zip code, or an empty {@link List}
	 *         if information is not available
	 */
	List<String> getCityByZipAndCountry(String zip, CountryCode country);

	/**
	 * Get the ZIP code of a city by providing the city name and the
	 * {@link CountryCode}
	 * 
	 * @param city
	 *            the name of the city
	 * @param country
	 *            the {@link CountryCode}
	 * @return the ZIP codes registered for the city, or an empty {@link List}
	 *         if information is not available
	 */
	List<String> getZipByCityAndCountry(String city, CountryCode country);

	/**
	 * Get the streets registered for a ZIP code within a country
	 * 
	 * @param zip
	 *            the ZIP code of the city
	 * @param country
	 *            the {@link CountryCode}
	 * @return the streets registered, or an empty {@link List} if information
	 *         is not available
	 */
	List<String> getStreetByZipAndCountry(String zip, CountryCode country);

	/**
	 * Get the streets registered for a citye within a country
	 * 
	 * @param city
	 *            the city name
	 * @param country
	 *            the {@link CountryCode}
	 * @return the streets registered in this city, or an empty {@link List} if
	 *         information is not available
	 */
	List<String> getStreetByCityAndCountry(String city, CountryCode country);

}
