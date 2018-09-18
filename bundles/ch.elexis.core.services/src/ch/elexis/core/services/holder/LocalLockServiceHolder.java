package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.ILocalLockService;

@Component
public class LocalLockServiceHolder {
	
	private static ILocalLockService localLockService;
	
	@Reference
	public void setLocalLockService(ILocalLockService localLockService){
		LocalLockServiceHolder.localLockService = localLockService;
	}
	
	public static ILocalLockService get(){
		return localLockService;
	}
}
