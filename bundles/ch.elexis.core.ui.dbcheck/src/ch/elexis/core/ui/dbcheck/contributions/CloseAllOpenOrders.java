package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.BestellungEntry;
import ch.elexis.data.Query;

public class CloseAllOpenOrders extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(final IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Bitte warten, Bestellungen werden geschlossen ...", IProgressMonitor.UNKNOWN);
		
		Query<BestellungEntry> qre = new Query<BestellungEntry>(BestellungEntry.class);
		qre.add(BestellungEntry.FLD_STATE, Query.NOT_EQUAL,
			Integer.toString(BestellungEntry.STATE_DONE));
		List<BestellungEntry> openEntries = qre.execute();
		for (BestellungEntry bestellungEntry : openEntries) {
			bestellungEntry.setState(BestellungEntry.STATE_DONE);
		}
		
		output.append(openEntries.size() + " offene Bestelleinträge geschlossen.\n");
		
		pm.done();
		
		return output.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Alle offenen Bestellungen schliessen";
	}
	
}
