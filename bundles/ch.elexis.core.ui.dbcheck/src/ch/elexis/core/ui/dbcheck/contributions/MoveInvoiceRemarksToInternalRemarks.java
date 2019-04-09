package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;

public class MoveInvoiceRemarksToInternalRemarks extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		
		List<Rechnung> qbe = new Query<Rechnung>(Rechnung.class).execute();
		pm.beginTask("Verschiebe Rechnungsbemerkungen in interne Bemerkungen", qbe.size());
		for (Rechnung rechnung : qbe) {
			String bemerkung = rechnung.getBemerkung();
			if (bemerkung != null) {
				rechnung.setInternalRemarks(bemerkung);
				rechnung.setBemerkung(null);
			}
			pm.worked(1);
		}
		
		pm.done();
		return null;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "[14991] Alle Rechnungsbemerkungen in interne Bemerkungen verschieben";
	}
	
}
