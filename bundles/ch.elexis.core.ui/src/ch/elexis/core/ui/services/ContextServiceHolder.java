package ch.elexis.core.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IContextService;

@Component
public class ContextServiceHolder {
	
	private static IContextService contextService;
	
	@Reference
	public void setContextService(IContextService contextService){
		ContextServiceHolder.contextService = contextService;
	}
	
	public static IContextService getService(){
		return contextService;
	}
}
