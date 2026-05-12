package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IAppointmentService;

public class AppointmentServiceHolder {

	public static IAppointmentService get() {
		return PortableServiceLoader.get(IAppointmentService.class);
	}
}
