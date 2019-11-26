package ch.elexis.core.ui.tasks;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public interface ITaskResultDetailContributor {
	
	/**
	 * @return the {@link ITaskDescriptor#getReferenceId()} (that is the specific
	 *         {@link IIdentifiedRunnable}) contributing the result dialog to.
	 */
	String getIdentifiedRunnableId();
	
	/**
	 * add a detail dialog for the task that is based on the {@link IIdentifiedRunnable} advertised
	 * in {@link #getIdentifiedRunnableId()}
	 *
	 * @param parent
	 * @param task
	 */
	void createDetailCompositeForTask(Composite parent, ITask task);
	
}
