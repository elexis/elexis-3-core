package ch.elexis.core.tasks.internal.model.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.services.IContextService;

@Component
public class ContextServiceHolder {
	
	private static IContextService contextService;
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
	public void setContextService(IContextService contextService){
		ContextServiceHolder.contextService = contextService;
	}
	
	public static IContextService get(){
		return contextService;
	}
	
	public static boolean isPresent(){
		return contextService != null;
	}
}
