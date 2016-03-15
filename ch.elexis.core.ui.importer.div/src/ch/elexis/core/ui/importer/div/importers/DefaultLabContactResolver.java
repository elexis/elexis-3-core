package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.model.IContact;

public class DefaultLabContactResolver implements ILabContactResolver {
	
	@Override
	public IContact getLabContact(String identifier, String sendingFacility){
		return new ContactBean(LabImportUtil.getOrCreateLabor(identifier));
	}
}
