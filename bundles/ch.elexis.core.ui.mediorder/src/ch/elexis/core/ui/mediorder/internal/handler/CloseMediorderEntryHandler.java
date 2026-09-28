package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.mediorder.MediorderCanExecuteUtil;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
import jakarta.inject.Inject;

public class CloseMediorderEntryHandler {

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
	public boolean canExecute(@Optional MPart part) {
		if (part == null || !(part.getObject() instanceof MediorderPart mediOrderPart)) {
			return false;
		}
		return MediorderCanExecuteUtil.canExecute(mediOrderPart.getSelectedStockEntries());
	}

	@Execute
	public void execute(MPart part) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> entries = mediOrderPart.getSelectedStockEntries();
		IPatient patient = entries.isEmpty() ? null : MediorderPartUtil.getPatient(entries.get(0)).orElse(null);
		MediorderPartUtil.logPickedUp(orderService, entries);
		for (IStockEntry entry : entries) {
			coreModelService.remove(entry);
		}
		if (patient != null) {
			MediorderUtil.removeMailSticker(patient, coreModelService, stickerService, stockService);
		}
		mediOrderPart.refresh();
	}
}
