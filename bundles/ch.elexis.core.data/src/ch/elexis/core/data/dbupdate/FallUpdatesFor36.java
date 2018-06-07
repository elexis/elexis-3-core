package ch.elexis.core.data.dbupdate;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;
import ch.elexis.core.data.extension.CoreOperationExtensionPoint;
import ch.elexis.core.data.util.IRunnableWithProgress;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Fall;
import ch.elexis.data.Query;

public class FallUpdatesFor36 {
	
	protected static Logger log = LoggerFactory.getLogger(FallUpdatesFor36.class);
	
	/**
	 * https://redmine.medelexis.ch/issues/11487
	 * 
	 * @since 3.6
	 */
	public static void transferLawAndCostBearerTo36Model(){
		String CONFIG_KEY = "FallGesetzCostBearerMigratedTo36";
		
		String value = CoreHub.globalCfg.get(CONFIG_KEY, null);
		if (value == null) {
			CoreHub.globalCfg.set(CONFIG_KEY, "-1"); // -1 == wip
			CoreHub.globalCfg.flush();
			
			IRunnableWithProgress irwp = new IRunnableWithProgress() {
				
				private StringBuilder errors = new StringBuilder();
				
				@Override
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException{
					
					monitor.beginTask("Aktualisiere Abrechnungssystem-Konfiguration", 2);
					log.info("Starting FallUpdatesFor36#transferLawAndCostBearerTo36Model");
					
					moveFallExtInfoBillingToFallBillingSystem(SubMonitor.convert(monitor, 1));
					
					SubMonitor monitor2 = SubMonitor.convert(monitor, 1);
					String[] abrechnungsSysteme = BillingSystem.getAbrechnungsSysteme();
					monitor2.beginTask("Aktualisiere Gesetz und Kostenträger",
						abrechnungsSysteme.length);
					for (String abrechnungssystem : abrechnungsSysteme) {
						monitor2
							.subTask("Aktualisiere Gesetz und Kostenträger für Abrechnungssystem ["
								+ abrechnungssystem + "]");
						// set formal law on billing system
						String abrechnungsSystemGesetz =
							BillingSystem.getBillingSystemConstant(abrechnungssystem, "gesetz");
						if (StringUtils.isNotEmpty(abrechnungsSystemGesetz)) {
							try {
								// try to configure formal
								BillingLaw billingLaw =
									BillingLaw.valueOf(abrechnungsSystemGesetz.toUpperCase());
								BillingSystem.setConfigurationValue(abrechnungssystem,
									BillingSystem.CFG_BILLINGLAW, billingLaw.name());
								// remove from all faelle#extinfo
								BillingSystem.removeExtInfoValueForAllFaelleOfBillingSystem(
									abrechnungsSystemGesetz, Arrays.asList(new String[] {
										"gesetz", "Gesetz"
								}));
								// remove constant from billing system
								BillingSystem.removeBillingSystemConstant(abrechnungssystem,
									"gesetz=" + abrechnungsSystemGesetz);
								BillingSystem.removeBillingSystemConstant(abrechnungssystem,
									"Gesetz=" + abrechnungsSystemGesetz);
							} catch (IllegalArgumentException iae) {
								log.error("Could not resolve law [{}] from billing systeem [{}]",
									abrechnungsSystemGesetz, abrechnungssystem);
								errors.append(
									"Fehler Gesetz-Konfiguration für " + abrechnungssystem + "\n");
							}
						} else {
							log.error("No gesetz constant found for billing system [{}]",
								abrechnungssystem);
						}
						
						// set Kostentraeger
						String requirements = BillingSystem.getRequirements(abrechnungssystem);
						// Kostenträger:K;Fallnummer:T
						if (requirements != null && requirements.contains("Kostenträger:K")) {
							BillingSystem.moveCostBearerFromExtinfoToDBRow(abrechnungssystem,
								"Kostenträger");
							requirements = requirements.replace("Kostenträger:K:;", "");
							requirements = requirements.replace("Kostenträger:K:", "");
							requirements = requirements.replace("Kostenträger:K;", "");
							requirements = requirements.replace("Kostenträger:K", "");
							CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
								+ abrechnungssystem + "/bedingungen", requirements); //$NON-NLS-1$
						} else {
							log.error("Could not find cost bearer entry for billing system [{}]",
								abrechnungssystem);
							errors.append("Kein Kostenträger-Konfiguration für ["
								+ abrechnungssystem + "] gefunden\n");
						}
						
						monitor2.worked(1);
					}
					
					CoreHub.globalCfg.set(CONFIG_KEY, StringConstants.ONE);
					CoreHub.globalCfg.flush();
					monitor.done();
					
					String errorString = errors.toString();
					StringBuilder message = new StringBuilder();
					if (StringUtils.isNotBlank(errorString)) {
						message.append(
							"Bei der automatisierten Aktualisierung der Abrechnungssysteme sind folgende Fehler aufgetreten:\n\n");
						message.append(errorString.toString() + "\n\n");
					} else {
						message.append(
							"Die automatisierte Übernahme wurde ohne Probleme abgeschlossen.");
					}
					message.append(
						"Bitte deaktivieren Sie, falls notwendig, manuell die Kostenträger-Auswahl.\nSiehe https://wiki.elexis.info/Ab4cf3");
					
					CoreOperationExtensionPoint.getCoreOperationAdvisor().openInformation(
						"Aktualisierung Abrechnungssystem - Automatische Übernahme",
						message.toString());
				}
				
			};
			
			AbstractCoreOperationAdvisor cod =
				CoreOperationExtensionPoint.getCoreOperationAdvisor();
			cod.showProgress(irwp, "Abrechnungssystem-Konfiguration aktualisieren");
		}
	}
	
	public static String moveFallExtInfoBillingToFallBillingSystem(IProgressMonitor pm){
		StringBuilder sb = new StringBuilder();
		Query<Fall> query = new Query<Fall>(Fall.class);
		query.clear(true);
		List<Fall> allFaelle = query.execute();
		pm.beginTask("Moving Fall#ExtInfo#billing values...", allFaelle.size());
		for (Fall fall : allFaelle) {
			String billingSystem =
				(String) fall.getExtInfoStoredObjectByKey(FallConstants.FLD_EXTINFO_BILLING);
			if (billingSystem != null) {
				fall.set(Fall.FLD_BILLINGSYSTEM, billingSystem);
				fall.setExtInfoStoredObjectByKey(FallConstants.FLD_EXTINFO_BILLING, null);
				sb.append("[" + fall.getId() + "] Moving Fall#ExtInfo#Billing [" + billingSystem
					+ "] to table\n");
			}
			pm.worked(1);
		}
		pm.done();
		return sb.toString();
	}
	
}
