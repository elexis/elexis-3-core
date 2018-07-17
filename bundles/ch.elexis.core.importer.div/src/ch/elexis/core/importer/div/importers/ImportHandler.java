package ch.elexis.core.importer.div.importers;

import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;

public abstract class ImportHandler {

		public enum OverwriteState {
				OVERWRITE, OVERWRITEALL, IGNORE
		}
		
		public abstract OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
			TransientLabResult newResult);

}
