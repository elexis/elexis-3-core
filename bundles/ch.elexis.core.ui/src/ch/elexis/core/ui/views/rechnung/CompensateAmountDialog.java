package ch.elexis.core.ui.views.rechnung;

import java.text.MessageFormat;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.AccountTransaction.Account;

public class CompensateAmountDialog extends Dialog {
	
	private final String compensateAmountMessage;
	private Text text;
	private Text errorMessageText;
	
	private String errorMessage;
	private IInputValidator validator;
	
	private String value = "";//$NON-NLS-1$
	private Account account = null;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public CompensateAmountDialog(Shell parentShell, String openAmount){
		super(parentShell);
		
		compensateAmountMessage =
			MessageFormat.format(Messages.RechnungsBlatt_compensateAmountMessage, openAmount);
		
		validator = new IInputValidator() {
			
			@Override
			public String isValid(String newText){
				boolean valid = StringUtils.isNotBlank(newText);
				return (valid) ? null : Messages.RechnungsBlatt_missingReason;
			}
		};
	}
	
	@Override
	protected void configureShell(Shell shell){
		super.configureShell(shell);
		shell.setText(Messages.RechnungsBlatt_compensateAmountTitle);
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		Label lblInfoText = new Label(container, SWT.WRAP);
		lblInfoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblInfoText.setText(compensateAmountMessage);
		
		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addModifyListener(e -> validateInput());
		
		errorMessageText = new Text(container, SWT.READ_ONLY | SWT.WRAP);
		errorMessageText
			.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		errorMessageText.setBackground(
			errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		errorMessageText.setForeground(UiDesk.getColor(UiDesk.COL_RED));
		
		Label lblAccountSelectionText = new Label(container, SWT.NONE);
		lblAccountSelectionText.setText(Messages.CompensateAmountDialog_selectAccount);
		
		ComboViewer comboViewerAccount = new ComboViewer(container, SWT.NONE);
		Combo comboAccount = comboViewerAccount.getCombo();
		comboAccount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		setErrorMessage(errorMessage);
		
		comboViewerAccount.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerAccount.setLabelProvider(new LabelProvider() {
			public String getText(Object element){
				Account account = (Account) element;
				return account.getName() + " (" + Integer.toString(account.getNumeric()) + ")";
			};
		});
		comboViewerAccount.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				if (ss.isEmpty()) {
					account = null;
				} else {
					account = (Account) ss.getFirstElement();
				}
				
			}
		});
		
		HashMap<Integer, Account> accounts = Account.getAccounts();
		accounts.remove(new Integer(-1));
		comboViewerAccount.setInput(accounts.values());
		
		return container;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		//do this here because setting the text will set enablement on the ok
		// button
		text.setFocus();
		if (value != null) {
			text.setText(value);
			text.selectAll();
		}
	}
	
	protected void validateInput(){
		String errorMessage = null;
		if (validator != null) {
			errorMessage = validator.isValid(text.getText());
		}
		// Bug 16256: important not to treat "" (blank error) the same as null
		// (no error)
		setErrorMessage(errorMessage);
	}
	
	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
		if (errorMessageText != null && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
			// Disable the error message text control if there is no error, or
			// no error text (empty or whitespace only).  Hide it also to avoid
			// color change.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
			boolean hasError = errorMessage != null
				&& (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();
			// Access the ok button by id, in case clients have overridden button creation.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
			Control button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(errorMessage == null);
			}
		}
	}
	
	public String getValue(){
		return value;
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		if (buttonId == IDialogConstants.OK_ID) {
			value = text.getText();
		} else {
			value = null;
		}
		super.buttonPressed(buttonId);
	}
	
	public Account getAccount(){
		return account;
	}
	
}
