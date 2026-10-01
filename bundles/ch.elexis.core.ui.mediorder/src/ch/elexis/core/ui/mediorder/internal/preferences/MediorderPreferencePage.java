package ch.elexis.core.ui.mediorder.internal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;

public class MediorderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "ch.elexis.core.ui.mediorder.preferences"; //$NON-NLS-1$

	public MediorderPreferencePage() {
		super(GRID);
		ConfigServicePreferenceStore prefs = new ConfigServicePreferenceStore(Scope.GLOBAL);
		prefs.setDefault(Preferences.MEDIORDER_AUTO_PRINT_LABELS, Preferences.MEDIORDER_AUTO_PRINT_LABELS_DEFAULT);
		setPreferenceStore(prefs);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(Preferences.MEDIORDER_AUTO_PRINT_LABELS,
				Messages.Mediorder_preferences_autoPrintLabels, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to initialize
	}
}
