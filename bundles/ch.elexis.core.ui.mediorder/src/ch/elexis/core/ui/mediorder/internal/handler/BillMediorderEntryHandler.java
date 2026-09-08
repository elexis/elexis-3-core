
package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.mediorder.AbstractBillAndCloseMediorderHandler;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.e4.dialog.StatusDialog;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
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

	@Inject
	IOrderService orderService;

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> entries = mediOrderPart.getSelectedStockEntries();
		IStatus status = billAndClose(coreModelService, contextService, stockService, stickerService, coverageService,
				billingService, entries, true);
		if (status.isOK()) {
			MediorderPartUtil.logBilled(orderService, entries);
		}
		StatusDialog.show(status, false);
		mediOrderPart.refresh();
	}

}