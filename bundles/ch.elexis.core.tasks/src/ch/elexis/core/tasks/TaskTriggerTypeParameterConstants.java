package ch.elexis.core.tasks;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class TaskTriggerTypeParameterConstants {
	
	public static final String FILESYSTEM_CHANGE_PARAM_DIRECTORY_PATH = "directoryPath";
	
	/**
	 * The path of the file that created the {@link TaskTriggerType#FILESYSTEM_CHANGE} event,
	 * will be passed to the {@link ITask}
	 */
	public static final String FILESYSTEM_CHANGE_RUNPARAM_EVENTFILE_PATH = "eventFilePath";
}
