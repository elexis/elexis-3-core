package ch.elexis.core.ui.dbcheck.contributions;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;

public class FixOmnivoreDirectCreationDate extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		JdbcLink jdbcLink = PersistentObject.getConnection();
		String sqlOmnivoreDateFix =
			"UPDATE ch_elexis_omnivore_data SET CreationDate = Datum WHERE CreationDate IS null AND Datum IS NOT null AND deleted like '0'";
		
		int ret = jdbcLink.exec(sqlOmnivoreDateFix);
		return ret + " Omnivore Einträge repariert";
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "OmnivoreDirect (mit Scan) - Datum für Erstelldatum übernehmen";
	}
	
}
