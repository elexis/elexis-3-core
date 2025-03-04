package ch.elexis.core.ui.tasks.parts.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.services.IElexisServerService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import jakarta.inject.Named;

public class TriggerTaskDescriptorOnESHandler {

	@Execute
	public void execute(@Optional IElexisServerService elexisServerService,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ITaskDescriptor taskDescriptor) {

		if (elexisServerService != null && taskDescriptor != null && elexisServerService.deliversRemoteEvents()) {
			ElexisEvent elexisEvent = new ElexisEvent();
			elexisEvent.setTopic(ElexisEventTopics.TASK_SERVICE + "trigger");
			elexisEvent.getProperties().put(ElexisEventTopics.PROPKEY_ID, taskDescriptor.getId());
			elexisServerService.postEvent(elexisEvent);
		}

	}

	@CanExecute
	public boolean canExecute(@Optional IElexisServerService elexisServerService,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ITaskDescriptor taskDescriptor) {
		return elexisServerService != null && elexisServerService.deliversRemoteEvents();
	}

}
