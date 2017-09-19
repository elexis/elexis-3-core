package ch.elexis.core.findings.templates.ui.dlg;

import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.model.Coding;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.ui.composites.CodingContentProposalProvider;

public class CodeSystemsDialog extends TitleAreaDialog {
	
	private Optional<Coding> selectedCode = Optional.empty();
	private CodingContentProposalProvider proposalProvider;
	private Text selectionTxt;
	
	public CodeSystemsDialog(Shell parentShell, Optional<Coding> selectedCode){
		super(parentShell);
		this.selectedCode = selectedCode;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("");
		setTitle("Codesystem: " + CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Code suche: ");
		
		selectionTxt = new Text(composite, SWT.BORDER);
		selectionTxt.setMessage("Coding selektieren");
		selectionTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		proposalProvider = new CodingContentProposalProvider();
		proposalProvider
			.setSelectedSystem(Optional.of(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem()));
		
		ContentProposalAdapter toAddressProposalAdapter = new ContentProposalAdapter(selectionTxt,
			new TextContentAdapter(), proposalProvider, null, null);
		toAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal){
				selectionTxt.setText(proposal.getContent());
				proposalProvider.getCodingForProposal(proposal).ifPresent(iCoding -> {
					Coding coding = ModelFactory.eINSTANCE.createCoding();
					coding.setCode(iCoding.getCode());
					coding.setDisplay(iCoding.getDisplay());
					coding.setSystem(iCoding.getSystem());
					selectedCode = Optional.of(coding);
				});
				selectionTxt.setSelection(selectionTxt.getText().length());
			}
		});

		selectedCode.ifPresent(item -> setSelection(new StructuredSelection(selectedCode.get())));
		return composite;
	}
	
	public ISelection getSelection(){
		StructuredSelection ret = new StructuredSelection();
		if (selectedCode.isPresent()) {
			ret = new StructuredSelection(selectedCode.get());
		}
		return ret;
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
		super.okPressed();
	}
	
	public Optional<Coding> getSelectedCode(){
		return selectedCode;
	}

	private void setSelection(ISelection selection){
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Coding coding = (Coding) ((IStructuredSelection) selection).getFirstElement();
			String label = "[" + coding.getCode() + "] " + coding.getDisplay();
			if (label != null) {
				selectionTxt.setText(label);
				selectionTxt.setSelection(selectionTxt.getText().length());
				selectedCode = Optional.of(coding);
			}
		} else {
			selectionTxt.setText("");
			selectedCode = Optional.empty();
		}
	}
	
}
