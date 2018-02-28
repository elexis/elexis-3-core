package ch.elexis.core.findings.util.commands;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.util.FindingsServiceHolder;
import ch.elexis.core.findings.util.ModelUtil;

/**
 * Delete the {@link IFinding} with all its children. Locking of the {@link IFinding} should be
 * handled by the caller, locking of the children is handled by the command.
 * 
 * @author thomas
 *
 */
public class FindingDeleteCommand implements IFindingCommand {
	private IFinding iFinding;
	private ILockingProvider locking;
	
	public FindingDeleteCommand(IFinding iFinding, ILockingProvider locking){
		this.iFinding = iFinding;
		this.locking = locking;
	}
	
	/**
	 * Execute the command. If the locking of one of the findings failed, deletion is aborted.
	 * 
	 * @throws ElexisException
	 *             if locking failed
	 */
	public void execute() throws ElexisException{
		if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iFinding;
			IObservation rootObservation =
				ModelUtil.getRootObservationRecursive(iObservation);
			
			List<IObservation> observationChildrens =
				ModelUtil.getObservationChildren(iObservation, new ArrayList<>(), 100);
			
			List<IObservation> lockedChildrens = new ArrayList<>();
			for (IObservation iObservationChild : observationChildrens) {
				if (!locking.acquireLock(iObservationChild).isOk()) {
					throw new ElexisException("Delete not possible, lock acquire failed");
				}
				lockedChildrens.add(iObservationChild);
			}
			
			// do the deletion
			for (IObservation child : lockedChildrens) {
				List<IObservation> sources = child.getSourceObservations(ObservationLinkType.REF);
				for (IObservation source : sources) {
					child.removeSourceObservation(source, ObservationLinkType.REF);
					source.removeTargetObservation(child, ObservationLinkType.REF);
				}
				FindingsServiceHolder.getiFindingsService().deleteFinding(child);
				locking.releaseLock(child);
			}
			
			List<IObservation> sources =
				iObservation.getSourceObservations(ObservationLinkType.REF);
			for (IObservation source : sources) {
				iObservation.removeSourceObservation(source, ObservationLinkType.REF);
				source.removeTargetObservation(iObservation, ObservationLinkType.REF);
			}
			FindingsServiceHolder.getiFindingsService().deleteFinding(iObservation);
			
			if (rootObservation != iObservation) {
				new UpdateFindingTextCommand(rootObservation).execute();
			}
		}
	}
}
