package ch.elexis.core.ui.tasks.parts;

import java.util.Comparator;

import ch.elexis.core.tasks.model.ITask;

public class ITaskComparators {
	
	public static Comparator<ITask> ofLastUpdate(){
		return Comparator.comparingLong(o -> ((ITask) o).getLastupdate());
	}
	
	public static Comparator<ITask> ofTaskDescriptorId(){
		return Comparator.comparing((task) -> task.getTaskDescriptor().getId(),
			String.CASE_INSENSITIVE_ORDER);
	}
}
