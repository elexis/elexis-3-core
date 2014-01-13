package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.ui.importer.div.importers.LabImportUtil.ImportUiHandler;
import ch.elexis.core.ui.importer.div.importers.LabImportUtil.TransientLabResult;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;

public class OverwriteAllImportUiHandler extends ImportUiHandler {
	
	@Override
	protected OverwriteState askOverwrite(Patient patient, LabResult oldResult,
		TransientLabResult newResult){
		return OverwriteState.OVERWRITEALL;
	}
	
}
