package ch.elexis.core.findings.util.commands;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.util.FindingsTextUtil;
import ch.elexis.core.findings.util.ModelUtil;

/**
 * Update the the text of the {@link IFinding}. If the {@link IFinding} is an instance of an
 * {@link IObservation} the text of all linked {@link IObservation}s are also updated.
 * 
 * @author thomas
 *
 */
public class UpdateFindingTextCommand implements IFindingCommand {
	
	private IFinding iFinding;
	
	public UpdateFindingTextCommand(IFinding finding){
		this.iFinding = finding;
	}
	
	@Override
	public void execute() throws ElexisException{
		IFinding rootFinding = getRootFinding();
		if (rootFinding instanceof IObservation) {
			ObservationType observationType = ((IObservation) rootFinding).getObservationType();
			if (observationType == ObservationType.REF) {
				FindingsTextUtil.getGroupText(((IObservation) rootFinding), true);
			} else {
				FindingsTextUtil.getObservationText(((IObservation) rootFinding), true);
			}
		}
	}
	
	private IFinding getRootFinding(){
		IFinding rootFinding = null;
		if (iFinding instanceof IObservation) {
			rootFinding = ModelUtil.getRootObservationRecursive((IObservation) iFinding);
		}
		return rootFinding;
	}
}