
package ch.elexis.core.ui.tasks.parts.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;
import jakarta.inject.Named;

public class DeleteTaskDescriptorHandler {

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ITaskDescriptor taskDescriptor) {
		TaskModelServiceHolder.get().delete(taskDescriptor);
	}

}