package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IElexisServerService;

@Component
public class ElexisServerServiceHolder {

	private static IElexisServerService elexisServerService;

	@Reference
	public void setElexisServerService(IElexisServerService elexisServerService) {
		ElexisServerServiceHolder.elexisServerService = elexisServerService;
	}

	public static IElexisServerService get() {
		return elexisServerService;
	}
}
