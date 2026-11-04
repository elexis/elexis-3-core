package ch.elexis.core.mediorder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public class MediorderBilling {

	private final IModelService coreModelService;
	private final IContextService contextService;
	private final IStockService stockService;
	private final IStickerService stickerService;
	private final ICoverageService coverageService;
	private final IBillingService billingService;

	public MediorderBilling(IModelService coreModelService, IContextService contextService, IStockService stockService,
			IStickerService stickerService, ICoverageService coverageService, IBillingService billingService) {
		this.coreModelService = coreModelService;
		this.contextService = contextService;
		this.stockService = stockService;
		this.stickerService = stickerService;
		this.coverageService = coverageService;
		this.billingService = billingService;
	}

	/**
	 * 
	 * Call before sending the order, as suppliers may set the state of the entries
	 * themselves. Pass the result to {@link #billOrderedEntries} after sending.
	 * 
	 * @param orderEntries
	 * @return the open entries of the stocks
	 */
	public static List<IOrderEntry> getOpenPatientOrderEntries(List<IOrderEntry> orderEntries) {
		return orderEntries.stream().filter(entry -> OrderEntryState.OPEN == entry.getState())
				.filter(entry -> getPatient(entry.getStock()).isPresent()).collect(Collectors.toList());
	}

	public IStatus billOrderedEntries(List<IOrderEntry> orderEntries, IOrderService orderService) {
		Map<IPatient, Map<IArticle, Integer>> amountsByPatient = new LinkedHashMap<>();
		for (IOrderEntry entry : orderEntries) {
			if (entry.isDeleted() || OrderEntryState.OPEN == entry.getState() || entry.getArticle() == null
					|| entry.getAmount() <= 0) {
				continue;
			}
			getPatient(entry.getStock())
					.ifPresent(patient -> amountsByPatient.computeIfAbsent(patient, p -> new LinkedHashMap<>())
							.merge(entry.getArticle(), entry.getAmount(), Integer::sum));
		}
		return bill(amountsByPatient, orderService);
	}

	public IStatus bill(Map<IPatient, Map<IArticle, Integer>> amountsByPatient, IOrderService orderService) {
		MultiStatus multiStatus = new MultiStatus(getClass(), IStatus.OK, Messages.Mediorder_Billing_Failed);
		amountsByPatient.forEach((patient, amounts) -> {
			IStatus status = bill(patient, amounts);
			if (status.isOK()) {
				orderService.getHistoryService().logMediorderBilled(patient,
						amounts.keySet().stream().map(IArticle::getLabel).collect(Collectors.toList()));
			} else {
				multiStatus.add(
						new Status(status.getSeverity(), getClass(), patient.getLabel() + ": " + status.getMessage())); //$NON-NLS-1$
			}
		});
		return multiStatus.isOK() ? Status.OK_STATUS : multiStatus;
	}

	public IStatus bill(IPatient patient, Map<IArticle, Integer> amounts) {
		Map<Boolean, Map<IArticle, Integer>> amountsByObligation = amounts.entrySet().stream()
				.collect(Collectors.partitioningBy(entry -> entry.getKey().isObligation(),
						Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)));
		for (Map.Entry<Boolean, Map<IArticle, Integer>> entry : amountsByObligation.entrySet()) {
			IStatus status = processArticles(entry.getValue(), patient, entry.getKey());
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	private IStatus processArticles(Map<IArticle, Integer> amounts, IPatient patient, boolean isObligatory) {
		if (amounts.isEmpty()) {
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

		Optional<IEncounter> encounter = coverageService.getLatestEncounter(coverage.get())
				.filter(e -> billingService.isEditable(e).isOK());
		if (encounter.isEmpty()) {
			encounter = Optional.of(
					new IEncounterBuilder(coreModelService, coverage.get(), contextService.getActiveMandator().get())
							.buildAndSave());
		}
		setBillingText(encounter.get(), patient);
		for (Map.Entry<IArticle, Integer> amount : amounts.entrySet()) {
			Result<IBilled> result = billingService.bill(amount.getKey(), encounter.get(), amount.getValue(), false);
			if (!result.isOK()) {
				return Status.error(result.getCombinedMessages());
			}
		}
		MediorderUtil.removeMailSticker(patient, coreModelService, stickerService, stockService);
		return Status.OK_STATUS;
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

	private void setBillingText(IEncounter encounter, IPatient patient) {
		String reference = MediorderHistoryRef.encode(patient.getId(), findCurrentOrderBlobId(patient));
		VersionedResource resource = encounter.getVersionedEntry();
		if (resource == null) {
			resource = VersionedResource.load(null);
		}
		Samdas samdas = new Samdas(StringUtils.defaultString(resource.getHead()));
		Samdas.Record record = samdas.getRecord();
		if (hasBillingXRef(record, reference)) {
			return;
		}
		String text = StringUtils.defaultString(record.getText());
		String separator = text.isEmpty() ? StringUtils.EMPTY : StringUtils.LF;
		record.setText(text + separator + Messages.Mediorder_Billing_Text);
		if (reference != null) {
			record.add(new Samdas.XRef(XRefExtensionConstants.providerMediorderID, reference,
					text.length() + separator.length(), Messages.Mediorder_Billing_Text.length()));
		}
		resource.update(samdas.toString(), contextService.getActiveUser().get().getId());
		encounter.setVersionedEntry(resource);
		coreModelService.save(encounter);
		contextService.postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
	}

	/**
	 * Skipped if the encounter already references the order, e.g. when obligatory
	 * and non obligatory articles are billed on the same encounter.
	 * 
	 * @param encounter the encounter the articles are billed on
	 * @param patient   the patient the order belongs to
	 * @return
	 */
	private boolean hasBillingXRef(Samdas.Record record, String reference) {
		return record.getXrefs().stream()
				.anyMatch(xref -> XRefExtensionConstants.providerMediorderID.equals(xref.getProvider())
						&& Objects.equals(reference, xref.getID()));
	}

	private String findCurrentOrderBlobId(IPatient patient) {
		IQuery<IBlob> query = coreModelService.getQuery(IBlob.class);
		query.and("id", COMPARATOR.LIKE, MediorderBlobId.idPrefix(patient.getId()) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		return query.execute().stream().filter(blob -> MediorderBlobId.belongsTo(blob.getId(), patient.getId()))
				.max(Comparator
						.comparing((IBlob blob) -> MediorderBlobId.resolveTimestamp(blob).orElse(LocalDateTime.MIN)))
				.map(IBlob::getId).orElse(null);
	}

	private static Optional<IPatient> getPatient(IStock stock) {
		IPerson owner = stock != null ? stock.getOwner() : null;
		if (owner == null || !owner.isPatient()) {
			return Optional.empty();
		}
		return Optional.ofNullable(owner.asIPatient());
	}
}
