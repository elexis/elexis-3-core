package ch.elexis.core.ui.mediorder.internal.handler;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
import jakarta.inject.Inject;

/**
 * Takes the missing amount of the selected entries from the mandator or default
 * stock and bills it. Only executable if the stock contains the whole missing
 * amount of every selected entry.
 */
public class TakeFromStockHandler {

	@Inject
	IStockService stockService;

	@Inject
	IContextService contextService;

	@Inject
	IOrderService orderService;

	@CanExecute
	public boolean canExecute(@Optional MPart part) {
		if (part == null || !(part.getObject() instanceof MediorderPart mediOrderPart)) {
			return false;
		}
		return MediorderPartUtil.canTakeFromStock(mediOrderPart.getSelectedStockEntries(), stockService,
				contextService, orderService);
	}

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		mediOrderPart.takeFromStock(mediOrderPart.getSelectedStockEntries());
	}
}
