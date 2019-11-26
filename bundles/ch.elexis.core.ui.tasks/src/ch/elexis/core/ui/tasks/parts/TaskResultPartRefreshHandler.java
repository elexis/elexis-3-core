
package ch.elexis.core.ui.tasks.parts;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class TaskResultPartRefreshHandler {
	
	@Execute
	public void execute(MPart part){
		TaskResultPart taskResultPart = (TaskResultPart) part.getObject();
		taskResultPart.refresh();
	}
	
	@CanExecute
	public boolean canExecute(MPart part){
		return true;
	}
	
}