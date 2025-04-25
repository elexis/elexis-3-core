package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.ui.mediorder.MediorderPart;
import jakarta.inject.Inject;

public class ShowPatientOrderHandler {

	@Inject
	IOrderService orderService;

	@Inject
	EPartService partService;

	@Inject
	IContextService contextService;

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> selectedStockEntries = mediOrderPart.getSelectedStockEntries();
		for (IStockEntry stockEntry : selectedStockEntries) {
			IOrderEntry orderEntry = orderService.findOpenOrderEntryForStockEntry(stockEntry);
			if (orderEntry != null) {
				IOrder order = orderEntry.getOrder();
				if (order != null) {
					MPart orderPart = partService.findPart("ch.elexis.OrderManagementView");
					if (orderPart == null) {
						orderPart = partService.createPart("ch.elexis.OrderManagementView");
					}
					partService.showPart(orderPart, PartState.VISIBLE);
					contextService.setTyped(order);
					break;
				}
			}
		}

	}

}
