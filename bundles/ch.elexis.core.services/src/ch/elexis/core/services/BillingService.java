package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

@Component
public class BillingService implements IBillingService {
	
	@Reference
	private IAccessControlService accessControlService;
	
	private List<IBilledAdjuster> adjusters = new ArrayList<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setBilledAdjuster(IBilledAdjuster adjuster){
		if (!adjusters.contains(adjuster)) {
			adjusters.add(adjuster);
		}
	}
	
	public void unsetBilledAdjuster(IBilledAdjuster adjuster){
		if (adjusters.contains(adjuster)) {
			adjusters.remove(adjuster);
		}
	}
	
	@Override
	public Result<IEncounter> isEditable(IEncounter encounter){
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			if (!coverage.isOpen()) {
				return new Result<>(SEVERITY.WARNING, 0,
					"Diese Konsultation gehört zu einem abgeschlossenen Fall", encounter, false);
			}
		}
		
		IMandator encounterMandator = encounter.getMandator();
		boolean checkMandant =
			!accessControlService.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
		boolean mandatorOk = true;
		boolean invoiceOk = true;
		IMandator activeMandator =
			ContextServiceHolder.get().getRootContext().getActiveMandator().orElse(null);
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
			String msg = "";
			if (!mandatorLoggedIn) {
				msg = "Es ist kein Mandant eingeloggt";
			} else {
				if (!invoiceOk) {
					msg = "Für diese Behandlung wurde bereits eine Rechnung erstellt.";
				} else {
					msg = "Diese Behandlung ist nicht von Ihnen";
				}
			}
			return new Result<IEncounter>(SEVERITY.WARNING, 0, msg, encounter, false);
		}
	}
	
	@Override
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount){
		Result<IBillable> verificationResult =
			billable.getVerifier().verifyAdd(billable, encounter, amount);
		if (verificationResult.isOK()) {
			IBillableOptifier optifier = billable.getOptifier();
			Result<IBilled> optifierResult = optifier.add(billable, encounter, amount);
			
			// TODO refactor
			if (!optifierResult.isOK() && optifierResult.getCode() == 11) {
				String initialResult = optifierResult.toString();
				// code 11 is tarmed exclusion due to side see TarmedOptifier#EXKLUSIONSIDE
				// set a context variable to specify the side see TarmedLeistung#SIDE, TarmedLeistung#SIDE_L, TarmedLeistung#SIDE_R
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
			
			for (IBilledAdjuster iBilledAdjuster : adjusters) {
				iBilledAdjuster.adjust(optifierResult.get());
			}
			
			return optifierResult;
		} else {
			return translateResult(verificationResult);
		}
	}
	
	private Result<IBilled> translateResult(Result<IBillable> verificationResult){
		Result<IBilled> ret = new Result<>();
		verificationResult.getMessages().forEach(msg -> {
			ret.addMessage(msg.getSeverity(), msg.getText());
		});
		return ret;
	}
	
	@Override
	public Optional<IBillingSystemFactor> getBillingSystemFactor(String system, LocalDate date){
		IQuery<IBillingSystemFactor> query =
			CoreModelServiceHolder.get().getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS, system);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_FROM,
			COMPARATOR.LESS_OR_EQUAL, date);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_TO,
			COMPARATOR.GREATER_OR_EQUAL, date);
		return query.executeSingleResult();
	}
}
