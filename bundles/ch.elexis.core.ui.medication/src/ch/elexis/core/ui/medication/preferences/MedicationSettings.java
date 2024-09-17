package ch.elexis.core.ui.medication.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.medication.property.MedicationUiTester;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.core.ui.preferences.inputs.MultilineFieldEditor;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;

public class MedicationSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor sortingFieldEditor;
	private BooleanFieldEditor predefinedSymptomDaysFieldEditor;
	private IntegerFieldEditor symptomDurationFieldEditor;

	public MedicationSettings() {
		super(GRID);
		setPreferenceStore(new ConfigServicePreferenceStore(Scope.USER));
		getPreferenceStore().setDefault(Preferences.MEDICATION_SETTINGS_EMEDIPLAN_HEADER_COMMENT,
				Messages.Medication_headerComment);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(CreatePrescriptionHelper.MEDICATION_SETTINGS_ALWAYS_SHOW_SIGNATURE_DIALOG,
				ch.elexis.core.l10n.Messages.MedicationSettings_ShowSignatureDialog, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CreatePrescriptionHelper.MEDICATION_SETTINGS_SIGNATURE_STD_DISPENSATION,
				ch.elexis.core.l10n.Messages.MedicationSettings_PreselectSignatureDispensation,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(CreatePrescriptionHelper.MEDICATION_SETTINGS_DISPENSE_ARTIKELSTAMM_CONVERT,
				ch.elexis.core.l10n.Messages.MedicationSettings_CheckAndConvertItemMaster, getFieldEditorParent()));
		sortingFieldEditor = new BooleanFieldEditor(MedicationUiTester.MEDICATION_SETTINGS_SHOW_CUSTOM_SORT,
				ch.elexis.core.l10n.Messages.MedicationSettings_ShowCustomSorting, getFieldEditorParent());
		addField(sortingFieldEditor);
		addField(new BooleanFieldEditor(Preferences.USR_SUPPRESS_INTERACTION_CHECK,
				ch.elexis.core.l10n.Messages.UserSettings2_SuppressInteractionCheck, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.MEDICATION_SETTINGS_SHOW_DIALOG_ON_BILLING,
				ch.elexis.core.l10n.Messages.Medication_SettingsShowDialogOnBilling,
				getFieldEditorParent()));
		predefinedSymptomDaysFieldEditor = new BooleanFieldEditor(Preferences.MEDICATION_SETTINGS_DEFAULT_SYMPTOMS,
				ch.elexis.core.l10n.Messages.MedicationSettings_EnablePredefinedSymptomDays, getFieldEditorParent());
		addField(predefinedSymptomDaysFieldEditor);
		getPreferenceStore().setDefault(Preferences.MEDICATION_SETTINGS_SYMPTOM_DURATION, 30);
		symptomDurationFieldEditor = new IntegerFieldEditor(Preferences.MEDICATION_SETTINGS_SYMPTOM_DURATION,
				ch.elexis.core.l10n.Messages.MedicationSettings_NumberOfStopDays, getFieldEditorParent(), 3);
		symptomDurationFieldEditor.setEnabled(
				getPreferenceStore().getBoolean(Preferences.MEDICATION_SETTINGS_DEFAULT_SYMPTOMS),
				getFieldEditorParent());
		addField(symptomDurationFieldEditor);
		addField(new MultilineFieldEditor(Preferences.MEDICATION_SETTINGS_EMEDIPLAN_HEADER_COMMENT,
				ch.elexis.core.l10n.Messages.MedicationSettings_ShowCommentOnEMediplan, getFieldEditorParent()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == predefinedSymptomDaysFieldEditor) {
			boolean enabled = (Boolean) event.getNewValue();
			symptomDurationFieldEditor.setEnabled(enabled, getFieldEditorParent());
		}
		if (event.getSource() == sortingFieldEditor) {
			MedicationView view = (MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView(MedicationView.PART_ID);
			if (view != null) {
				if (event.getNewValue() == Boolean.FALSE) {
					view.setMedicationTableViewerComparator(ViewerSortOrder.DEFAULT);
				}
			}
		}
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
