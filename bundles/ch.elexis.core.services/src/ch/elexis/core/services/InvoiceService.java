package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.builder.IAccountTransactionBuilder;
import ch.elexis.core.model.builder.IInvoiceBilledBuilder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

@Component
public class InvoiceService implements IInvoiceService {
	
	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public Result<IInvoice> invoice(List<IEncounter> encounters){
		Result<IInvoice> result = new Result<>();
		
		if (encounters == null || encounters.isEmpty()) {
			return result.add(Result.SEVERITY.WARNING, 1,
				"Die Rechnung enthält keine Behandlungen (Konsultationen)", null, true); // js:
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
			if (encounter.getBilled().isEmpty()
				|| EncounterServiceHolder.get().getSales(encounter).isZero()) {
				LoggerFactory.getLogger(getClass()).warn(
					"Ignoring encounter [" + encounter.getLabel() + "] with sales amount zero.");
			} else {
				List<IBilled> encounterBilled = encounter.getBilled();
				for (IBilled billed : encounterBilled) {
					if (billed.getNetPrice().isZero() && isBillingCheckZero()) {
						IPatient pat = encounter.getCoverage().getPatient();
						String msg = "Die Konsultation vom "
							+ encounter.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
							+ " für\nPatient Nr. " + pat.getPatientNr() + ", " + pat.getLastName()
							+ ", " + pat.getFirstName() + ", "
							+ pat.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
							+ "\n" + "enthält mindestens eine Leistung zum Preis 0.00.\n"
							+ "\nDie Ärztekasse würde so eine Rechnung zurückgeben.\n\n";
						return result.add(Result.SEVERITY.WARNING, 1, msg
							+ "Diese Rechnung wird jetzt nicht erstellt."
							+ "\n\nBitte prüfen Sie die verrechneten Leistungen,"
							+ "oder verschieben Sie die Konsultation zu einem später zu verrechnenden Fall!",
							null, true);
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
						"Die Liste enthält unterschiedliche Rechnungssteller "
							+ encounter.getLabel(),
						ret, true);
					continue;
				}
			}
			ICoverage encounterCoverage = encounter.getCoverage();
			if (encounterCoverage == null) {
				result = result.add(Result.SEVERITY.ERROR, 3,
					"Fehlender Fall bei Konsultation " + encounter.getLabel(), ret, true);
				continue;
			}
			if (coverage == null) {
				coverage = encounterCoverage;
				ret.setCoverage(coverage);
				coverage.setBillingProposalDate(null); // ggf. Rechnungsvorschlag löschen
			} else {
				if (!coverage.getId().equals(encounterCoverage.getId())) {
					result = result.add(Result.SEVERITY.ERROR, 4,
						"Die Liste enthält unterschiedliche Faelle " + encounter.getLabel(), ret,
						true);
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
			actDate.adjustInto(encounter.getDate());
			if (actDate.isBefore(startDate)) {
				startDate.adjustInto(actDate);
			}
			if (actDate.isAfter(endDate)) {
				endDate.adjustInto(actDate);
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
					"Die Rechnung hat keinen gültigen Fall (" + getInvoiceDesc(ret) + ")", ret,
					true);
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
				IInvoiceBilled invoiceBilled =
					new IInvoiceBilledBuilder(CoreModelServiceHolder.get(), ret, billed).build();
				newInvoiceBilled.add(invoiceBilled);
			}
		}
		CoreModelServiceHolder.get().save((List<Identifiable>) (List<?>) newInvoiceBilled);
		if (ret.getOpenAmount().isZero()) {
			ret.setState(InvoiceState.PAID);
			CoreModelServiceHolder.get().save(ret);
		} else {
			if (coverage != null) {
				IAccountTransaction invoiceBilled =
					new IAccountTransactionBuilder(CoreModelServiceHolder.get(), ret,
						coverage.getPatient(), sum.negate(), ret.getDate(),
						"Rn " + ret.getNumber() + " erstellt.").buildAndSave();
			}
		}
		return result.add(SEVERITY.OK, 0, "Ok", ret, false);
	}
	
	private boolean isBillingCheckZero(){
		Optional<IContact> userContact = ContextServiceHolder.get().getActiveUserContact();
		if (userContact.isPresent()) {
			return ConfigServiceHolder.get().get(userContact.get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_ZERO_CHECK, false);
		}
		return false;
	}
	
	private boolean isBillingStrict(){
		Optional<IContact> userContact = ContextServiceHolder.get().getActiveUserContact();
		if (userContact.isPresent()) {
			return ConfigServiceHolder.get().get(userContact.get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, true);
		}
		return true;
	}
	
	private static String getInvoiceDesc(IInvoice invoice){
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
	public List<IEncounter> cancel(IInvoice invoice, boolean reopen){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IInvoice> getInvoiceWithNumber(String number){
		INamedQuery<IInvoice> query =
			CoreModelServiceHolder.get().getNamedQuery(IInvoice.class, "number");
		List<IInvoice> found = query.executeWithParameters(query.getParameterMap("number", number));
		if (found.size() > 0) {
			if(found.size() > 1) {
				logger.warn("Found " + found.size() + " invoices with number " + number + " using first");
			}
			return Optional.of(found.get(0));
		}
		return Optional.empty();
	}
	
	@Override
	public List<IInvoice> getInvoices(IEncounter encounter){
		INamedQuery<IInvoiceBilled> query =
			CoreModelServiceHolder.get().getNamedQuery(IInvoiceBilled.class, "encounter");
		List<IInvoiceBilled> invoicebilled =
			query.executeWithParameters(query.getParameterMap("encounter", encounter));
		HashSet<IInvoice> uniqueInvoices = new HashSet<IInvoice>();
		invoicebilled.forEach(ib -> uniqueInvoices.add(ib.getInvoice()));
		return new ArrayList<IInvoice>(uniqueInvoices);
	}
}
