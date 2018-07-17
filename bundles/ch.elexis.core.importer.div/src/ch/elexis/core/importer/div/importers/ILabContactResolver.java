package ch.elexis.core.importer.div.importers;

import ch.elexis.core.model.ILaboratory;

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
	public ILaboratory getLabContact(String identifier, String sendingFacility);
}
