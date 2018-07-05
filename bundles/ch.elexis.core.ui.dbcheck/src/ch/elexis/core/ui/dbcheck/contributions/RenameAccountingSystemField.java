package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dbcheck.contributions.dialogs.RenameAccountingSysFieldDialog;
import ch.elexis.core.ui.dbcheck.contributions.dialogs.SelectValueAccountingSysFieldDialog;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Query;

public class RenameAccountingSystemField extends ExternalMaintenance {
	private static final String DESCRIPTION = "Feld aus Abrechnungssystem umbenennen [3341]";
	private static final String REQUIRED = "/bedingungen";
	private static final String OPTIONAL = "/fakultativ";
	private static final String SEPARATOR = ":";
	
	private String accountingSystem;
	private String currFieldName;
	private String newFieldName;
	
	int proceedAsBefore = -1;
	boolean useLegacyValue = true;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		if (openRenameAccountingSysFieldDialog()) {
			StringBuilder output = new StringBuilder();
			String beginTask =
				"Feld '" + currFieldName + "' aus Abrechnungssystem [" + accountingSystem
					+ "] in '" + newFieldName + "' umbenennen";
			pm.beginTask(beginTask, 3);
			output.append(beginTask + "\n\n");
			
			pm.subTask("Lade Fälle des betroffenen Abrechnungssystem ...");
			Query<Fall> qbe = new Query<Fall>(Fall.class);
			qbe.add(FallConstants.FLD_EXTINFO_BILLING, Query.EQUALS, accountingSystem);
			qbe.addToken(Fall.FLD_DATUM_BIS + " is NULL OR " + Fall.FLD_DATUM_BIS + " = ''");
			List<Fall> fallList = qbe.execute();
			pm.worked(1);
			
			if (fallList.isEmpty()) {
				output.append("Keine relevanten Fälle gefunden\n");
			}
			
			pm.subTask("Feldbezeichnung in einzelnen Fällen wird aktualisiert ...");
			for (Fall fall : fallList) {
				String currField = fall.getInfoString(currFieldName);
				String newField = fall.getInfoString(newFieldName);
				
				if (!currField.isEmpty()) {
					// newField isn't occupied yet
					if (newField.isEmpty()) {
						// copy value to new field and remove currField entry
						fall.setInfoString(newFieldName, currField);
					} else if (!currField.equals(newField)) {
						// -1 not initialized yet or 1 do not proceed as before
						if (proceedAsBefore == -1 || proceedAsBefore == 1) {
							openSelectValueAccountingSysFieldDialog(fall, currField, newField);
						}
						
						// use value of old field name
						if (useLegacyValue) {
							fall.setInfoString(newFieldName, currField);
						}
					}
					// delete legacy field
					fall.clearInfoString(currFieldName);
				}
			}
			pm.worked(1);
			output.append("Feldbezeichnungen für " + fallList.size() + " Fälle aktualisiert\n");
			
			//update configuration of accounting system
			pm.subTask("Globale Einstellungen werden aktualisiert... ");
			String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + accountingSystem;
			String updatedConfig = updateFieldConfiguration(key, currFieldName, newFieldName);
			pm.worked(1);
			
			output.append("Globale Einstellung '" + updatedConfig + "' wurden aktualisiert!\n\n");
			output.append("Umbenennung abgeschlossen!");
			pm.done();
			
			return output.toString();
		} else {
			return DESCRIPTION + " abgebrochen";
		}
	}
	
	private void openSelectValueAccountingSysFieldDialog(Fall fall, String currField,
		String newField){
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			
			@Override
			public void run(){
				SelectValueAccountingSysFieldDialog replaceWhichDialog =
					new SelectValueAccountingSysFieldDialog(UiDesk.getTopShell(), fall.getPatient(),
						accountingSystem, currFieldName, currField, newFieldName, newField);
				
				replaceWhichDialog.open();
				proceedAsBefore = replaceWhichDialog.rememberProceedure() ? 0 : 1;
				useLegacyValue = replaceWhichDialog.useLegacyValue();
			}
		});
		
	}
	
	private boolean openRenameAccountingSysFieldDialog(){
		accountingSystem = "";
		currFieldName = "";
		newFieldName = "";
		
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			
			@Override
			public void run(){
				RenameAccountingSysFieldDialog rasfDialog =
					new RenameAccountingSysFieldDialog(display.getActiveShell());
				if (rasfDialog.open() == Dialog.OK) {
					accountingSystem = rasfDialog.getAccountingSystem();
					currFieldName = rasfDialog.getPresentFieldName();
					newFieldName = rasfDialog.getNewFieldName();
				}
			}
		});
		return !accountingSystem.isEmpty() && !currFieldName.isEmpty() && !newFieldName.isEmpty();
	}
	
	/**
	 * updates the accounting systems configuration. checks for containment in required fields first
	 * and than in optional fields
	 * 
	 * @param key
	 *            configuration key of accounting system
	 * @param currName
	 *            field name as it is
	 * @param newName
	 *            to set for currName
	 * 
	 * @return key of the updated config
	 */
	private String updateFieldConfiguration(String key, String currName, String newName){
		String configKey = key + REQUIRED;
		String config = CoreHub.globalCfg.get(configKey, null);
		// use optionals if requireds doesn't contain field
		if (config == null || !config.contains(currName + SEPARATOR)) {
			configKey = key + OPTIONAL;
			config = CoreHub.globalCfg.get(configKey, null);
		}
		// rename field
		String updatedConfig = config.replace(currName + SEPARATOR, newName + SEPARATOR);
		CoreHub.globalCfg.set(configKey, updatedConfig);
		CoreHub.globalCfg.flush();
		
		return configKey;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return DESCRIPTION;
	}
	
}
