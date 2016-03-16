package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.OverwriteAllImportHandler;

/**
 * Use for testing purposes only
 */
public class TestHL7Parser extends HL7Parser {
	
	public TestHL7Parser(String mylab){
		super(mylab, new ImporterPatientResolver(), new LabImportUtil(),
			new OverwriteAllImportHandler(), new DefaultLabContactResolver(),
			CoreHub.localCfg.get(CFG_IMPORT_ENCDATA, false));
	}
	
}
