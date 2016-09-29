package ch.elexis.core.ui.preferences;

import java.util.HashMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.AccountTransaction.Account;

public class AccountsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private TableViewer viewer;
	private ToolBarManager toolbarmgr;
	
	@Override
	public void init(IWorkbench workbench){
		setTitle("Konti für Buchungen");
		setDescription(
			"Die hier verwalteten Konti, können bei Buchungen vergeben werden,"
				+ " und sind dann bei den Auswertungen (Zahlungsjournal) ersichtlich.");
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		
		toolbarmgr = new ToolBarManager();
		toolbarmgr.add(new AddAccountAction());
		toolbarmgr.add(new RemoveAccountAction());
		toolbarmgr.add(new InitDefaultsAction());
		ToolBar toolbar = toolbarmgr.createControl(ret);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));

		Composite tableComposite = new Composite(ret, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new FillLayout());
		viewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().setHeaderVisible(true);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
		tableComposite.setLayout(columnLayout);
		
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Nummer");
		columnLayout.setColumnData(column.getColumn(), new ColumnWeightData(30, 60));
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Account) {
					return Integer.toString(((Account) element).getNumeric());
				}
				return super.getText(element);
			}
		});
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Name");
		columnLayout.setColumnData(column.getColumn(), new ColumnWeightData(70, 140));
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Account) {
					return ((Account) element).getName();
				}
				return super.getText(element);
			}
		});
		
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				if(e1 instanceof Account && e2 instanceof Account) {
					Integer e1Num = ((Account) e1).getNumeric();
					Integer e2Num = ((Account) e2).getNumeric();
					return e1Num.compareTo(e2Num);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		updateViewer();
		
		return ret;
	}
	
	private void updateViewer(){
		HashMap<Integer, Account> accounts = Account.getAccounts();
		accounts.remove(new Integer(-1));
		viewer.setInput(accounts.values());
	}
	
	private class AddAccountAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public String getToolTipText(){
			return "Account hinzufügen";
		}
		
		@Override
		public void run(){
			Account newAccount = new Account(-1, "?");
			EditAccountDialog dialog = new EditAccountDialog(getShell(), newAccount);
			
			if (dialog.open() == Window.OK) {
				Account.addAccount(newAccount);
				updateViewer();
			}
		}
	}
	
	private class RemoveAccountAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public String getToolTipText(){
			return "Account entfernen";
		}
		
		@Override
		public void run(){
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()) {
				Account account = (Account) selection.getFirstElement();
				Account.removeAccount(account);
				updateViewer();
			}
		}
	}
	
	private class InitDefaultsAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DATA.getImageDescriptor();
		}
		
		@Override
		public String getToolTipText(){
			return "Defaults Accounts initialisieren";
		}
		
		@Override
		public void run(){
			Account.initDefaults();
			updateViewer();
		}
	}
	
	private class EditAccountDialog extends TitleAreaDialog {
		
		private Account account;
		private Text numericText;
		private Text nameText;
		
		public EditAccountDialog(Shell parentShell, Account account){
			super(parentShell);
			this.account = account;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout(2, false));
			
			setTitle("Konto editieren"); //$NON-NLS-1$
			Label lbl = new Label(ret, SWT.NONE);
			lbl.setText("Nummer");
			numericText = new Text(ret, SWT.BORDER);
			GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
			gd.widthHint = 100;
			numericText.setLayoutData(gd);
			if (account != null) {
				numericText.setText(Integer.toString(account.getNumeric()));
			}
			
			lbl = new Label(ret, SWT.NONE);
			lbl.setText("Name");
			nameText = new Text(ret, SWT.BORDER);
			nameText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			if (account != null) {
				nameText.setText(account.getName());
			}
			
			return ret;
		}
		
		@Override
		protected void okPressed(){
			String name = nameText.getText();
			String numeric = numericText.getText();
			if (name == null || name.isEmpty()) {
				setErrorMessage("Bitte Name eingeben.");
				return;
			}
			if (numeric == null || numeric.isEmpty()) {
				setErrorMessage("Bitte Nummer eingeben.");
				return;
			}
			Integer numericInt = -1;
			try {
				numericInt = Integer.parseInt(numeric);
			} catch (NumberFormatException nfe) {
				setErrorMessage("Bitte numerischen Wert als Nummer eingeben.");
				return;
			}
			
			HashMap<Integer, Account> accounts = Account.getAccounts();
			if (accounts.containsKey(numericInt)) {
				setErrorMessage("Der Wert " + numericInt + " ist bereits vergeben.");
				return;
			}
			account.setNumeric(numericInt);
			account.setName(name);
			super.okPressed();
		}
	}
}
