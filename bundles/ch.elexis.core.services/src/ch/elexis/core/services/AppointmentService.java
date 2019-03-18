package ch.elexis.core.services;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IAppointment;

@Component
public class AppointmentService implements IAppointmentService {
	
	@Override
	public IAppointment clone(IAppointment appointment){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean delete(IAppointment appointment, boolean whole){
		// TODO Auto-generated method stub
		return false;
	}
	
}
