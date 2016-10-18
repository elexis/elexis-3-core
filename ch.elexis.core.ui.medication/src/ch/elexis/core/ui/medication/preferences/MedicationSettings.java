package ch.elexis.core.ui.medication.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;

public class MedicationSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public MedicationSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG,
			"Signatur Dialog auch bei vorhandener Std. Signatur anzeigen", getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION,
			"Bei Signatur Dialog Abgabe vorselektieren", getFieldEditorParent()));
	}
	
	@Override
	public void init(IWorkbench workbench){}
}
