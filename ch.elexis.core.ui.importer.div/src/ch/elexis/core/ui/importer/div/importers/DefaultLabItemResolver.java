package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.ui.importer.div.importers.HL7.OBX;
import ch.rgw.tools.TimeTool;

public class DefaultLabItemResolver implements ILabItemResolver {
	
	private String dat;
	private int sequence;
	
	public DefaultLabItemResolver(){
		dat = new TimeTool().toString(TimeTool.DATE_GER);
		sequence = 0;
	}
	
	@Override
	public String getTestName(OBX obx){
		return obx.getItemName();
	}
	
	@Override
	public String getTestGroupName(OBX obx){
		return Messages.HL7Parser_AutomaticAddedGroup + dat;
	}
	
	@Override
	public String getNextTestGroupSequence(OBX obx){
		return Integer.toString(sequence++);
	}
	
}
