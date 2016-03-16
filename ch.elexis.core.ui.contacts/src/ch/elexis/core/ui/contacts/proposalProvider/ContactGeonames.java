package ch.elexis.core.ui.contacts.proposalProvider;

import java.util.Collections;
import java.util.List;

import ch.elexis.core.types.Country;
import ch.elexis.core.ui.contacts.extension.ContactGeonamesExtensionPoint;
import ch.elexis.core.ui.contacts.interfaces.IContactGenoameService;

public class ContactGeonames {
	
	private static Country currentCountryCode;
	private static IContactGenoameService currentService;
	
	public static void setCountry(Country country){
		currentCountryCode = country;
		currentService = ContactGeonamesExtensionPoint.getGeonameServiceForCountry(country);
	}
	
	public static List<String[]> getLabeledCities(){
		if (currentService != null)
			return currentService.getLabeledCities();
		return Collections.emptyList();
	}
	
	public static List<String> getZipByCity(String city){
		if (currentService != null)
			return currentService.getZipByCity(city);
		return Collections.emptyList();
	}
	
	public static List<String> getStreetByZip(String zip){
		if (currentService != null)
			return currentService.getStreetByZip(zip);
		return Collections.emptyList();
	}
	
	public static List<String> getZip(){
		if (currentService != null)
			return currentService.getZip();
		return Collections.emptyList();
	}
	
	public static List<String> getCityByZip(String zip){
		if (currentService != null)
			return currentService.getCityByZip(zip);
		return Collections.emptyList();
	}
	
	public static String getDialPrefix(){
		if (currentService != null)
			return currentService.getDialPrefix();
		return "";
	}
	
	public static Country getCurrentCountryCode(){
		return currentCountryCode;
	}
	
}
