package ch.elexis.core.services.es;

import java.util.TimerTask;

import ch.elexis.core.common.InstanceStatus;

public class RefreshTask extends TimerTask {

	private ElexisServerService elexisServerService;

	public RefreshTask(ElexisServerService elexisServerService) {
		this.elexisServerService = elexisServerService;
	}

	@Override
	public void run() {
		elexisServerService.validateElexisServerConnection();

		InstanceStatus instanceStatus = elexisServerService.createInstanceStatus();
		elexisServerService.updateInstanceStatus(instanceStatus);
	}

}
