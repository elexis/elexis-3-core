package ch.elexis.core.ui.tasks;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.tasks.model.ITask;

public interface ITaskResultDetailContributions {
	
	public void createDetailCompositeForTask(Composite parent, ITask task);
}
