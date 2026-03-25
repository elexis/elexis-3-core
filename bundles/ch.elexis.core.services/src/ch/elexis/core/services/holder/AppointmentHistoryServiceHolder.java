package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IAppointmentHistoryManagerService;

public class AppointmentHistoryServiceHolder {

	public static IAppointmentHistoryManagerService get() {
		return PortableServiceLoader.get(IAppointmentHistoryManagerService.class);
	}
}
