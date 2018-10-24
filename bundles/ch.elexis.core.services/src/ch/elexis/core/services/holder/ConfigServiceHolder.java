package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IConfigService;

@Component
public class ConfigServiceHolder {
	
	private static IConfigService configService;
	
	@Reference
	public void setModelService(IConfigService modelService){
		ConfigServiceHolder.configService = modelService;
	}
	
	public static IConfigService get(){
		if (configService == null) {
			throw new IllegalStateException("No IConfigService available");
		}
		return configService;
	}
}
