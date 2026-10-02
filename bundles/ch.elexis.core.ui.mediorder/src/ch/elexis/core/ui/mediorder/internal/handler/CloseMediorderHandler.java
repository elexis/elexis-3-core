package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;

import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.mediorder.MediorderCanExecuteUtil;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class CloseMediorderHandler {

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	IOrderService orderService;

	@Inject
	IStickerService stickerService;

	@Inject
	IStockService stockService;

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IStock stock) {
		return stock != null && MediorderCanExecuteUtil.canExecute(stock.getStockEntries());
	}

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		for (IStock stock : mediOrderPart.getSelectedStocks()) {
			List<IStockEntry> entries = new ArrayList<>(stock.getStockEntries());
			MediorderPartUtil.dispense(entries, coreModelService, orderService, stickerService, stockService);
		}
		mediOrderPart.refresh();
	}

}
