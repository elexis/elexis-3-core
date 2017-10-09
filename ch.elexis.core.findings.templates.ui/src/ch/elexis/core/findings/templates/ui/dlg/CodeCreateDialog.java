package ch.elexis.core.findings.templates.ui.dlg;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;

public class CodeCreateDialog extends TitleAreaDialog {
	
	private Text txtDisplay;
	private Text txtCode;
	
	public CodeCreateDialog(Shell parentShell){
		super(parentShell);
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("Neuen Code anlegen");
		setTitle("Codesystem: " + CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label lblCode = new Label(composite, SWT.NONE);
		lblCode.setText("Code");
		
		txtCode = new Text(composite, SWT.BORDER);
		txtCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label lblDisplay = new Label(composite, SWT.NONE);
		lblDisplay.setText("Display");
		
		txtDisplay = new Text(composite, SWT.BORDER);
		txtDisplay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return composite;
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
		String code = txtCode.getText();
		String display = txtDisplay.getText();
		
		if (StringUtils.isEmpty(code) || StringUtils.isEmpty(display)) {
			MessageDialog.openError(getShell(), "Error", "Bitte korriegen Sie Ihre Eingaben.");
		}
		else {
			FindingsServiceHolder.codingService.addLocalCoding(new ICoding() {
				
				@Override
				public String getSystem(){
					return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
				}
				
				@Override
				public String getDisplay(){
					return display;
				}
				
				@Override
				public String getCode(){
					return code;
				}
			});
			super.okPressed();
		}
		

	}
}
