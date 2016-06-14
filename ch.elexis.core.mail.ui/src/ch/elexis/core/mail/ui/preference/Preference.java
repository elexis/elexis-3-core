package ch.elexis.core.mail.ui.preference;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.ui.client.MailClientComponent;

public class Preference extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite parentComposite;
	
	private MailAccountComposite accountComposite;
	private ComboViewer accountsViewer;
	private Button testButton;
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected Control createContents(Composite parent){
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));
		
		accountsViewer = new ComboViewer(parentComposite);
		accountsViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		accountsViewer.setContentProvider(new ArrayContentProvider());
		accountsViewer.setLabelProvider(new LabelProvider());
		updateAccountsCombo();
		accountsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				int index = accountsViewer.getCombo().getSelectionIndex();
				if (index == 0) {
					accountComposite.setAccount(new MailAccount());
					testButton.setEnabled(true);
				} else {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					String accountId = (String) selection.getFirstElement();
					if (accountId != null) {
						Optional<MailAccount> selectedAccount =
							MailClientComponent.getMailClient().getAccount(accountId);
						if (selectedAccount.isPresent()) {
							accountComposite.setAccount(selectedAccount.get());
							testButton.setEnabled(true);
						}
					}
				}
			}
		});
		
		ToolBar accountsTool = new ToolBar(parentComposite, SWT.NONE);
		
		accountComposite = new MailAccountComposite(parentComposite, SWT.NONE);
		accountComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		testButton = new Button(parentComposite, SWT.PUSH);
		testButton.setText("Test");
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (MailClientComponent.getMailClient() != null
					&& accountComposite.getAccount() != null) {
					if (MailClientComponent.getMailClient()
						.testAccount(accountComposite.getAccount())) {
						MessageDialog.openInformation(getShell(), "Test", "Test erfolgreich.");
					} else {
						String message = MailClientComponent.getLastErrorMessage();
						MessageDialog.openError(getShell(), "Fehler", message);
					}
				}
			}
		});
		
		ToolBarManager accountsToolMgr = new ToolBarManager(accountsTool);
		accountsToolMgr.add(new SaveAccountAction(accountComposite, this));
		accountsToolMgr.add(new RemoveAccountAction(accountComposite, this));
		accountsToolMgr.update(true);

		return parentComposite;
	}
	
	@Override
	public boolean performOk(){
		MailAccount account = accountComposite.getAccount();
		if (account != null) {
			MailClientComponent.getMailClient().saveAccount(account);
		}
		return super.performOk();
	}
	
	protected void updateAccountsCombo(){
		if (MailClientComponent.getMailClient() != null) {
			accountsViewer.getControl().setEnabled(true);
			List<String> accountsInput = MailClientComponent.getMailClient().getAccounts();
			accountsInput.add(0, "Neu erstellen");
			accountsViewer.setInput(accountsInput);
			if (accountComposite != null) {
				MailAccount account = accountComposite.getAccount();
				if (account != null) {
					accountsViewer.setSelection(new StructuredSelection(account.getId()));
				}
			}
		} else {
			accountsViewer.getControl().setEnabled(false);
			testButton.setEnabled(false);
		}
	}
}
