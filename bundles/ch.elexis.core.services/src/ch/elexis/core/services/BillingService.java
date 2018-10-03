package ch.elexis.core.services;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
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
		
		IMandator mandator = encounter.getMandator();
		//		checkMandant = !CoreHub.acl.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
		//		boolean mandantOK = true;
		//		boolean billOK = true;
		//		Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
		//		boolean bMandantLoggedIn = (mandator != null);
		//		
		//		// if m is null, ignore checks (return true)
		//		if (m != null && mandator != null) {
		//			if (checkMandant && !(m.getId().equals(mandator.getId()))) {
		//				mandantOK = false;
		//			}
		//			
		//			if (checkBill) {
		//				Rechnung rn = getRechnung();
		//				if (rn == null || (!rn.exists())) {
		//					billOK = true;
		//				} else {
		//					int stat = rn.getStatus();
		//					if (stat == RnStatus.STORNIERT) {
		//						billOK = true;
		//					} else {
		//						billOK = false;
		//					}
		//				}
		//			}
		//		}
		//		
		//		boolean ok = billOK && mandantOK && bMandantLoggedIn;
		//		if (ok) {
		//			return true;
		//		}
		//		
		//		// something is not ok
		//		if (showError) {
		//			String msg = "";
		//			if (!bMandantLoggedIn) {
		//				msg = "Es ist kein Mandant eingeloggt";
		//			} else {
		//				if (!billOK) {
		//					msg = "Für diese Behandlung wurde bereits eine Rechnung erstellt.";
		//				} else {
		//					msg = "Diese Behandlung ist nicht von Ihnen";
		//				}
		//			}
		//			
		//			MessageEvent.fireError("Konsultation kann nicht geändert werden", msg);
		//		}
		//		
		//		return false;
		
		return new Result<>(encounter);
	}
	
	@Override
	public Result<IBillable> bill(IBillable billable, IEncounter encounter, double amount){
		Result<IBillable> verificationResult =
			billable.getVerifier().verifyAdd(billable, encounter);
		if (verificationResult.isOK()) {
			Result<IBillable> optifierResult =
				billable.getOptifier().add(billable, encounter, amount);
			return optifierResult;
		} else {
			return verificationResult;
		}
	}
}
