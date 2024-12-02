package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IAppointmentHistoryManagerService;

@Component
public class AppointmentHistoryServiceHolder {

	private static IAppointmentHistoryManagerService appointmentHistoryManagerService;

	@Reference
	public void setAppointmentHistoryManagerService(
			IAppointmentHistoryManagerService appointmentHistoryManagerService) {
		AppointmentHistoryServiceHolder.appointmentHistoryManagerService = appointmentHistoryManagerService;
	}

	public static IAppointmentHistoryManagerService get() {
		return appointmentHistoryManagerService;
	}
}
