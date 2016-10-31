package ch.elexis.core.findings.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.migration.IMigratorService;

@Component
public class MigratorServiceComponent {
	private static IMigratorService migratorService;
	
	@Reference(unbind = "-")
	public void setFindingMigratorService(IMigratorService migratorService){
		MigratorServiceComponent.migratorService = migratorService;
	}
	
	public static IMigratorService getService(){
		return migratorService;
	}
}
