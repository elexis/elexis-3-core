package ch.elexis.core.ui.mediorder;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public abstract class AbstractBillAndCloseMediorderHandler {

	protected IStatus billAndClose(IModelService coreModelService, IContextService contextService,
			ICoverageService coverageService, IBillingService billingService, List<IStockEntry> stockEntries,
			boolean removeStockEntry) {

		if (stockEntries.isEmpty()) {
			return Status.OK_STATUS;
		}

		IStock stock = stockEntries.get(0).getStock();
		IPerson person = stock.getOwner();
		if (!person.isPatient()) {
			return Status.error("Not a patient stock");
		}

		IPatient patient = person.asIPatient();
		ICoverage coverage = coverageService.getLatestOpenCoverage(patient)
				.orElse(coverageService.createDefaultCoverage(patient));

		IEncounter billingEncounter = new IEncounterBuilder(coreModelService, coverage,
				contextService.getActiveMandator().get()).build();
		VersionedResource vr = VersionedResource.load(null);
		vr.update("Verrechnung Patientenbestellung", contextService.getActiveUser().get().getId());
		billingEncounter.setVersionedEntry(vr);
		coreModelService.save(billingEncounter);

		for (IStockEntry stockEntry : stockEntries) {
			IArticle article = stockEntry.getArticle();
			Result<IBilled> result = billingService.bill(article, billingEncounter, stockEntry.getCurrentStock());

			if (!result.isOK()) {
				return Status.error(result.getCombinedMessages());
			}

			int currentStock = stockEntry.getCurrentStock();
			int maximumStock = stockEntry.getMaximumStock();
			int minimumStock = stockEntry.getMinimumStock();
			boolean isBalanced = currentStock == maximumStock && maximumStock == minimumStock;

			if (isBalanced) {
				if (removeStockEntry) {
					coreModelService.remove(stockEntry);
				} else {
					stockEntry.setMinimumStock(0);
					stockEntry.setCurrentStock(0);
					stockEntry.setMaximumStock(0);
					coreModelService.save(stockEntry);
				}
			}
		}

		if (stock.getStockEntries().isEmpty() && removeStockEntry) {
			coreModelService.remove(stock);
	}

		return Status.OK_STATUS;
	}

}
