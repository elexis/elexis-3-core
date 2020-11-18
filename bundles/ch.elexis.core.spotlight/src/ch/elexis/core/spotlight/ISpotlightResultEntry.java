package ch.elexis.core.spotlight;

public interface ISpotlightResultEntry {
	
	public enum Category {
			PATIENT, LETTER, DOCUMENT, ENCOUNTER, APPOINTMENT
	};
	
	public Category getCategory();
	
	public String getLabel();
	
	/**
	 * Depending on Category this string contains information on how to load the resp. Object.<br>
	 * PATIENT: contactId<br>
	 * 
	 * @return
	 */
	public String getIdentifierString();
	
}
