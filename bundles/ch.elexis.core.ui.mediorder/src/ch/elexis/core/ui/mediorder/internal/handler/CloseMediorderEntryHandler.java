package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.ui.mediorder.MediorderCanExecuteUtil;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
import jakarta.inject.Inject;

public class CloseMediorderEntryHandler {

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	ICoverageService coverageService;

	@Inject
	IOrderService orderService;

	@CanExecute
	public boolean canExecute(@Optional MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		return MediorderCanExecuteUtil.canExecute(mediOrderPart.getSelectedStockEntries(), coverageService);
	}

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> entries = mediOrderPart.getSelectedStockEntries();
		MediorderPartUtil.logPickedUp(orderService, entries);
		for (IStockEntry entry : entries) {
			coreModelService.remove(entry);
		}
		mediOrderPart.refresh();
	}
}
