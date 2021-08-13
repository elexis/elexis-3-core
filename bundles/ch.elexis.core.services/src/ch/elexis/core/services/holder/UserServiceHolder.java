package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IUserService;

@Component
public class UserServiceHolder {
	
	private static IUserService userService;
	
	@Reference
	public void setInvoiceService(IUserService userService){
		UserServiceHolder.userService = userService;
	}
	
	public static IUserService get(){
		return userService;
	}
}
