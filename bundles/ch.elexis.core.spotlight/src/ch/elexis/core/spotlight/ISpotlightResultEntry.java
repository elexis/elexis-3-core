package ch.elexis.core.spotlight;

import java.util.Optional;

public interface ISpotlightResultEntry {

	public enum Category {
		PATIENT, LETTER, DOCUMENT, ENCOUNTER, APPOINTMENT
	};

	public Category getCategory();

	public String getLabel();

	/**
	 * Depending on Category this string contains information on how to load the
	 * resp. Object.<br>
	 * PATIENT: contactId in KONTAKT table<br>
	 * ENCOUNTER: encounterId in BEHANDLUNGEN table<br>
	 *
	 * @return
	 */
	public String getLoaderString();

	/**
	 * The object identified in {@link #getLoaderString()} may or may have not been
	 * already loaded. The first loader will set it here for others to re-use.
	 *
	 * @return
	 */
	public Optional<Object> getObject();

	public void setObject(Object object);
}
