package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IAccessControlService;

@Component
public class AccessControlServiceHolder {
	
	private static IAccessControlService accessControlService;
	
	@Reference
	public void setContextService(IAccessControlService accessControlService){
		AccessControlServiceHolder.accessControlService = accessControlService;
	}
	
	public static IAccessControlService get(){
		if (accessControlService == null) {
			throw new IllegalStateException("No IAccessControlService available");
		}
		return accessControlService;
	}
	
}
