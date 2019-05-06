package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IXidService;

@Component
public class XidServiceHolder {
	private static IXidService xidService;
	
	@Reference
	public void setStoreToStringService(IXidService xidService){
		XidServiceHolder.xidService = xidService;
	}
	
	public static IXidService get(){
		if (xidService == null) {
			throw new IllegalStateException("No IXidService available");
		}
		return xidService;
	}
}
