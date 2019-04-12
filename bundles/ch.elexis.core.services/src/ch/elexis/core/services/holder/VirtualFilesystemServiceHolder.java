package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IVirtualFilesystemService;

@Component
public class VirtualFilesystemServiceHolder {
	
	private static IVirtualFilesystemService virtualFilesystemService;
	
	@Reference
	public void setLocalLockService(IVirtualFilesystemService virtualFilesystemService){
		VirtualFilesystemServiceHolder.virtualFilesystemService = virtualFilesystemService;
	}
	
	public static IVirtualFilesystemService get(){
		return virtualFilesystemService;
	}
}
