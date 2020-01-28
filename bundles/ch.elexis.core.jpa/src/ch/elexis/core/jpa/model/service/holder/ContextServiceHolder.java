package ch.elexis.core.jpa.model.service.holder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IContact;
import ch.elexis.core.services.IContextService;


@Component
public class ContextServiceHolder {
	
	private static IContextService contextService;
	
	@Reference
	public void setContextService(IContextService contextService){
		ContextServiceHolder.contextService = contextService;
	}
	
	public static IContextService get(){
		if (contextService == null) {
			throw new IllegalStateException("No IContextService available");
		}
		return contextService;
	}
	
	public static boolean isPresent(){
		return contextService != null;
	}
	
	public static Optional<IContact> getActiveUserContact(){
		return isPresent() ? contextService.getActiveUserContact() : Optional.empty();
	}
}
