package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Fall.Tiers;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

public class SetFallCopyForPatientWithTel extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder sb = new StringBuilder();
		
		Query<Fall> query = new Query<Fall>(Fall.class);
		List<Fall> allFaelle = query.execute();
		sb.append(allFaelle.size() + " Fälle insgesamt.\n");
		pm.beginTask("Set copy to patient...", allFaelle.size());
		boolean setValue = true;
		int changedFaelle = 0;
		for (Fall fall : allFaelle) {
			Patient patient = fall.getPatient();
			if (patient != null && StringUtils.isNotBlank(patient.getMailAddress())
				&& StringUtils.isNotBlank(patient.getNatel())) {
				if (fall.isOpen() && fall.getTiersType() == Tiers.PAYANT) {
					if (setValue != fall.getCopyForPatient()) {
						changedFaelle++;
					}
					fall.setCopyForPatient(setValue);
				}
			}
			pm.worked(1);
		}
		sb.append("Kopie an Patienten (E-Mail & Mobil vorhanden), in " + changedFaelle
			+ " TP Fällen angepasst.");
		return sb.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Kopie an Patienten für TP Fälle (E-Mail & Mobil vorhanden)";
	}
	
}
