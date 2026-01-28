package ch.elexis.core.mediorder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public abstract class AbstractBillAndCloseMediorderHandler {

	private IModelService coreModelService;
	private IContextService contextService;
	private ICoverageService coverageService;
	private IBillingService billingService;
	private IStockService stockService;
	private IStickerService stickerService;

	private boolean removeStockEntry;

	protected IStatus billAndClose(IModelService coreModelService, IContextService contextService,
			IStockService stockService, IStickerService stickerService,
			ICoverageService coverageService, IBillingService billingService, List<IStockEntry> stockEntries,
			boolean removeStockEntry) {

		this.coreModelService = coreModelService;
		this.contextService = contextService;
		this.coverageService = coverageService;
		this.billingService = billingService;
		this.removeStockEntry = removeStockEntry;
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

		IPatient patient = person.asIPatient();

		Map<Boolean, List<IStockEntry>> mapEntries = stockEntries.stream()
				.collect(Collectors.partitioningBy(entry -> entry.getArticle().isObligation()));
		for (Map.Entry<Boolean, List<IStockEntry>> entry : mapEntries.entrySet()) {
			IStatus status = processArticles(entry.getValue(), patient, entry.getKey());
			if (!status.isOK()) {
				return status;
			}
		}

		if (stock.getStockEntries().isEmpty() && removeStockEntry) {
			coreModelService.remove(stock);
		}

		return Status.OK_STATUS;
	}


	/**
	 * Charges a list of items for a specified patient, taking into account whether
	 * each article is mandatory. This method checks if the patient has coverage
	 * under the appropriate law. If such coverage does not exist, it creates a new
	 * one for the patient.
	 * 
	 * @param articles
	 * @param patient
	 * @param isObligatory indicates whether the items are mandatory or not
	 * @return
	 */
	private IStatus processArticles(List<IStockEntry> articles, IPatient patient, boolean isObligatory) {
		if (articles.isEmpty()) {
			return Status.OK_STATUS;
		}

		Optional<ICoverage> coverage = coverageService.getLatestOpenCoverage(patient);
		if (isObligatory) {
			if (coverage.isEmpty() || !coverage.get().getBillingSystem().getLaw().equals(BillingLaw.KVG)) {
				coverage = getOrCreateCoverage(patient, BillingLaw.KVG);
			}
		} else {
			if (coverage.isEmpty() || !(coverage.get().getBillingSystem().getLaw().equals(BillingLaw.ORG)
					|| coverage.get().getBillingSystem().getLaw().equals(BillingLaw.privat))) {
				coverage = getOrCreateCoverage(patient, BillingLaw.privat);
			}
		}

		Optional<IEncounter> encounter = coverageService.getLatestEncounter(coverage.get());
		if (encounter.isEmpty()) {
			encounter = Optional.of(
					new IEncounterBuilder(coreModelService, coverage.get(), contextService.getActiveMandator().get())
							.buildAndSave());
		}
		setBillingText(encounter.get());
		return billAndAdjustArticleStock(encounter.get(), articles, removeStockEntry);
	}

	private Optional<ICoverage> getOrCreateCoverage(IPatient patient, BillingLaw law) {
		Optional<ICoverage> coverage = coverageService.getCoverageWithLaw(patient, law);
		if (coverage.isEmpty()) {
			coverage = Optional
					.of(new ICoverageBuilder(coreModelService, patient, coverageService.getDefaultCoverageLabel(),
							coverageService.getDefaultCoverageReason(), law.toString()).buildAndSave());
		}
		return coverage;
	}

	private void setBillingText(IEncounter encounter) {
		VersionedResource vr = VersionedResource.load(null);
		vr.update(Messages.Mediorder_Billing_Text, contextService.getActiveUser().get().getId());
		encounter.setVersionedEntry(vr);
		coreModelService.save(encounter);
	}

	private IStatus billAndAdjustArticleStock(IEncounter encounter, List<IStockEntry> entries,
			boolean removeStockEntry) {
		for (IStockEntry stockEntry : entries) {
			IArticle article = stockEntry.getArticle();

			Result<IBilled> result = billingService.bill(article, encounter, stockEntry.getCurrentStock());
			if (!result.isOK()) {
				return Status.error(result.getCombinedMessages());
			}
			removeMailSticker(encounter.getPatient());
		}
		return Status.OK_STATUS;
	}

	protected void removeMailSticker(IPatient patient) {
		if (stickerService.hasSticker(patient,
				coreModelService.load(Constants.MEDIORDER_MAIL_STICKER_ID, ISticker.class).get())) {
			Optional<IStock> stock = stockService.getPatientStock(patient);
			if (stock.isEmpty()
					|| MediorderUtil.calculateStockState(stockService.getPatientStock(patient).get()) != 1) {
				stickerService.removeSticker(
						coreModelService.load(Constants.MEDIORDER_MAIL_STICKER_ID, ISticker.class).get(), patient);
			}
		}
	}
}