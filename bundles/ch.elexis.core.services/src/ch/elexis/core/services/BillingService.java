package ch.elexis.core.services;

import org.osgi.service.component.annotations.Component;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IBillable;
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
		boolean checkMandant = !CoreHub.acl.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
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
			Result<IBilled> optifierResult =
				billable.getOptifier().add(billable, encounter, amount);
			return optifierResult;
		} else {
			return verificationResult;
		}
	}
}
