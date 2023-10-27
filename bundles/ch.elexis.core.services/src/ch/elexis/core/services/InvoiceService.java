package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.InvoiceConstants;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IAccountTransactionBuilder;
import ch.elexis.core.model.builder.IInvoiceBilledBuilder;
import ch.elexis.core.model.builder.IPaymentBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

@Component
public class InvoiceService implements IInvoiceService {

	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

	@SuppressWarnings("unchecked")
	@Override
	public Result<IInvoice> invoice(List<IEncounter> encounters) {
		Result<IInvoice> result = new Result<>();

		int origSize = encounters.size();
		encounters = encounters.stream().filter(e -> e.isBillable()).collect(Collectors.toList());
		if (encounters.size() < origSize) {
			LoggerFactory.getLogger(getClass())
					.warn("Ignoring [" + (origSize - encounters.size()) + "] not billable encounters.");
		}

		if (encounters == null || encounters.isEmpty()) {
			return result.add(Result.SEVERITY.WARNING, 1, "Die Rechnung enthält keine Behandlungen (Konsultationen)",
					null, true); // js:
		}

		// patients are persons
		for (IEncounter encounter : encounters) {
			IPatient patient = encounter.getCoverage().getPatient();
			if (!patient.isPerson()) {
				logger.warn("Patient [" + patient.getPatientNr()
						+ "] is person was not set. Setting is person automatically.");
				patient.setPerson(true);
				CoreModelServiceHolder.get().save(patient);
			}
		}

		for (IEncounter encounter : encounters) {
			if (encounter.getBilled().isEmpty() || EncounterServiceHolder.get().getSales(encounter).isZero()) {
				LoggerFactory.getLogger(getClass())
						.warn("Ignoring encounter [" + encounter.getLabel() + "] with sales amount zero.");
			} else {
				List<IBilled> encounterBilled = encounter.getBilled();
				for (IBilled billed : encounterBilled) {
					if (billed.getPrice().isZero() && isBillingCheckZero()) {
						IPatient pat = encounter.getCoverage().getPatient();
						String msg = "Die Konsultation vom "
								+ encounter.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
								+ " für\nPatient Nr. " + pat.getPatientNr() + ", " + pat.getLastName() + ", "
								+ pat.getFirstName() + ", "
								+ pat.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
								+ StringUtils.LF + "enthält mindestens eine Leistung zum Preis 0.00.\n"
								+ "\nDie Ärztekasse würde so eine Rechnung zurückgeben.\n\n";
						return result.add(Result.SEVERITY.WARNING, 1, msg + "Diese Rechnung wird jetzt nicht erstellt."
								+ "\n\nBitte prüfen Sie die verrechneten Leistungen,"
								+ "oder verschieben Sie die Konsultation zu einem später zu verrechnenden Fall!", null,
								true);
					}
				}
			}
		}

		IInvoice ret = CoreModelServiceHolder.get().create(IInvoice.class);

		LocalDate startDate = LocalDate.of(2999, 12, 31);
		LocalDate endDate = LocalDate.of(2000, 1, 1);
		LocalDate actDate = LocalDate.now();
		IMandator mandator = null;
		List<IDiagnosisReference> encounterDiagnosis = null;
		ICoverage coverage = null;
		Money sum = new Money();

		for (IEncounter encounter : encounters) {
			IInvoice shouldntExist = encounter.getInvoice();
			if ((shouldntExist != null) && (shouldntExist.getState() != InvoiceState.CANCELLED)) {
				logger.warn("Tried to create bill for already billed kons " + encounter.getLabel());
				continue;
			}
			IMandator encounterMandator = encounter.getMandator();
			if (encounterMandator == null) {
				result = result.add(Result.SEVERITY.ERROR, 1,
						"Ungültiger Mandant bei Konsultation " + encounter.getLabel(), ret, true);
				continue;
			}
			if (mandator == null) {
				mandator = encounterMandator;
				ret.setMandator(mandator);
			} else {
				if (!encounterMandator.getBiller().getId().equals(mandator.getBiller().getId())) {
					result = result.add(Result.SEVERITY.ERROR, 2,
							"Die Liste enthält unterschiedliche Rechnungssteller " + encounter.getLabel(), ret, true);
					continue;
				}
			}
			ICoverage encounterCoverage = encounter.getCoverage();
			if (encounterCoverage == null) {
				result = result.add(Result.SEVERITY.ERROR, 3, "Fehlender Fall bei Konsultation " + encounter.getLabel(),
						ret, true);
				continue;
			}
			if (coverage == null) {
				coverage = encounterCoverage;
				ret.setCoverage(coverage);
				coverage.setBillingProposalDate(null); // ggf. Rechnungsvorschlag löschen
			} else {
				if (!coverage.getId().equals(encounterCoverage.getId())) {
					result = result.add(Result.SEVERITY.ERROR, 4,
							"Die Liste enthält unterschiedliche Faelle " + encounter.getLabel(), ret, true);
					continue;
				}
			}
			if ((encounterDiagnosis == null) || (encounterDiagnosis.size() == 0)) {
				encounterDiagnosis = encounter.getDiagnoses();
			}
			if (encounter.getDate() == null) {
				result = result.add(Result.SEVERITY.ERROR, 5,
						"Ungültiges Datum bei Konsultation " + encounter.getLabel(), ret, true);
				continue;
			}
			actDate = actDate.with(encounter.getDate());
			if (actDate.isBefore(startDate)) {
				startDate = startDate.with(actDate);
			}
			if (actDate.isAfter(endDate)) {
				endDate = endDate.with(actDate);
			}
			sum.addMoney(EncounterServiceHolder.get().getSales(encounter));
		}
		// perform some checks
		if (coverage == null) {
			result = result.add(Result.SEVERITY.ERROR, 8,
					"Die Rechnung hat keinen gültigen Fall (" + getInvoiceDesc(ret) + ")", ret, true);
		} else {
			if (isBillingStrict() && !CoverageServiceHolder.get().isValid(coverage)) {
				result = result.add(Result.SEVERITY.ERROR, 8,
						"Die Rechnung hat keinen gültigen Fall (" + getInvoiceDesc(ret) + ")", ret, true);
			}
		}
		if (isBillingStrict()) {
			if (encounterDiagnosis == null || encounterDiagnosis.isEmpty()) {
				result = result.add(Result.SEVERITY.ERROR, 6,
						"Die Rechnung enthält keine Diagnose (" + getInvoiceDesc(ret) + ")", ret, true);
			}
		}

		ret.setDateFrom(startDate);
		ret.setDateTo(endDate);
		ret.setDate(LocalDate.now());
		ret.setState(InvoiceState.OPEN);
		ret.setStateDate(LocalDate.now());
		ret.setTotalAmount(sum);
		if (!result.isOK()) {
			return result;
		}
		// create and persist invoice billed and invoice
		CoreModelServiceHolder.get().save(ret);
		List<IInvoiceBilled> newInvoiceBilled = new ArrayList<>();
		for (IEncounter encounter : encounters) {
			encounter.setInvoice(ret);
			CoreModelServiceHolder.get().save(encounter);
			// save all verrechnet of this rechnung
			List<IBilled> encounterBilled = encounter.getBilled();
			for (IBilled billed : encounterBilled) {
				IInvoiceBilled invoiceBilled = new IInvoiceBilledBuilder(CoreModelServiceHolder.get(), ret, billed)
						.build();
				newInvoiceBilled.add(invoiceBilled);
			}
		}
		CoreModelServiceHolder.get().save((List<Identifiable>) (List<?>) newInvoiceBilled);
		if (ret.getOpenAmount().isZero()) {
			ret.setState(InvoiceState.PAID);
			CoreModelServiceHolder.get().save(ret);
		} else {
			if (coverage != null) {
				IAccountTransaction invoiceBilled = new IAccountTransactionBuilder(CoreModelServiceHolder.get(), ret,
						coverage.getPatient(), sum.negate(), ret.getDate(), "Rn " + ret.getNumber() + " erstellt.")
								.buildAndSave();
			}
		}
		return result.add(SEVERITY.OK, 0, "Ok", ret, false);
	}

