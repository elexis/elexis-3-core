package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.model.FallConstants;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Query;

public class MoveFallExtInfoBillingToFallBillingSystem extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder sb = new StringBuilder();
		Query<Fall> query = new Query<Fall>(Fall.class);
		query.clear(true);
		List<Fall> allFaelle = query.execute();
		pm.beginTask("Moving Fall#ExtInfo#billing values...", allFaelle.size());
		for (Fall fall : allFaelle) {
			String billingSystem =
				(String) fall.getExtInfoStoredObjectByKey(FallConstants.FLD_EXTINFO_BILLING);
			if (billingSystem != null) {
				fall.set(Fall.FLD_BILLINGSYSTEM, billingSystem);
				fall.setExtInfoStoredObjectByKey(FallConstants.FLD_EXTINFO_BILLING, null);
				sb.append("[" + fall.getId() + "] Moving Fall#ExtInfo#Billing [" + billingSystem
					+ "] to table\n");
			}
			pm.worked(1);
		}
		pm.done();
		return sb.toString();
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "[6105] Move all Fall#Extinfo#billing entries to Fall#Gesetz";
	}
	
}
