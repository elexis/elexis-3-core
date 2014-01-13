package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.ui.importer.div.importers.HL7.OBX;

public interface ILabItemResolver {
	public String getTestName(OBX obx);
	
	public String getTestGroupName(OBX obx);
	
	public String getNextTestGroupSequence(OBX obx);
}
