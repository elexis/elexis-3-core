package ch.elexis.core.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.StatusUtil;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

@Component
public class BillingService implements IBillingService {

	private static Logger logger = LoggerFactory.getLogger(BillingService.class);

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IAccessControlService accessControlService;

	@Reference
	private IStockService stockService;

	@Reference
	private IContextService contextService;

	private List<IBilledAdjuster> billedAdjusters = new ArrayList<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setBilledAdjuster(IBilledAdjuster adjuster) {
		if (!billedAdjusters.contains(adjuster)) {
			billedAdjusters.add(adjuster);
		}
	}

	public void unsetBilledAdjuster(IBilledAdjuster adjuster) {
		if (billedAdjusters.contains(adjuster)) {
			billedAdjusters.remove(adjuster);
		}
	}

	private List<IBillableAdjuster> billableAdjusters = new ArrayList<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setBillableAdjuster(IBillableAdjuster adjuster) {
		if (!billableAdjusters.contains(adjuster)) {
			billableAdjusters.add(adjuster);
		}
	}

	public void unsetBillableAdjuster(IBillableAdjuster adjuster) {
		if (billableAdjusters.contains(adjuster)) {
			billableAdjusters.remove(adjuster);
		}
	}

	@Override
	public Result<IEncounter> isEditable(IEncounter encounter) {
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			if (!coverage.isOpen()) {
				return new Result<>(SEVERITY.WARNING, 0, "Diese Konsultation gehört zu einem abgeschlossenen Fall",
						encounter, false);
			}
		}

		IMandator encounterMandator = encounter.getMandator();
		boolean checkMandant = !accessControlService.evaluate(EvACE.of("LSTG_CHARGE_FOR_ALL"));
		boolean mandatorOk = true;
		boolean invoiceOk = true;
		IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		boolean mandatorLoggedIn = (activeMandator != null);

		// if m is null, ignore checks (return true)
		if (encounterMandator != null && activeMandator != null) {
			if (checkMandant && !(encounterMandator.getId().equals(activeMandator.getId()))) {
				mandatorOk = false;
			}

			IInvoice rn = encounter.getInvoice();
			if (rn == null) {
				invoiceOk = true;
			} else {
				InvoiceState state = rn.getState();
				if (state == InvoiceState.CANCELLED) {
					invoiceOk = true;
				} else {
					invoiceOk = false;
				}
			}
		}

