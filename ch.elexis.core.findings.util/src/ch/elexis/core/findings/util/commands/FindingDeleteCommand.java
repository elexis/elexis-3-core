package ch.elexis.core.findings.util.commands;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.util.FindingsServiceHolder;


public class FindingDeleteCommand implements IFindingCommand {
	private IFinding iFinding;
	
	public FindingDeleteCommand(IFinding iFinding){
		this.iFinding = iFinding;
	}
	
	public void execute(){
		FindingsServiceHolder.getiFindingsService().deleteFinding(iFinding);
	}
}
