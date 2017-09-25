package ch.elexis.core.findings.util.commands;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.util.FindingsServiceHolder;

public class ObservationDeleteCommand implements IFindingCommand {
	
	private IObservation iObservation;
	
	public ObservationDeleteCommand(IObservation iObservation){
		this.iObservation = iObservation;
	}
	
	@Override
	public void execute(){
		List<IObservation> childrens = getOberservationChildrens(iObservation, new ArrayList<>());
		for (IObservation iObservation : childrens) {
			FindingsServiceHolder.getiFindingsService().deleteFinding(iObservation);
		}
	}
	
	private List<IObservation> getOberservationChildrens(IObservation iObservation,
		List<IObservation> list){
		List<IObservation> refChildrens =
			iObservation.getTargetObseravtions(ObservationLinkType.REF);
		list.add(iObservation);
		
		for (IObservation child : refChildrens) {
			getOberservationChildrens(child, list);
		}
		return list;
	}
	
}
