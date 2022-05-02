package ch.elexis.core.findings.ui.dbcheck;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

public class ObservationsCleanup extends ExternalMaintenance {

	private static Logger logger = LoggerFactory.getLogger(ObservationsCleanup.class);

	private int deletedCount = 0;
	private int allCount = 0;
	private IFindingsService findingsService;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		List<Patient> allPatients = new Query<Patient>(Patient.class).execute();
		deletedCount = 0;
		allCount = 0;
		findingsService = FindingsServiceComponent.getService();
		pm.beginTask("Mehrfache und leere Observations (Befunde) entfernen", allPatients.size());
		for (Patient patient : allPatients) {
			List<IObservation> observations = findingsService.getPatientsFindings(patient.getId(), IObservation.class);
			observations = filterVitalSigns(observations);
			observations = removeNoCodeObservations(observations);
			Map<String, List<IObservation>> groupedObservations = groupObservations(observations);
			for (String key : groupedObservations.keySet()) {
				List<IObservation> matchingObservations = groupedObservations.get(key);
				allCount += matchingObservations.size();
				if (matchingObservations.size() > 1) {
					// remove all but first ...
					for (int i = 1; i < matchingObservations.size(); i++) {
						logger.info("Deleting duplicate observation " + key + " pat [" + patient.getPatCode() + "]");
						findingsService.deleteFinding(matchingObservations.get(i));
						deletedCount++;
					}
				}
			}
			pm.worked(1);
		}
		return deletedCount + " Observations wurden entfernt.\n " + allCount + " Observations insgesamt.";
	}

	private List<IObservation> filterVitalSigns(List<IObservation> observations) {
		List<IObservation> ret = new ArrayList<>();
		for (IObservation iObservation : observations) {
			if (iObservation.getCategory() == ObservationCategory.VITALSIGNS) {
				ret.add(iObservation);
			}
		}
		return ret;
	}

	private List<IObservation> removeNoCodeObservations(List<IObservation> observations) {
		List<IObservation> ret = new ArrayList<>();
		for (IObservation iObservation : observations) {
			if (iObservation.getCoding() == null || iObservation.getCoding().isEmpty()) {
				logger.info("Deleting observation without code - " + iObservation);
				findingsService.deleteFinding(iObservation);
				deletedCount++;
			} else {
				ret.add(iObservation);
			}
		}
		return ret;
	}

	/**
	 * Get a map with observations effective time, code and value as string key.
	 * Matching observations are as lists in the value of the map.
	 *
	 * @param observations
	 * @return
	 */
	private Map<String, List<IObservation>> groupObservations(List<IObservation> observations) {
		Map<String, List<IObservation>> ret = new HashMap<>();
		for (IObservation iObservation : observations) {
			String key = "[" + iObservation.getEffectiveTime().toString() + "]["
					+ iObservation.getCoding().get(0).getSystem() + "#" + iObservation.getCoding().get(0).getCode()
					+ "][" + iObservation.getText().orElse(StringUtils.EMPTY) + "]";
			List<IObservation> list = ret.get(key);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(iObservation);
			ret.put(key, list);
		}
		return ret;
	}

	@Override
	public String getMaintenanceDescription() {
		return "Mehrfache und leere Observations (Befunde) entfernen.";
	}
}