		boolean ok = invoiceOk && mandatorOk && mandatorLoggedIn;
		if (ok) {
			return new Result<>(encounter);
		} else {
			String msg = StringUtils.EMPTY;
			if (!mandatorLoggedIn) {
				msg = "Es ist kein Mandant eingeloggt";
			} else {
				if (!invoiceOk) {
					msg = "Für diese Behandlung wurde bereits eine Rechnung erstellt.";
				} else {
					msg = "Diese Behandlung ist nicht von Ihnen";
				}
			}
			return new Result<>(SEVERITY.WARNING, 0, msg, encounter, false);
		}
	}

	@Override
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount) {
		Result<IEncounter> editable = isEditable(encounter);
		if (!editable.isOK()) {
			return translateResult(editable);
		}
		IBillable beforeAdjust = billable;
		CoreModelServiceHolder.get().refresh(encounter, true);
		logger.info("Billing [" + amount + "] of [" + billable + "] on [" + encounter + "]");
		for (IBillableAdjuster iBillableAdjuster : billableAdjusters) {
			billable = iBillableAdjuster.adjust(billable, encounter);
		}
		if (billable != null) {
			Result<IBillable> verificationResult = billable.getVerifier().verifyAdd(billable, encounter, amount);
			if (verificationResult.isOK()) {
				IBillableOptifier optifier = billable.getOptifier();
				Result<IBilled> optifierResult = optifier.add(billable, encounter, amount);

				if (billable instanceof IArticle) {
					IStatus status = stockService.performSingleDisposal((IArticle) billable, doubleToInt(amount),
							contextService.getActiveMandator().map(m -> m.getId()).orElse(null),
							Optional.ofNullable(encounter.getPatient()).orElse(null), encounter.getPatient());
					if (!status.isOK()) {
						StatusUtil.logStatus(logger, status, true);
						for (IStatus child : status.getChildren()) {
							if (child.getSeverity() == IStatus.WARNING) {
								optifierResult.add(SEVERITY.WARNING, 0, Messages.Mediorder_reservation, null, false);
							}
						}
					}
				}

				// TODO refactor
				if (!optifierResult.isOK() && optifierResult.getCode() == 11) {
					String initialResult = optifierResult.toString();
					// code 11 is tarmed exclusion due to side see TarmedOptifier#EXKLUSIONSIDE
					// set a context variable to specify the side see TarmedLeistung#SIDE,
					// TarmedLeistung#SIDE_L, TarmedLeistung#SIDE_R
					optifier.putContext("Seite", "r");
					optifierResult = optifier.add(billable, encounter, amount);
					if (!optifierResult.isOK() && optifierResult.getCode() == 11) {
						optifier.putContext("Seite", "l");
						optifierResult = optifier.add(billable, encounter, amount);
					}
					if (optifierResult.isOK()) {
						String message = "Achtung: " + initialResult + "\n Es wurde bei der Position "
								+ billable.getCode() + " automatisch die Seite gewechselt."
								+ " Bitte korrigieren Sie die Leistung falls dies nicht korrekt ist.";
						optifierResult.addMessage(SEVERITY.OK, message);
					}
					optifier.clearContext();
				}

				if (optifierResult.get() != null) {
					for (IBilledAdjuster iBilledAdjuster : billedAdjusters) {
						iBilledAdjuster.adjust(optifierResult.get());
					}
					if (optifierResult.isOK()) {
						CodeElementServiceHolder.updateStatistics(billable,
								ContextServiceHolder.get().getActiveUserContact().orElse(null));
						CodeElementServiceHolder.updateStatistics(billable, encounter.getPatient());
					}
				}

				return optifierResult.getSeverity().equals(SEVERITY.OK) ? optifierResult
						: translateWarningOnly(optifierResult);
			} else {
				return translateResult(verificationResult);
			}
		} else {
			return new Result<>(Result.SEVERITY.WARNING, 1,
					"Folgende Leistung '" + beforeAdjust.getCode()
							+ "' konnte im aktuellen Kontext (Fall, Konsultation, Gesetz) nicht verrechnet werden.",
					null, false);
		}
	}

	/**
	 * Get double as int rounded half up.
	 *
	 * @param value
	 * @return
	 */
	private int doubleToInt(double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(0, RoundingMode.HALF_UP);
		return bd.intValue();
	}

	@Override
	public Result<?> removeBilled(IBilled billed, IEncounter encounter) {
		Result<IEncounter> editable = isEditable(encounter);
		if (!editable.isOK()) {
			return editable;
		}

		IBillable billable = billed.getBillable();
		if (billable != null && billable.getOptifier() != null) {
			billable.getOptifier().remove(billed, encounter);
		} else {
			encounter.removeBilled(billed);
		}

		if (billable instanceof IArticle) {

			// TODO stock return via event
			IArticle article = (IArticle) billable;
			String mandatorId = contextService.getActiveMandator().map(m -> m.getId()).orElse(null);
			stockService.performSingleReturn(article, (int) billed.getAmount(), mandatorId);

			// TODO prescription via event
			Object prescId = billed.getExtInfo(Constants.FLD_EXT_PRESC_ID);
			if (prescId instanceof String) {
				IPrescription prescription = coreModelService.load((String) prescId, IPrescription.class).orElse(null);
				if (prescription != null && EntryType.SELF_DISPENSED == prescription.getEntryType()) {
					coreModelService.remove(prescription);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, prescription);
				}
			}
		}

		return Result.OK();
	}

	private Result<IBilled> translateResult(Result<?> vfcResult) {
		Result<IBilled> ret = new Result<>(vfcResult.getSeverity(), null);
		vfcResult.getMessages().forEach(msg -> ret.addMessage(msg.getSeverity(), msg.getText()));
		return ret;
	}

	private Result<IBilled> translateWarningOnly(Result<?> result) {
		Result<IBilled> ret = new Result<>(SEVERITY.WARNING, null);
		result.getMessages().stream().filter(msg -> SEVERITY.WARNING.equals(msg.getSeverity()))
				.forEach(msg -> ret.addMessage(msg.getSeverity(), msg.getText()));
		return ret;
	}

	@Override
	public Optional<IBillingSystemFactor> getBillingSystemFactor(String system, LocalDate date) {
		IQuery<IBillingSystemFactor> query = coreModelService.getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS, system);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_FROM, COMPARATOR.LESS_OR_EQUAL, date);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_TO, COMPARATOR.GREATER_OR_EQUAL, date);
		return query.executeSingleResult();
	}

	@Override
	public void setBillingSystemFactor(LocalDate from, LocalDate to, double factor, String system) {
		if (to == null) {
			// 20380118, TimeTool.END_OF_UNIX_EPOCH
			to = LocalDate.of(2038, 1, 18);
		}

		IQuery<IBillingSystemFactor> query = coreModelService.getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS, system);
		List<IBillingSystemFactor> existingWithSystem = query.execute();
		for (IBillingSystemFactor iBillingSystemFactor : existingWithSystem) {
			if (iBillingSystemFactor.getValidTo() == null || iBillingSystemFactor.getValidTo().isAfter(from)) {
				iBillingSystemFactor.setValidTo(from);
				coreModelService.save(iBillingSystemFactor);
			}
		}
		IBillingSystemFactor billingSystemFactor = coreModelService.create(IBillingSystemFactor.class);
		billingSystemFactor.setFactor(factor);
		billingSystemFactor.setSystem(system);
		billingSystemFactor.setValidFrom(from);
		billingSystemFactor.setValidTo(to);
		coreModelService.save(billingSystemFactor);
	}

	@Override
	public IStatus changeAmountValidated(IBilled billed, double newAmount) {
		double oldAmount = billed.getAmount();
		if (newAmount == oldAmount) {
			return Status.OK_STATUS;
		}

		IEncounter encounter = billed.getEncounter();
		if (newAmount == 0) {
			removeBilled(billed, encounter);
			return Status.OK_STATUS;
		}

		MultiStatus ret = new MultiStatus(getClass(), 0, null);
		boolean bAllowOverrideStrict = ConfigServiceHolder.get()
				.getActiveUserContact(Preferences.LEISTUNGSCODES_ALLOWOVERRIDE_STRICT, false);

		double difference = newAmount - oldAmount;
		if (difference > 0) {
			IBillable billable = billed.getBillable();
			double fractions = difference % 1;
			int differenceInt = (int) difference;
			for (int i = 0; i < differenceInt; i++) {
				Result<IBilled> result = bill(billable, encounter, 1.0);
				if (bAllowOverrideStrict) {
					if (ret.isOK() && !result.isOK()) {
						String message = result.getMessages().stream().map(m -> m.getText())
								.collect(Collectors.joining(", "));
						ret.add(new Status(Status.WARNING, "ch.elexis.core.services", message));
					}
				} else if (!result.isOK()) {
					String message = result.getMessages().stream().map(m -> m.getText())
							.collect(Collectors.joining(", "));
					ret.add(new Status(Status.ERROR, "ch.elexis.core.services", message));
				}
			}
			if (fractions > 0.0) {
				Result<IBilled> result = bill(billable, encounter, fractions);
				if (bAllowOverrideStrict) {
					if (ret.isOK() && !result.isOK()) {
						String message = result.getMessages().stream().map(m -> m.getText())
								.collect(Collectors.joining(", "));
						ret.add(new Status(Status.WARNING, "ch.elexis.core.services", message));
					}
				} else if (!result.isOK()) {
					String message = result.getMessages().stream().map(m -> m.getText())
							.collect(Collectors.joining(", "));
					ret.add(new Status(Status.ERROR, "ch.elexis.core.services", message));
				}
			}
		} else if (difference < 0) {
			changeAmount(billed, newAmount);
		}

		if (ret.getChildren().length > 0) {
			List<String> messages = Arrays.stream(ret.getChildren()).map(IStatus::getMessage).toList();
			String message = messages.stream().distinct().collect(Collectors.joining(", "));
			return new Status(Status.ERROR, "ch.elexis.core.services", message);
		} else {
			return ret;
		}
	}

	@Override
	public void changeAmount(IBilled billed, double newAmount) {
		double oldAmount = billed.getAmount();
		billed.setAmount(newAmount);
		IBillable billable = billed.getBillable();
		if (billable instanceof IArticle) {
			IArticle art = (IArticle) billable;
			String mandatorId = contextService.getActiveMandator().map(m -> m.getId()).orElse(null);
			double difference = newAmount - oldAmount;
			if (difference > 0) {
				stockService.performSingleDisposal(art, (int) difference, mandatorId, null);
			} else if (difference < 0) {
				difference *= -1;
				stockService.performSingleReturn(art, (int) difference, mandatorId);
			}
		}
	}
}