	private boolean isBillingCheckZero() {
		Optional<IContact> userContact = ContextServiceHolder.get().getActiveUserContact();
		if (userContact.isPresent()) {
			return ConfigServiceHolder.get().get(userContact.get(),
					ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_ZERO_CHECK, false);
		}
		return false;
	}

	private boolean isBillingStrict() {
		Optional<IContact> userContact = ContextServiceHolder.get().getActiveUserContact();
		if (userContact.isPresent()) {
			return ConfigServiceHolder.get().get(userContact.get(),
					ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, true);
		}
		return true;
	}

	private static String getInvoiceDesc(IInvoice invoice) {
		StringBuilder sb = new StringBuilder();
		if (invoice == null) {
			sb.append("Keine Rechnungsnummer");
		} else {
			ICoverage coverage = invoice.getCoverage();
			sb.append("Rechnung: " + invoice.getNumber()).append(" / ");
			if (coverage == null) {
				sb.append("Kein Fall");
			} else {
				sb.append("Fall: " + coverage.getLabel()).append(" / ");
				IPatient pat = coverage.getPatient();
				if (pat == null) {
					sb.append("Kein Patient");
				} else {
					sb.append(pat.getLabel());
				}
			}
		}
		return sb.toString();
	}

	@Override
	public List<IEncounter> cancel(IInvoice invoice, boolean reopen) {
		InvoiceState invoiceState = invoice.getState();
		List<IEncounter> ret = Collections.emptyList();
		if (!InvoiceState.CANCELLED.equals(invoiceState) && !InvoiceState.DEPRECIATED.equals(invoiceState)) {
			Money amount = invoice.getTotalAmount();
			Money demandAmount = invoice.getDemandAmount();
			if (!demandAmount.isZero()) {
				amount.addMoney(demandAmount);
			}
			addPayment(invoice, amount, "Storno");
			if (reopen) {
				ret = removeEncounters(invoice);

				invoice.setState(InvoiceState.CANCELLED);
				CoreModelServiceHolder.get().save(invoice);
			} else {
				invoice.setState(InvoiceState.DEPRECIATED);
				CoreModelServiceHolder.get().save(invoice);
			}
		} else if (reopen && InvoiceState.CANCELLED.equals(invoiceState)) {
			// if bill is canceled ensure that all kons are opened
			ret = removeEncounters(invoice);
		}
		return ret;
	}

