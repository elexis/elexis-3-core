package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;

public class OverwriteAllImportUiHandler extends ImportHandler {
	
	@Override
	public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
		TransientLabResult newResult){
		return OverwriteState.OVERWRITEALL;
	}
	
}
