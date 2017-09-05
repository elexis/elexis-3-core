package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.dto.InvoiceCorrectionDTO;

public class InvoiceCorrectionWizardDialog extends WizardDialog {
	
	public InvoiceCorrectionWizardDialog(Shell shell, InvoiceCorrectionDTO invoiceCorrectionDTO){
		super(shell, new InvoiceCorrectionWizard(invoiceCorrectionDTO));
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		// TODO Auto-generated method stub
		super.createButtonsForButtonBar(parent);

		Button finish = getButton(IDialogConstants.FINISH_ID);
		finish.setText("Fertigstellen");
		finish.setVisible(true);
	    setButtonLayoutData(finish);
		
		Button back = getButton(IDialogConstants.BACK_ID);
		back.setVisible(false);
		
		Button cancel = getButton(IDialogConstants.CANCEL_ID);
		cancel.setText("Abbrechen");
		
		Button btnCreateInvoice = getButton(IDialogConstants.NEXT_ID);
		btnCreateInvoice.setText("Korrektur durchführen");
		setButtonLayoutData(btnCreateInvoice);
		
		cancel.moveBelow(null);
		finish.moveAbove(cancel);
		btnCreateInvoice.moveAbove(finish);
		back.moveAbove(btnCreateInvoice);
		
	}
	
	@Override
	protected void finishPressed(){
		super.finishPressed();
		super.close();
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		super.buttonPressed(buttonId);
		
		if (IDialogConstants.NEXT_ID == buttonId) {
			getButton(IDialogConstants.NEXT_ID).setEnabled(false);
			getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
			getButton(IDialogConstants.FINISH_ID).setVisible(true);
			
		}
	}
}