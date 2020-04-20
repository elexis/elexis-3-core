package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;

public class PopulateAnschriftColumnInKontaktTable extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		List<Kontakt> contacts = new Query<Kontakt>(Kontakt.class).execute();
		pm.beginTask("Setze Standard-Werte bei leeren Anschriften-Spalten in Kontakte-Tabelle", contacts.size());
		for (Kontakt contact : contacts) {
			contact.getPostAnschrift();
			pm.worked(1);
		}
		pm.done();

		return "ok";
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Standard-Werte bei leeren Anschriften-Spalten in Kontakte-Tabelle setzen";
	}
	
}
