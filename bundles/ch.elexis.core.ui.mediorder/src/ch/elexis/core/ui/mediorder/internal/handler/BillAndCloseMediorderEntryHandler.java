
package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.e4.dialog.StatusDialog;
import ch.elexis.core.ui.mediorder.AbstractBillAndCloseMediorderHandler;
import ch.elexis.core.ui.mediorder.MediorderPart;

public class BillAndCloseMediorderEntryHandler extends AbstractBillAndCloseMediorderHandler {

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	IContextService contextService;

	@Inject
	ICoverageService coverageService;

	@Inject
	IStickerService stickerService;

	@Inject
	IStockService stockService;

	@Execute
	public void execute(MPart part, ESelectionService selectionService, IEncounterService encounterService,
			IBillingService billingService) {

		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> selectedStockEntries = mediOrderPart.getSelectedStockEntries();
		IStatus status = billAndClose(coreModelService, contextService, stockService, stickerService, coverageService,
				billingService,
				selectedStockEntries, true);
		StatusDialog.show(status, false);

		mediOrderPart.refresh();
	}

}