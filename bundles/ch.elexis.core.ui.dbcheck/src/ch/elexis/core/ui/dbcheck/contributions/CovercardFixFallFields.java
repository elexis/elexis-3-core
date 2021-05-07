package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Fall;
import ch.elexis.data.Query;

public class CovercardFixFallFields extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Bitte warten, Fälle werden geladen ...", IProgressMonitor.UNKNOWN);
		
		String billingSystemStd = ConfigServiceHolder.getGlobal("covercard/billinginfo/std/method", null);
		String billingSystemExc = ConfigServiceHolder.getGlobal("covercard/billinginfo/exc/method", null);
		
		updateCovercardBillingSystem();
		
		if (StringUtils.isNotBlank(billingSystemStd) || StringUtils.isNotBlank(billingSystemExc)) {
			Query<Fall> query = new Query<>(Fall.class);
			query.startGroup();
			query.add(Fall.FLD_BILLINGSYSTEM, Query.EQUALS, "Covercard");
			if (StringUtils.isNotBlank(billingSystemStd)) {
				query.or();
				query.add(Fall.FLD_BILLINGSYSTEM, Query.EQUALS, billingSystemStd);
			}
			if (StringUtils.isNotBlank(billingSystemExc)) {
				query.or();
				query.add(Fall.FLD_BILLINGSYSTEM, Query.EQUALS, billingSystemExc);
			}
			query.endGroup();
			List<Fall> coverages = query.execute();
			pm.beginTask(
				"Es wurden " + coverages.size()
					+ " Fälle geladen diese werden nun geprüft, bitte warten ...",
				coverages.size());
			int covercardCount = 0;
			for (Fall fall : coverages) {
				Object infoElement = fall.getInfoElement("Covercard");
				if (infoElement instanceof Map) {
					covercardCount++;
					String billingSystem = fall.getAbrechnungsSystem();
					updateBillingSystem(billingSystem);
					@SuppressWarnings("unchecked")
					Map<String, String> fields = (Map<String, String>) infoElement;
					fall.setInfoString("Versicherungsnummer",
						StringUtils.defaultIfBlank(fields.get("NUM_ASSURE"), ""));
					fall.setInfoString("VEKANr",
						StringUtils.defaultIfBlank(fields.get("NUM_UE"), ""));
					if (StringUtils.isBlank(fields.get("VAL_CARTE"))) {
						fall.setInfoString("VEKAValid", "20991231");
					} else {
						fall.setInfoString("VEKAValid", fields.get("VAL_CARTE"));
					}
				}
				pm.worked(1);
			}
			output.append(
				"Es wurden " + covercardCount + " Fälle mit covercard Informationen geprüft");
			pm.done();
		}
		
		return output.toString();
	}
	
	private void updateBillingSystem(String billingSystem){
		String requirements = BillingSystem.getRequirements(billingSystem);
		if (!requirements.contains("Versicherungsnummer:T")) {
			if (StringUtils.isNotBlank(requirements)) {
				requirements += ";Versicherungsnummer:T";
			} else {
				requirements = "Versicherungsnummer:T";
			}
			ConfigServiceHolder.setGlobal(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/bedingungen", requirements);
		}
		String optionals = BillingSystem.getOptionals(billingSystem);
		if(optionals == null) {
			optionals = "VEKANr:T";
		} else {
			if(!optionals.contains("VEKANr")) {
				if (optionals.endsWith(":")) {
					optionals = optionals.substring(0, optionals.length() - 1);
				}
				optionals += ";VEKANr:T";
			}
		}
		ConfigServiceHolder.setGlobal(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/fakultativ", optionals);
	}
	
	private void updateCovercardBillingSystem(){
		String requirements = BillingSystem.getRequirements("Covercard");
		if (requirements.contains("Versicherten-Nummer:T")) {
			if (requirements.contains(";Versicherten-Nummer:T")) {
				requirements = requirements.replace(";Versicherten-Nummer:T", "");
			} else if (requirements.contains("Versicherten-Nummer:T;")) {
				requirements = requirements.replace("Versicherten-Nummer:T;", "");
			} else {
				requirements = requirements.replace("Versicherten-Nummer:T", "");
			}
		}
		ConfigServiceHolder.setGlobal(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ "Covercard" + "/bedingungen", requirements);
		String optionals = BillingSystem.getOptionals("Covercard");
		if (optionals == null || !optionals.contains("VEKANr")) {
			if (StringUtils.isNotBlank(optionals)) {
				optionals += ";VEKANr:T";
			} else {
				optionals = "VEKANr:T";
			}
			ConfigServiceHolder.setGlobal(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ "Covercard" + "/fakultativ", optionals);
		}
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Covercard Informationen der Fälle prüfen und richtig stellen";
	}
	
}
