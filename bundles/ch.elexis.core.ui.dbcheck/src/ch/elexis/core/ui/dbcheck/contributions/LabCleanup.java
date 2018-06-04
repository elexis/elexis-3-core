package ch.elexis.core.ui.dbcheck.contributions;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;

public class LabCleanup extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		Query<Patient> qp = new Query<Patient>(Patient.class);
		List<Patient> results = qp.execute();
		int deletedCount = 0;
		int allCount = 0;
		// search for duplicate LabResult, get grouped already groups by date
		for (Patient patient : results) {
			HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> allGrouped =
				LabResult.getGrouped(patient);
			Set<String> groups = allGrouped.keySet();
			for (String group : groups) {
				HashMap<String, HashMap<String, List<LabResult>>> groupAll = allGrouped.get(group);
				Set<String> items = groupAll.keySet();
				for (String item : items) {
					HashMap<String, List<LabResult>> itemAll = groupAll.get(item);
					Set<String> days = itemAll.keySet();
					for (String day : days) {
						List<LabResult> dayAll = itemAll.get(day);
						// here we are list of results of item and day lets filter
						deletedCount += filterResults(dayAll);
						allCount += dayAll.size();
					}
				}
			}
			// cache Map in SoftCache is a memory leak on heavy use ... so resetCache 
			PersistentObject.resetCache();
		}
		return deletedCount + " Werte wurden entfernt.\n " + allCount + " Werte insgesamt.";
	}
	
	private int filterResults(List<LabResult> dayAll){
		int deleted = 0;
		LabResult previous = null;
		for (LabResult labResult : dayAll) {
			// remove empty
			if (labResult.getResult().isEmpty()) {
				labResult.delete();
				deleted++;
				continue;
			}
			if (previous != null) {
				if (sameResult(previous, labResult)) {
					labResult.delete();
					deleted++;
					continue;
				}
			}
			previous = labResult;
		}
		return deleted;
	}
	
	/**
	 * Make sure we got the same result, false positives could be really bad.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private boolean sameResult(LabResult left, LabResult right){
		if (!left.getResult().equals(right.getResult())) {
			return false;
		}
		if (!left.getUnit().equals(right.getUnit())) {
			return false;
		}
		if (!left.getRefFemale().equals(right.getRefFemale())) {
			return false;
		}
		if (!left.getRefMale().equals(right.getRefMale())) {
			return false;
		}
		
		TimeTool leftObsTime = left.getObservationTime();
		TimeTool rightObsTime = right.getObservationTime();
		if ((leftObsTime == null && rightObsTime != null)
			|| (leftObsTime != null && rightObsTime == null)) {
			return false;
		}
		if (leftObsTime != null && rightObsTime != null) {
			if (!leftObsTime.isEqual(rightObsTime)) {
				return false;
			}
		}
		TimeTool leftAnalyseTime = left.getAnalyseTime();
		TimeTool rightAnalyseTime = right.getAnalyseTime();
		if ((leftAnalyseTime == null && rightAnalyseTime != null)
			|| (leftAnalyseTime != null && rightAnalyseTime == null)) {
			return false;
		}
		if (leftAnalyseTime != null && rightAnalyseTime != null) {
			if (!leftAnalyseTime.isEqual(rightAnalyseTime)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Mehrfache und leere Laborwerte entfernen.";
	}
}
