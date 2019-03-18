package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IAppointmentService;

@Component
public class AppointmentServiceHolder {
	private static IAppointmentService appoinemtnService;
	
	@Reference
	public void setAppointmentService(IAppointmentService appoinemtnService){
		AppointmentServiceHolder.appoinemtnService = appoinemtnService;
	}
	
	public static IAppointmentService get(){
		return appoinemtnService;
	}
}
