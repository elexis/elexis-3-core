package ch.elexis.core.importer.div.importers;

import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.data.interfaces.IPatient;

public class OverwriteAllImportHandler extends ImportHandler {
	
	@Override
	public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
		TransientLabResult newResult){
		return OverwriteState.OVERWRITEALL;
	}
	
}
