package ch.elexis.core.ui.dbcheck.contributions;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;

public class FixEmptyDiagnoseKonsultation extends ExternalMaintenance {
	private HashMap<Mandant, Integer> missingMap;
	private HashMap<Mandant, String> mandantDiagnoseMap;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
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
				
				String diagnoseId =
					CoreHub.getUserSetting(mandant).get(Preferences.USR_DEFDIAGNOSE, "");
				String diagnoseLabel = null;
				
				// add the diagnose if default diagnose is defined
				if (diagnoseId != null && !diagnoseId.isEmpty()) {
					IDiagnose diagnose = (IDiagnose) CoreHub.poFactory.createFromString(diagnoseId);
					if (diagnose == null) {
						diagnoseLabel = diagnoseId + " existiert nicht";
					} else {
						k.addDiagnose(diagnose);
						diagnoseLabel = diagnose.getLabel();
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
				output.append(mandant.getVorname() + " " + mandant.getName() + " ("
					+ mandant.getLabel() + "): " + result
					+ " Konsultationen ohne Diagnose (keine Standarddiagnose definiert)\n");
			} else {
				output.append(mandant.getVorname() + " " + mandant.getName() + " ("
					+ mandant.getLabel() + "): " + result
					+ " Konsultationen mit Standarddiagnose (" + diagnose + ") vervollständigt\n");
			}
		}
		pm.worked(1);
		
		pm.done();
		return output.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Standarddiagnose für offene Konsultationen ohne Diagnose eintragen";
	}
	
}
