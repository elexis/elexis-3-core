package ch.elexis.core.findings.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.IFindingsService;

@Component(service = {})
public class FindingsServiceHolder {
	
	private static IFindingsService iFindingsService;
	
	@Reference(unbind = "-")
	public void setiFindingsService(IFindingsService iFindingsService){
		FindingsServiceHolder.iFindingsService = iFindingsService;
	}
	
	public static IFindingsService getiFindingsService(){
		return iFindingsService;
	}
}
