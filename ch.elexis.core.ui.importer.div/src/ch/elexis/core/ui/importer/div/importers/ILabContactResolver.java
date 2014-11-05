package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;

public interface ILabContactResolver {
	/**
	 * resolve the labor contact
	 * 
	 * @param identifier
	 *            the identifier sent from the plugin
	 * @param sendingFacility
	 *            the identifier sent from the lab device
	 * @return the found matching lab contact
	 */
	public Labor getLabContact(String identifier, String sendingFacility);
}