	private List<IEncounter> removeEncounters(IInvoice invoice) {
		List<IEncounter> encounters = invoice.getEncounters();
		for (IEncounter iEncounter : encounters) {
			iEncounter.setInvoice(null);
		}
		CoreModelServiceHolder.get().save(encounters);
		return encounters;
	}

	@Override
	public Optional<IInvoice> getInvoiceWithNumber(String number) {
		INamedQuery<IInvoice> query = CoreModelServiceHolder.get().getNamedQuery(IInvoice.class, "number");
		List<IInvoice> found = query.executeWithParameters(query.getParameterMap("number", number));
		if (found.size() > 0) {
			if (found.size() > 1) {
				logger.warn("Found " + found.size() + " invoices with number " + number + " using first");
			}
			return Optional.of(found.get(0));
		}
		return Optional.empty();
	}

	@Override
	public List<IInvoice> getInvoices(IEncounter encounter) {
		INamedQuery<IInvoiceBilled> query = CoreModelServiceHolder.get().getNamedQuery(IInvoiceBilled.class,
				"encounter");
		List<IInvoiceBilled> invoicebilled = query.executeWithParameters(query.getParameterMap("encounter", encounter));
		HashSet<IInvoice> uniqueInvoices = new HashSet<IInvoice>();
		invoicebilled.stream().filter(ib -> ib.getInvoice() != null).forEach(ib -> uniqueInvoices.add(ib.getInvoice()));
		return new ArrayList<IInvoice>(uniqueInvoices);
	}

	@Override
	public boolean hasStornoBeforeDate(IInvoice invoice, LocalDate date) {
		List<IPayment> zahlungen = invoice.getPayments();
		for (IPayment zahlung : zahlungen) {
			if (zahlung.getRemark().equals("Storno")) {
				if (zahlung.getDate().isBefore(date) || zahlung.getDate().equals(date)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getCombinedId(IInvoice invoice) {
		IPatient patient = invoice.getCoverage().getPatient();
		String pid;
		if (ConfigServiceHolder.get().get("PatIDMode", "number").equals("number")) {
			pid = StringUtils.leftPad(patient.getCode(), 6, '0');
		} else {
			pid = new TimeTool(patient.getDateOfBirth()).toString(TimeTool.DATE_COMPACT);
		}
		String nr = StringUtils.leftPad(invoice.getNumber(), 6, '0');
		return pid + nr;
	}

	@Override
	public Optional<IAccountTransaction> getAccountTransaction(IPayment payment) {
		IQuery<IAccountTransaction> query = CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		query.and(ModelPackage.Literals.IACCOUNT_TRANSACTION__PAYMENT, COMPARATOR.EQUALS, payment);
		return query.executeSingleResult();
	}

	@Override
	public void removePayment(IPayment payment) {
		IQuery<IAccountTransaction> query = CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		query.and(ModelPackage.Literals.IACCOUNT_TRANSACTION__PAYMENT, COMPARATOR.EQUALS, payment);
		CoreModelServiceHolder.get().remove(query.execute());

		if (payment.getInvoice() != null) {
			payment.getInvoice().addTrace(InvoiceConstants.CORRECTION, "Zahlung gelöscht");
		}
		CoreModelServiceHolder.get().delete(payment);
	}

	@Override
	public IPayment addPayment(IInvoice invoice, Money amount, String remark) {
		Money oldOpen = invoice.getOpenAmount();
		InvoiceState oldInvoiceState = invoice.getState();

		IPayment payment = new IPaymentBuilder(CoreModelServiceHolder.get(), invoice, amount, remark).buildAndSave();
		new IAccountTransactionBuilder(CoreModelServiceHolder.get(), payment).buildAndSave();

		Money newOffen = invoice.getOpenAmount();
		if (newOffen.isNeglectable()) {
			invoice.setState(InvoiceState.PAID);
		} else if (newOffen.isNegative()) {
			invoice.setState(InvoiceState.EXCESSIVE_PAYMENT);
		} else if (newOffen.getCents() < oldOpen.getCents()) {
			invoice.setState(InvoiceState.PARTIAL_PAYMENT);
		}
		if (invoice.getState() != oldInvoiceState) {
			CoreModelServiceHolder.get().save(invoice);
		}
		return payment;
	}
}
