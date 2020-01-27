package ch.elexis.core.findings.codings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.IValueSetService;

@Component
public class ValueSetServiceComponent {
	private static IValueSetService valueSetService;
	
	@Reference
	public void setIValueSetService(IValueSetService valueSetService){
		ValueSetServiceComponent.valueSetService = valueSetService;
	}
	
	public void unsetIValueSetService(IValueSetService valueSetService){
		ValueSetServiceComponent.valueSetService = null;
	}
	
	public static IValueSetService getService(){
		if (valueSetService == null) {
			throw new IllegalStateException("No IValueSetService set");
		}
		return valueSetService;
	}
}
