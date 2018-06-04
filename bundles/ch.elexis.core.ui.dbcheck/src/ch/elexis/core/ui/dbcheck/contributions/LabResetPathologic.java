package ch.elexis.core.ui.dbcheck.contributions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.LabResult;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

public class LabResetPathologic extends ExternalMaintenance {
	
	private List<String> problems = new ArrayList<>();
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		Query<LabResult> qlr = new Query<LabResult>(LabResult.class);
		List<LabResult> results = qlr.execute();
		int size = results.size();
		pm.beginTask(getMaintenanceDescription() +" ("+size+" Laborwerte)", results.size());
		int allCount = 0;
		int changedCount = 0;
		for (LabResult labResult : results) {
			if (pm.isCanceled()) {
				addProblem("Cancelled.", labResult);
				return getProblemsString() + "\n" + changedCount + " Werte wurden geändert.\n "
					+ allCount + " Werte insgesamt.";
			}
			
			LockResponse result = CoreHub.getLocalLockService().acquireLockBlocking(labResult, 50,
				new NullProgressMonitor());
			if (result.isOk()) {
				boolean wasPathologic = labResult.isFlag(LabResultConstants.PATHOLOGIC);
				// reset patholgic by resetting ref values
				labResult.setRefFemale(labResult.getRefFemale());
				labResult.setRefMale(labResult.getRefMale());
				boolean isPathologic = labResult.isFlag(LabResultConstants.PATHOLOGIC);
				if (wasPathologic != isPathologic) {
					changedCount++;
				}
				LockResponse releaseLock =
					CoreHub.getLocalLockService().releaseLock(result.getLockInfo());
				if (!releaseLock.isOk()) {
					addProblem("Could not release lock for LabResult [" + labResult.getLabel() + "]"
						+ "[" + labResult.getId() + "]", labResult);
				}
			} else {
				addProblem("Could not acquire lock for LabResult [" + labResult.getLabel() + "]"
					+ "[" + labResult.getId() + "]", labResult);
			}
			
			allCount++;
			if ((allCount % 1000) == 0) {
				// cache Map in SoftCache is a memory leak on heavy use ... so resetCache 
				PersistentObject.resetCache();
				pm.setTaskName(getMaintenanceDescription() +" ("+size+" Laborwerte => "+allCount +" bearbeitet)");
			}
			pm.worked(1);
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		pm.done();
		return changedCount + " Werte wurden geändert.\n " + allCount + " Werte insgesamt.";
	}
	
	private void addProblem(String prefix, LabResult labResult){
		problems.add("[" + prefix + "]" + "[" + labResult.getId() + "] - [" + labResult.getLabel()
			+ "] of [" + labResult.getPatient().getLabel() + "]");
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Pathologisch bei allen Laborwerten neu setzen.";
	}
	
	private String getProblemsString(){
		if (problems != null && !problems.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nProblems:\n");
			problems.stream().forEach(problem -> sb.append(problem + "\n"));
			return sb.toString();
		}
		return "";
	}
}
