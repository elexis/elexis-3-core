package ch.elexis.core.findings.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.IValueSetService;

@Component(service = {})
public class ValueSetServiceHolder {
	
	private static IValueSetService iValueSetService;
	
	@Reference(unbind = "-")
	public void setIValueSetService(IValueSetService iValueSetService){
		ValueSetServiceHolder.iValueSetService = iValueSetService;
	}
	
	public static IValueSetService getIValueSetService(){
		return iValueSetService;
	}
}
