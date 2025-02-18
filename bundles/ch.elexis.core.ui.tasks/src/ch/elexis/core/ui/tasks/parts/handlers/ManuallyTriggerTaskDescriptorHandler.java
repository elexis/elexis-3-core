
package ch.elexis.core.ui.tasks.parts.handlers;

import java.util.Collections;
import java.util.Objects;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;
import jakarta.inject.Named;

public class ManuallyTriggerTaskDescriptorHandler {

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ITaskDescriptor taskDescriptor,
			IContextService contextService, ITaskService taskService) {

		try {
			if (canExecute(taskDescriptor, contextService)) {
				taskService.trigger(taskDescriptor, null, TaskTriggerType.MANUAL, Collections.emptyMap());
			}

		} catch (TaskException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ITaskDescriptor taskDescriptor,
			IContextService contextService) {

		IUser user = contextService.getActiveUser().orElse(null);
		if (user != null) {
			return (user.isAdministrator() || Objects.equals(taskDescriptor.getOwner(), user));
		}

		return false;
	}

}