
package ch.elexis.core.ui.mediorder.internal.handler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.mediorder.AbstractBillAndCloseMediorderHandler;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.e4.dialog.StatusDialog;
import ch.elexis.core.ui.mediorder.MediorderPart;
import jakarta.inject.Inject;

public class BillMediorderEntryHandler extends AbstractBillAndCloseMediorderHandler {

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

	@Inject
	IBillingService billingService;

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		IStatus status = billAndClose(coreModelService, contextService, stockService, stickerService, coverageService,
				billingService,
				mediOrderPart.getSelectedStockEntries(), true);
		StatusDialog.show(status, false);
		mediOrderPart.refresh();
	}

}