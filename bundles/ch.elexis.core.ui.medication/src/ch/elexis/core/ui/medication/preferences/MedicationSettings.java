package ch.elexis.core.ui.medication.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.preferences.Messages;
import ch.elexis.core.ui.medication.property.MedicationUiTester;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;

public class MedicationSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	private BooleanFieldEditor sortingFieldEditor;
	
	public MedicationSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
		getPreferenceStore().setDefault(Preferences.MEDICATION_SETTINGS_EMEDIPLAN_HEADER_COMMENT, Messages.Medication_headerComment);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG,
			"Signatur Dialog auch bei vorhandener Std. Signatur anzeigen", getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION,
			"Bei Signatur Dialog Abgabe vorselektieren", getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT,
			"Beim dispensieren auf Artikelstamm prüfen, und konvertieren", getFieldEditorParent()));
		
		sortingFieldEditor =
			new BooleanFieldEditor(MedicationUiTester.MEDICATION_SETTINGS_SHOW_CUSTOM_SORT,
				"Persönliche Sortierung anzeigen", getFieldEditorParent());
		addField(sortingFieldEditor);
		addField(new BooleanFieldEditor(Preferences.USR_SUPPRESS_INTERACTION_CHECK,
				ch.elexis.core.l10n.Messages.UserSettings2_SuppressInteractionCheck, getFieldEditorParent()));

		addField(new MultilineFieldEditor(Preferences.MEDICATION_SETTINGS_EMEDIPLAN_HEADER_COMMENT, "Eine Bemerkung auf dem eMediplan anzeigen"
			, getFieldEditorParent()));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event){
		super.propertyChange(event);
		
		// make sure custom sorting is disabled
		if (event.getSource() == sortingFieldEditor) {
			MedicationView view =
				(MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(MedicationView.PART_ID);
			if (view != null) {
				if (event.getNewValue() == Boolean.FALSE) {
					view.setMedicationTableViewerComparator(ViewerSortOrder.DEFAULT);
				}
			}
		}
	}
	
	@Override
	public void init(IWorkbench workbench){}
}
