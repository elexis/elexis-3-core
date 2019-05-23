package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IAccountService;

@Component
public class AccountServiceHolder {
	private static IAccountService accountService;
	
	@Reference
	public void setAppointmentService(IAccountService accountService){
		AccountServiceHolder.accountService = accountService;
	}
	
	public static IAccountService get(){
		return accountService;
	}
}
