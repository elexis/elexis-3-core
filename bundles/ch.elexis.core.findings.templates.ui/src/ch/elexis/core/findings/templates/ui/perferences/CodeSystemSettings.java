package ch.elexis.core.findings.templates.ui.perferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.templates.ui.composite.CodesSystemsComposite;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class CodeSystemSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public CodeSystemSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	protected void adjustGridLayout(){
		/** ignore adjusting **/
	}
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected void createFieldEditors(){
		CodesSystemsComposite codesSystemsComposite =
			new CodesSystemsComposite(getFieldEditorParent());
		codesSystemsComposite.createContens();
	}
}
