package ch.elexis.core.ui.dbcheck.contributions;

import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;

public class FixEmptyDiagnoseKonsultation extends ExternalMaintenance {
	private HashMap<Mandant, Integer> missingMap;
	private HashMap<Mandant, String> mandantDiagnoseMap;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		missingMap = new HashMap<Mandant, Integer>();
		mandantDiagnoseMap = new HashMap<Mandant, String>();
		StringBuilder output = new StringBuilder();
		pm.beginTask("Fixing consultations with no diagnose", 3);

		pm.subTask("Find all consultations ...");
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_BILL_ID, StringConstants.EMPTY, null);
		List<Konsultation> kons = qbe.execute();
		pm.worked(1);

		pm.subTask("Find consultations without diagnose ...");
		for (Konsultation k : kons) {
			Fall fall = k.getFall();
			if (fall != null && fall.exists() && fall.isOpen() && k.getDiagnosen().size() < 1) {
				Mandant mandant = k.getMandant();

				String diagnoseId = CoreHub.getUserSetting(mandant).get(Preferences.USR_DEFDIAGNOSE, StringUtils.EMPTY);
				String diagnoseLabel = null;

				// add the diagnose if default diagnose is defined
				if (diagnoseId != null && !diagnoseId.isEmpty()) {
					Optional<Identifiable> diagnose = StoreToStringServiceHolder.get().loadFromString(diagnoseId);
					if (diagnose.isPresent()) {
						IEncounter encounter = NoPoUtil.loadAsIdentifiable(k, IEncounter.class).get();
						encounter.addDiagnosis((IDiagnosis) diagnose.get());
						CoreModelServiceHolder.get().save(encounter);
						diagnoseLabel = diagnose.get().getLabel();
					} else {
						diagnoseLabel = diagnoseId + " existiert nicht";
					}
				}

				// add to the map for output in the end
				int found = 1;
				if (missingMap.containsKey(mandant)) {
					found = missingMap.get(mandant) + 1;
				}
				missingMap.put(mandant, found);

				if (!mandantDiagnoseMap.containsKey(mandant)) {
					mandantDiagnoseMap.put(mandant, diagnoseLabel);
				}
			}
		}
		pm.worked(1);

		pm.subTask("Show results ...");
		for (Mandant mandant : mandantDiagnoseMap.keySet()) {
			String diagnose = mandantDiagnoseMap.get(mandant);
			int result = missingMap.get(mandant);

			// no default diagnose set
			if (diagnose == null) {
				output.append(mandant.getVorname() + StringUtils.SPACE + mandant.getName() + " (" + mandant.getLabel()
						+ "): " + result + " Konsultationen ohne Diagnose (keine Standarddiagnose definiert)\n");
			} else {
				output.append(mandant.getVorname() + StringUtils.SPACE + mandant.getName() + " (" + mandant.getLabel()
						+ "): " + result + " Konsultationen mit Standarddiagnose (" + diagnose + ") vervollständigt\n");
			}
		}
		pm.worked(1);

		pm.done();
		return output.toString();
	}

	@Override
	public String getMaintenanceDescription() {
		return "Standarddiagnose für offene Konsultationen ohne Diagnose eintragen";
	}

}
