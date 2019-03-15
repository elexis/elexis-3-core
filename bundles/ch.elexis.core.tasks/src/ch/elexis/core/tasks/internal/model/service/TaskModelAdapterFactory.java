package ch.elexis.core.tasks.internal.model.service;

import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.tasks.internal.model.impl.TaskDescriptor;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class TaskModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private static TaskModelAdapterFactory INSTANCE;
	
	public static synchronized TaskModelAdapterFactory getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new TaskModelAdapterFactory();
		}
		return INSTANCE;
	}
	
	private TaskModelAdapterFactory(){
		super();
	}
	
	@Override
	protected void initializeMappings(){
		addMapping(new MappingEntry(ITaskDescriptor.class, TaskDescriptor.class,
			ch.elexis.core.jpa.entities.TaskDescriptor.class));	
	}
	
}
