package ch.elexis.core.services.es;

import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import ch.elexis.core.common.InstanceStatus;

public class RefreshTask extends TimerTask {

	private ElexisServerService elexisServerService;

	public RefreshTask(ElexisServerService elexisServerService) {
		this.elexisServerService = elexisServerService;
	}

	@Override
	public void run() {
		try {
			elexisServerService.validateElexisServerConnection();

			InstanceStatus instanceStatus = elexisServerService.createInstanceStatus();
			elexisServerService.updateInstanceStatus(instanceStatus);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("run problem", e);
		}
	}

}
