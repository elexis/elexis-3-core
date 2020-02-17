
package ch.elexis.core.ui.tasks.parts.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.jface.viewers.IFilter;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.tasks.parts.TaskResultPart;

public class TaskResultPartTableFilterHandler {
	
	private IFilter showFailuresOnlyFilter;
	
	@Execute
	public void execute(MPart part, MDirectToolItem item){
		TaskResultPart taskResultPart = (TaskResultPart) part.getObject();
		
		if (item.isSelected()) {
			if (showFailuresOnlyFilter == null) {
				showFailuresOnlyFilter = (object) -> !((ITask) object).isSucceeded();
			}
			
			taskResultPart.getContentProvider().setFilter(showFailuresOnlyFilter);
		} else {
			taskResultPart.getContentProvider().setFilter(AcceptAllFilter.getInstance());
		}
		
	}
	
}