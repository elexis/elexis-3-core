
package ch.elexis.core.ui.tasks.parts;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.ui.tasks.ITaskResultDetailContributions;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class TaskLogDetailPart {

	@Inject
	ITaskResultDetailContributions taskResultDetailDialogContributions;

	@Inject
	ECommandService commandService;

	@Inject
	EHandlerService handlerService;

	@PostConstruct
	public void postConstruct(Composite parent, MPart part) {
		parent.setLayout(new GridLayout(1, false));

		ITask task = (ITask) part.getTransientData().get("task"); //$NON-NLS-1$
		part.setIconURI(TaskResultLabelProvider.getInstance().getIconURI(task));

		String runAt;
		LocalDateTime _runAt = task.getRunAt();
		if (_runAt != null) {
			runAt = TimeUtil.formatSafe(_runAt);
		} else {
			runAt = "queued"; //$NON-NLS-1$
		}

		String partLabel = task.getTaskDescriptor().getReferenceId() + " - " + runAt; //$NON-NLS-1$
		part.setLabel(partLabel);

		Map<String, Object> e4Services = new HashMap<>();
		e4Services.put(ECommandService.class.getName(), commandService);
		e4Services.put(EHandlerService.class.getName(), handlerService);

		taskResultDetailDialogContributions.createDetailCompositeForTask(parent, task, e4Services);
	}

}