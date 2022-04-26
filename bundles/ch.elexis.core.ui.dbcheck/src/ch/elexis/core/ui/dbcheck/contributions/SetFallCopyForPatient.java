package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Fall.Tiers;
import ch.elexis.data.Query;

public class SetFallCopyForPatient extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		StringBuilder sb = new StringBuilder();

		Query<Fall> query = new Query<Fall>(Fall.class);
		List<Fall> allFaelle = query.execute();
		sb.append(allFaelle.size() + " Fälle insgesamt.\n");
		pm.beginTask("Set copy to patient...", allFaelle.size());
		boolean setValue = true;
		int tpFaelle = 0;
		int changedFaelle = 0;
		for (Fall fall : allFaelle) {
			if (fall.isOpen() && fall.getTiersType() == Tiers.PAYANT) {
				tpFaelle++;
				if (setValue != fall.getCopyForPatient()) {
					changedFaelle++;
				}
				fall.setCopyForPatient(setValue);

			}
			pm.worked(1);
		}
		sb.append("Kopie an Patienten, in " + changedFaelle + " von " + tpFaelle + " TP Fällen angepasst.");
		return sb.toString();
	}

	@Override
	public String getMaintenanceDescription() {
		return "Kopie an Patienten, aller editierbaren TP Fälle setzen";
	}

}
