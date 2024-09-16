package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;

public class RemoveMediorderEntryHandler {

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Execute
	public void execute(MPart part, ESelectionService selectionService) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> selectedStockEntries = mediOrderPart.getSelectedStockEntries();
		selectedStockEntries.forEach(entry -> MediorderPartUtil.removeStockEntry(entry, coreModelService));
		mediOrderPart.refresh();
	}

}
