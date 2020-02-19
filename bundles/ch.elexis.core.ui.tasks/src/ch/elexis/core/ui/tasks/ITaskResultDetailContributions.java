package ch.elexis.core.ui.tasks;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.TaskState;

public interface ITaskResultDetailContributions {
	
	/**
	 * @param parent
	 * @param task
	 *            in either {@link TaskState#COMPLETED} or {@link TaskState#COMPLETED_WARN} other
	 *            states are covered by a generic composite
	 * @param e4Services
	 */
	public void createDetailCompositeForTask(Composite parent, ITask task,
		Map<String, Object> e4Services);
}
