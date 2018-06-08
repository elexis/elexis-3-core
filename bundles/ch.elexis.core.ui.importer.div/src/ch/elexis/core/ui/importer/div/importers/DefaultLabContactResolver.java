package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.data.interfaces.IContact;
import ch.elexis.core.importer.div.importers.ILabContactResolver;

public class DefaultLabContactResolver implements ILabContactResolver {
	
	@Override
	public IContact getLabContact(String identifier, String sendingFacility){
		return new ContactBean(LabImportUtil.getOrCreateLabor(identifier));
	}
}
