package ch.elexis.core.ui.contacts.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.contacts.controls.DiagnosesComposite;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class DiagnoseSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public DiagnoseSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new BooleanFieldEditor(DiagnosesComposite.DIAGNOSE_SETTINGS_USE_STRUCTURED,
			"Diagnosen strukturiert anzeigen", getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(DiagnosesComposite.DIAGNOSE_SETTINGS_AUTO_CREATE,
			"Strukturierte Diagnose automatisch aus Text erzeugen", getFieldEditorParent()));
		
	}

	
	@Override
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
}
