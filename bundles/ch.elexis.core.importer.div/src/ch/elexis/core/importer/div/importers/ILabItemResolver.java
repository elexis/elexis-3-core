package ch.elexis.core.importer.div.importers;

import ch.elexis.hl7.model.AbstractData;

public interface ILabItemResolver {
	
	public String getTestGroupName(AbstractData data);
	
	public String getTestName(AbstractData data);
	
	public String getNextTestGroupSequence(AbstractData data);
}
