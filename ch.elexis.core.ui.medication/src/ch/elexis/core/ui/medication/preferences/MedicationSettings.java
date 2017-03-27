package ch.elexis.core.ui.medication.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.medication.property.MedicationUiTester;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;

public class MedicationSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	private BooleanFieldEditor sortingFieldEditor;
	
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
		
		addField(new BooleanFieldEditor(
			CreatePrescriptionHelper.MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT,
			"Beim dispensieren auf Artikelstamm prüfen, und konvertieren", getFieldEditorParent()));
		
		sortingFieldEditor =
			new BooleanFieldEditor(MedicationUiTester.MEDICATION_SETTINGS_SHOW_CUSTOM_SORT,
				"Persönliche Sortierung anzeigen", getFieldEditorParent());
		addField(sortingFieldEditor);
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
