package ch.elexis.core.findings.templates.ui.dlg;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.templates.model.Type;
import ch.elexis.core.findings.templates.ui.composite.FindingsDetailComposite;

public class FindingsDialog extends TitleAreaDialog {
	
	private final FindingsTemplates model;
	private FindingsDetailComposite findingsDetailComposite;

	public FindingsDialog(Shell parentShell, FindingsTemplates model){
		super(parentShell);
		this.model = model;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("Neue Befundvorlage anlegen");
		setTitle("Befund Vorlage");
		
		findingsDetailComposite = new FindingsDetailComposite(parent, model);
		findingsDetailComposite.createContents();
		FindingsTemplate findingsTemplate = ModelFactory.eINSTANCE.createFindingsTemplate();
		findingsTemplate.setTitle("Neue Vorlage");
		findingsTemplate.setType(Type.OBSERVATION);
		findingsTemplate.setInputData(ModelFactory.eINSTANCE.createInputDataNumeric());
		findingsDetailComposite.setSelection(model, findingsTemplate);
		
		return findingsDetailComposite;
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		super.buttonPressed(buttonId);
	}
	
	@Override
	protected void okPressed(){
		
		FindingsTemplate findingsTemplate = findingsDetailComposite.getSelection();
		if (findingsTemplate != null) {
			model.getFindingsTemplates().add(findingsTemplate);
			super.okPressed();
		}
	}
}
