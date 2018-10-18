package ch.elexis.core.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.rgw.tools.Result;

@Component
public class EncounterService implements IEncounterService {
	
	@Reference
	private IAccessControlService accessControlService;
	
	@Reference
	private IBillingService billingService;
	
	@Override
	public boolean isEditable(IEncounter encounter){
		boolean editable = false;
		boolean hasRight =
			accessControlService.request(AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED);
		if (hasRight) {
			// user has right to change encounter. in this case, the user
			// may change the text even if the encounter has already been
			// billed, so don't check if it is billed
			editable = isEditableInternal(encounter);
		} else {
			// normal case, check all
			editable = billingService.isEditable(encounter).isOK();
		}
		
		return editable;
	}
	
	public Result<IEncounter> transferToCoverage(IEncounter encounter, ICoverage coverage,
		boolean ignoreEditable){
		Result<IEncounter> editableResult = billingService.isEditable(encounter);
		if (ignoreEditable || editableResult.isOK()) {
			ICoverage encounterCovearage = encounter.getCoverage();
			encounter.setCoverage(coverage);
			if (encounterCovearage != null) {
				ch.elexis.core.services.ICodeElementService codeElementService =
					CodeElementServiceHolder.get();
				HashMap<Object, Object> context = getCodeElementServiceContext(encounter);
				List<IBilled> encounterBilled = encounter.getBilled();
				for (IBilled billed : encounterBilled) {
					IBillable billable = billed.getBillable();
					// TODO update after getFactor and getPoints methods are established
					// tarmed needs to be recharged
					//					if (isTarmed(billed)) {
					//						// make sure verrechenbar is matching for the kons
					//						Optional<ICodeElement> matchingVerrechenbar =
					//							codeElementService.createFromString(billable.getCodeSystemName(),
					//								billable.getCode(), context);
					//						if (matchingVerrechenbar.isPresent()) {
					//							int amount = billed.getZahl();
					//							removeLeistung(billed);
					//							for (int i = 0; i < amount; i++) {
					//								addLeistung((IVerrechenbar) matchingVerrechenbar.get());
					//							}
					//						} else {
					//							MessageEvent.fireInformation("Info",
					//								"Achtung: durch den Fall wechsel wurde die Position "
					//									+ billable.getCode()
					//									+ " automatisch entfernt, da diese im neuen Fall nicht vorhanden ist.");
					//							removeLeistung(billed);
					//						}
					//					} else {
					//						TimeTool date = new TimeTool(billed.getKons().getDatum());
					//						double factor = billable.getFactor(date, f);
					//						billed.set(Verrechnet.SCALE_SELLING, Double.toString(factor));
					//					}
				}
			}
			CoreModelServiceHolder.get()
				.save(Arrays.asList(encounter, coverage, encounterCovearage));
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
		} else if (!editableResult.isOK()) {
			return editableResult;
		}
		return new Result<IEncounter>(encounter);
	}
	
	private HashMap<Object, Object> getCodeElementServiceContext(IEncounter encounter){
		HashMap<Object, Object> ret = new HashMap<>();
		ret.put(ICodeElementService.ContextKeys.CONSULTATION, encounter);
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			ret.put(ICodeElementService.ContextKeys.COVERAGE, coverage);
		}
		return ret;
	}
	
	private boolean isEditableInternal(IEncounter encounter){
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			if (!coverage.isOpen()) {
				return false;
			}
		}
		
		IMandator encounterMandator = encounter.getMandator();
		boolean checkMandant =
			!accessControlService.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
		boolean mandatorOK = true;
		IMandator activeMandator =
			ContextServiceHolder.get().getRootContext().getActiveMandator().orElse(null);
		boolean mandatorLoggedIn = (activeMandator != null);
		
		// if m is null, ignore checks (return true)
		if (encounterMandator != null && activeMandator != null) {
			if (checkMandant && !(encounterMandator.getId().equals(activeMandator.getId()))) {
				mandatorOK = false;
			}
		}
		
		boolean ok = mandatorOK && mandatorLoggedIn;
		if (ok) {
			return true;
		} else {
			return false;
		}
	}
}
