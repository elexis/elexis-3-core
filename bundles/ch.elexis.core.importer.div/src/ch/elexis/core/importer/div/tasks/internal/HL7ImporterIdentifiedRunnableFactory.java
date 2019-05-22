package ch.elexis.core.importer.div.tasks.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.importer.div.tasks.BillLabResultOnCreationIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IVirtualFilesystemService;

@Component
public class HL7ImporterIdentifiedRunnableFactory implements IIdentifiedRunnableFactory {
	
	private IModelService coreModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private void setModelService(IModelService modelService){
		coreModelService = modelService;
	}
	
	@Reference
	private ILabImportUtil labimportUtil;
	
	@Reference
	private IVirtualFilesystemService vfsService;

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables(){
		List<IIdentifiedRunnable> ret = new ArrayList<>();
		ret.add(new HL7ImporterIIdentifiedRunnable(coreModelService, labimportUtil, vfsService));
		ret.add(new BillLabResultOnCreationIdentifiedRunnable(coreModelService, null));
		return ret;
	}
	
}
