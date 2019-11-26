package ch.elexis.core.ui.tasks.internal;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.tasks.ITaskResultDetailContributions;
import ch.elexis.core.ui.tasks.ITaskResultDetailContributor;

@Component
public class TaskResultDetailDialogContributions implements ITaskResultDetailContributions {
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	private volatile List<ITaskResultDetailContributor> taskResultDetailDialogContributors;
	
	@Override
	public void createDetailCompositeForTask(Composite parent, ITask task){
		String identifiedRunnableId = task.getTaskDescriptor().getIdentifiedRunnableId();
		
		for (ITaskResultDetailContributor contribution : taskResultDetailDialogContributors) {
			if (identifiedRunnableId.equals(contribution.getIdentifiedRunnableId())) {
				contribution.createDetailCompositeForTask(parent, task);
				return;
			}
		}
		
		new GenericTaskResultDetailComposite(parent, task);
	}
	
}
