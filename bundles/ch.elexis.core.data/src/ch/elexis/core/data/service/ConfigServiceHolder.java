package ch.elexis.core.data.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IConfigService;

@Component
public class ConfigServiceHolder {
	
	private static IConfigService configService;
	
	@Reference
	public void setConfigService(IConfigService configService){
		ConfigServiceHolder.configService = configService;
	}
	
	public static IConfigService get(){
		return configService;
	}
}
