package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;

public class RemoveInvalidEncounters extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		int checked = 0;
		int deleted = 0;

		Query<Konsultation> query = new Query<Konsultation>(Konsultation.class);
		List<Konsultation> encounters = query.execute();
		pm.beginTask("Delete invalid encounters", encounters.size());
		for (Konsultation encounter : encounters) {
			checked++;
			if (encounter.getFall() == null || !encounter.getFall().exists()) {
				encounter.delete();
				deleted++;
			} else if (encounter.getFall().getPatient() == null || !encounter.getFall().getPatient().exists()) {
				encounter.delete();
				deleted++;
			}
			if (pm.isCanceled()) {
				break;
			}
			pm.worked(1);
		}

		pm.done();
		return deleted + " invalid encounters deleted of " + checked + " overall";
	}

	@Override
	public String getMaintenanceDescription() {
		return "Delete invalid encounters";
	}

}
