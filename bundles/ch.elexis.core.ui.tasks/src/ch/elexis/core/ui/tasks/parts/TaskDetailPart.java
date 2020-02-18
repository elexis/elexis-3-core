
package ch.elexis.core.ui.tasks.parts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
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
	Shell shell;
	
	@Inject
	ITaskResultDetailContributions taskResultDetailDialogContributions;
	
	@Inject
	ECommandService commandService;
	
	@Inject
	EHandlerService handlerService;
	
	@PostConstruct
	public void postConstruct(Composite parent, MPart part){
		parent.setLayout(new GridLayout(1, false));
		
		ITask task = (ITask) part.getTransientData().get("task");
		part.setIconURI(TaskResultLabelProvider.getInstance().getIconURI(task));
		
		String runAt;
		LocalDateTime _runAt = task.getRunAt();
		if(_runAt != null) {
			runAt = _runAt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
		} else {
			runAt = "queued";
		}
	
		String partLabel = task.getTaskDescriptor().getReferenceId() + " - " + runAt;
		part.setLabel(partLabel);
		
		Map<String, Object> e4Services = new HashMap<String, Object>();
		e4Services.put(ECommandService.class.getName(), commandService);
		e4Services.put(EHandlerService.class.getName(), handlerService);
		
		taskResultDetailDialogContributions.createDetailCompositeForTask(parent, task, e4Services);
	}
	
}