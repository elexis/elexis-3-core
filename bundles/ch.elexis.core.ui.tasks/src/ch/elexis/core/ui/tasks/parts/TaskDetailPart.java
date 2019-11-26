
package ch.elexis.core.ui.tasks.parts;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.tasks.ITaskResultDetailContributions;

public class TaskDetailPart {
	
	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;
	
	@Inject
	private ITaskResultDetailContributions taskResultDetailDialogContributions;
	
	@PostConstruct
	public void postConstruct(Composite parent, MPart part){
		parent.setLayout(new GridLayout(1, false));
		
		ITask task = (ITask) part.getTransientData().get("task");
		part.setIconURI(TaskResultLabelProvider.getInstance().getIconURI(task));
		
		String runAt = task.getRunAt().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
		String partLabel = task.getTaskDescriptor().getReferenceId() + " - " + runAt;
		part.setLabel(partLabel);
		
		taskResultDetailDialogContributions.createDetailCompositeForTask(parent, task);
	}
	
}