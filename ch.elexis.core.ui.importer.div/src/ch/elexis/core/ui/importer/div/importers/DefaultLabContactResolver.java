package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.data.Labor;

public class DefaultLabContactResolver implements ILabContactResolver {
	
	@Override
	public Labor getLabContact(String identifier, String sendingFacility){
		return LabImportUtil.getOrCreateLabor(identifier);
	}
}
