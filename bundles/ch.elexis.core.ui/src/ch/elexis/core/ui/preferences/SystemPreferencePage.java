package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.inputs.KontaktFieldEditor;

public class SystemPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	private SettingsPreferenceStore prefs = new SettingsPreferenceStore(CoreHub.localCfg);
	
	/**
	 * Create the preference page.
	 */
	public SystemPreferencePage(){
		super(GRID);
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		setPreferenceStore(prefs);
	}
	
	@Override
	protected void createFieldEditors(){
		FieldEditor editor;
		
		editor = new StringFieldEditor(Preferences.STATION_IDENT_TEXT,
			Messages.SystemPreferencePage_editor_labelText, getFieldEditorParent());
		addField(editor);
		
		editor = new StringFieldEditor(Preferences.STATION_IDENT_ID,
			Messages.SystemPreferencePage_editor_labelText_1, getFieldEditorParent());
		addField(editor);
		
		editor = new KontaktFieldEditor(CoreHub.globalCfg, Preferences.SELFCONTACT_ID,
			Messages.SystemPreferencePage_editor_selfContact_label, getFieldEditorParent());
		addField(editor);
	}
	
	@Override
	public boolean performOk(){
		prefs.flush();
		return super.performOk();
	}
}
