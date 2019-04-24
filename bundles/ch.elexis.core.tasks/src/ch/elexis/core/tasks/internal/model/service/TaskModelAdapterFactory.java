package ch.elexis.core.tasks.internal.model.service;

import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.tasks.internal.model.impl.Task;
import ch.elexis.core.tasks.internal.model.impl.TaskDescriptor;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class TaskModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private static TaskModelAdapterFactory instance;
	
	public static synchronized TaskModelAdapterFactory getInstance(){
		if (instance == null) {
			instance = new TaskModelAdapterFactory();
		}
		return instance;
	}
	
	private TaskModelAdapterFactory(){
		super();
	}
	
	@Override
	protected void initializeMappings(){
		addMapping(new MappingEntry(ITaskDescriptor.class, TaskDescriptor.class,
			ch.elexis.core.jpa.entities.TaskDescriptor.class));
		addMapping(
			new MappingEntry(ITask.class, Task.class, ch.elexis.core.jpa.entities.Task.class));
	}
	
}
