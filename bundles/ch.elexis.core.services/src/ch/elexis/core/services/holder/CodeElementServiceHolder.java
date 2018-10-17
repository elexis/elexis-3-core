package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.ICodeElementService;

@Component
public class CodeElementServiceHolder {
	private static ICodeElementService codeElementService;
	
	@Reference
	public void setContextService(ICodeElementService codeElementService){
		CodeElementServiceHolder.codeElementService = codeElementService;
	}
	
	public static ICodeElementService get(){
		return codeElementService;
	}
}
