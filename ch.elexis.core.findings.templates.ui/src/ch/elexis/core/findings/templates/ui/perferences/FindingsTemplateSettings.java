package ch.elexis.core.findings.templates.ui.perferences;

import java.util.Optional;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.ui.composite.FindingsComposite;
import ch.elexis.core.findings.templates.ui.composite.FindingsDetailComposite;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class FindingsTemplateSettings extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	private FindingsComposite findingsComposite;
	
	public FindingsTemplateSettings(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected void adjustGridLayout(){
		/** ignore adjusting **/
	}
	
	@Override
	protected void createFieldEditors(){
		FindingsTemplates model =
			FindingsServiceHolder.findingsTemplateService.getFindingsTemplates("Standard Vorlagen");
		findingsComposite = new FindingsComposite(getFieldEditorParent(), model);
		FindingsDetailComposite findingsDetailComposite =
			new FindingsDetailComposite(getFieldEditorParent(), model);
		findingsComposite.setFindingsDetailComposite(findingsDetailComposite);
		
		findingsComposite.createContents();
		findingsDetailComposite.createContents();
		findingsComposite.selectFirstTreeElement();
	}
	
	@Override
	public boolean performOk(){
		Optional<FindingsTemplates> model = findingsComposite.getModel();
		FindingsServiceHolder.findingsTemplateService.saveFindingsTemplates(model);
		return super.performOk();
	}
	
}
