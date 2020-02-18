package ch.elexis.core.ui.tasks.internal.detailcontributors;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.ui.tasks.ITaskResultDetailContributor;

@Component
public class HL7ImporterIdentifiedRunnableContributor implements ITaskResultDetailContributor {
	
	@Reference
	IVirtualFilesystemService vfsService;
	
	@Override
	public String getIdentifiedRunnableId(){
		// copied from  ch.elexis.core.importer.div.tasks.internal.HL7ImporterIIdentifiedRunnable#RUNNABLE_ID
		return "hl7importer";
	}
	
	@Override
	public void createDetailCompositeForTask(Composite parent, ITask task, Map<String, Object> e4Services){
		new HL7ImporterTaskResultDetailComposite(parent, task, e4Services, vfsService);
	}
	
}
