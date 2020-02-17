
package ch.elexis.core.ui.tasks.parts.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;

public class RemoveIdentifiableHandler {
	
	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<ITask> task){
		TaskModelServiceHolder.get().remove(task);
	}
		
}