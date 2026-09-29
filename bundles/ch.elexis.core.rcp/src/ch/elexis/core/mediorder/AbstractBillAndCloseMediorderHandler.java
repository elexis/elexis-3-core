package ch.elexis.core.mediorder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;

/**
 * Bills the current stock of patient stock entries, see
 * {@link MediorderBilling}.
 */
public abstract class AbstractBillAndCloseMediorderHandler {

	private IModelService coreModelService;
	private IStockService stockService;
	private IStickerService stickerService;

	protected IStatus billAndClose(IModelService coreModelService, IContextService contextService,
			IStockService stockService, IStickerService stickerService,
			ICoverageService coverageService, IBillingService billingService, List<IStockEntry> stockEntries,
			boolean removeStockEntry) {

		this.coreModelService = coreModelService;
		this.stockService = stockService;
		this.stickerService = stickerService;

		if (stockEntries.isEmpty()) {
			return Status.OK_STATUS;
		}

		IStock stock = stockEntries.get(0).getStock();
		IPerson person = stock.getOwner();
		if (!person.isPatient()) {
			return Status.error(Messages.Mediorder_inactive_patient_stock);
		}

		Map<IArticle, Integer> amounts = new LinkedHashMap<>();
		for (IStockEntry stockEntry : stockEntries) {
			amounts.merge(stockEntry.getArticle(), stockEntry.getCurrentStock(), Integer::sum);
		}
		IStatus status = new MediorderBilling(coreModelService, contextService, stockService, stickerService,
				coverageService, billingService).bill(person.asIPatient(), amounts);
		if (!status.isOK()) {
			return status;
		}

		if (stock.getStockEntries().isEmpty() && removeStockEntry) {
			coreModelService.remove(stock);
		}

		return Status.OK_STATUS;
	}

	protected void removeMailSticker(IPatient patient) {
		MediorderUtil.removeMailSticker(patient, coreModelService, stickerService, stockService);
	}
}
