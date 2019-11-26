
package ch.elexis.core.ui.tasks.parts;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.tasks.model.ITask;

public class RemoveIdentifiableHandler {
	
	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<ITask> task){
		System.out.println("a" + task);
	}
	
//	@Execute
//	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ITask task){
//		System.out.println("b" + task);
//	}
		
}