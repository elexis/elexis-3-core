package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.importer.div.importers.HL7Parser;

public class DefaultHL7Parser extends HL7Parser {
	
	public DefaultHL7Parser(String mylab){
		super(mylab, new ImporterPatientResolver(), new LabImportUtil(),
			new DefaultLabImportUiHandler(), new DefaultLabContactResolver(),
			CoreHub.localCfg.get(CFG_IMPORT_ENCDATA, false));
	}
	
}
