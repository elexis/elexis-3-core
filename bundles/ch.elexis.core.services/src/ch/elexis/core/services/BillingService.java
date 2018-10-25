package ch.elexis.core.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

@Component
public class BillingService implements IBillingService {
	
	@Reference
	private IAccessControlService accessControlService;
	
	@Override
	public Result<IEncounter> isEditable(IEncounter encounter){
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			if (!coverage.isOpen()) {
				return new Result<IEncounter>(SEVERITY.WARNING, 0,
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
		Result<IBilled> verificationResult =
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
			
			return optifierResult;
		} else {
			return verificationResult;
		}
	}
}
