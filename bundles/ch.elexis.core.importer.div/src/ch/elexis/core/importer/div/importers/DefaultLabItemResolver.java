package ch.elexis.core.importer.div.importers;

import ch.elexis.hl7.model.AbstractData;
import ch.rgw.tools.TimeTool;

public class DefaultLabItemResolver implements ILabItemResolver {

	private String dat;
	private int sequence;

	public DefaultLabItemResolver() {
		dat = new TimeTool().toString(TimeTool.DATE_GER);
		sequence = 0;
	}

	@Override
	public String getTestName(AbstractData data) {
		return data.getName();
	}

	@Override
	public String getTestGroupName(AbstractData data) {
		return Messages.Core_HL7_Z_automatic + dat;
	}

	@Override
	public String getNextTestGroupSequence(AbstractData data) {
		return Integer.toString(sequence++);
	}
}
