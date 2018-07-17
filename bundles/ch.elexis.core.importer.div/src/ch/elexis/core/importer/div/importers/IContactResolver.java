package ch.elexis.core.importer.div.importers;

import ch.elexis.core.model.IContact;

public interface IContactResolver<T extends IContact> {

	/**
	 * Get the contact. The message can be displayed to the user, or logged, ...
	 * 
	 * @return
	 */
	public T getContact(String message);
}
