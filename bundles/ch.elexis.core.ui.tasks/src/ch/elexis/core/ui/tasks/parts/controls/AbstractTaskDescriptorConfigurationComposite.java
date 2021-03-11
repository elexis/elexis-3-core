package ch.elexis.core.ui.tasks.parts.controls;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;

public abstract class AbstractTaskDescriptorConfigurationComposite extends Composite {
	
	protected ITaskDescriptor taskDescriptor;
	
	public AbstractTaskDescriptorConfigurationComposite(Composite parent, int style){
		super(parent, style);
	}
	
	protected boolean saveTaskDescriptor(){
		// TODO update resp entry in list
		if (taskDescriptor != null) {
			return TaskModelServiceHolder.get().save(taskDescriptor);
		}
		return false;
	}
	
	public void setSelection(ITaskDescriptor taskDescriptor){
		this.taskDescriptor = taskDescriptor;
	}
	
}
