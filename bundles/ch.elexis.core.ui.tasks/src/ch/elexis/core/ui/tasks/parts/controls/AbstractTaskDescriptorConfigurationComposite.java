package ch.elexis.core.ui.tasks.parts.controls;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;

public abstract class AbstractTaskDescriptorConfigurationComposite extends Composite {

	protected ITaskDescriptor taskDescriptor;

	public AbstractTaskDescriptorConfigurationComposite(Composite parent, int style) {
		super(parent, style);
	}

	public void refresh() {
	};

	protected boolean saveTaskDescriptor() {
		// TODO update resp entry in list
		if (taskDescriptor != null) {
			TaskModelServiceHolder.get().save(taskDescriptor);
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, taskDescriptor);
			return true;
		}
		return false;
	}

	public void setSelection(ITaskDescriptor taskDescriptor) {
		this.taskDescriptor = taskDescriptor;
	}

}
