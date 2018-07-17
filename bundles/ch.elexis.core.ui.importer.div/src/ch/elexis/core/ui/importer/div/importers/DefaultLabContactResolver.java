package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.ui.importer.div.services.LabImportUtilHolder;

public class DefaultLabContactResolver implements ILabContactResolver {
	
	@Override
	public ILaboratory getLabContact(String identifier, String sendingFacility){
		return LabImportUtilHolder.get().getOrCreateLabor(identifier);
	}
}
