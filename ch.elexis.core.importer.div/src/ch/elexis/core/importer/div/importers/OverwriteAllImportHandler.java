package ch.elexis.core.importer.div.importers;

import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;

public class OverwriteAllImportHandler extends ImportHandler {
	
	@Override
	public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
		TransientLabResult newResult){
		return OverwriteState.OVERWRITEALL;
	}
	
}
