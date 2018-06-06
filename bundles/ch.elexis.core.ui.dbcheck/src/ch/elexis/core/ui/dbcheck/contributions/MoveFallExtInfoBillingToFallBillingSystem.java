package ch.elexis.core.ui.dbcheck.contributions;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.data.dbupdate.FallUpdatesFor36;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class MoveFallExtInfoBillingToFallBillingSystem extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		return FallUpdatesFor36.moveFallExtInfoBillingToFallBillingSystem(pm);
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "[6105] Move all Fall#Extinfo#billing entries to Fall#Gesetz";
	}
	
}
