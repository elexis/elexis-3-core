/*******************************************************************************
 * Copyright (c) 2006-2009, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    Gerry Weirich - adapted to use the new AccountTransaction-class
 *    				  actions added
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.AddBuchungDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.AccountTransaction.Account;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

/**
 * This view shows the current patient's account
 */

public class AccountView extends ViewPart implements IActivationListener {
	
	public static final String ID = "ch.elexis.views.rechnung.AccountView"; //$NON-NLS-1$
	
	private static final String ACCOUNT_EXCESS_JOB_NAME = Messages.AccountView_calculateBalance; //$NON-NLS-1$
	private BackgroundJob accountExcessJob;
	
	private FormToolkit tk;
	private Form form;
	private Label balanceLabel;
	private Label excessLabel;
	private TableViewer accountViewer;
	
	private Patient actPatient;
	
	private Action addPaymentAction, removePaymentAction;
	private int sortColumn;
	private boolean sortReverse;
	
	// column indices
	private static final int DATE = 0;
	private static final int AMOUNT = 1;
	private static final int BILL = 2;
	private static final int REMARKS = 3;
	private static final int ACCOUNT = 4;
	
	private static final String[] COLUMN_TEXT = {
		Messages.AccountView_date, // DATE
		Messages.AccountView_amount, // AMOUNT
		Messages.AccountView_bill, // BILL
		Messages.AccountView_remarks, // REMARKS
		Messages.AccountView_account, // ACCOUNT
	};
	
	private static final int[] COLUMN_WIDTH = {
		80, // DATE
		80, // AMOUNT
		80, // BILL
		160, // REMARKS
		80 // ACCOUNT
	};
	private ElexisEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		public void runInUi(ElexisEvent ev){
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				Patient selectedPatient = (Patient) ev.getObject();
				setPatient(selectedPatient);
			} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
				setPatient(null);
			}
			
		}
	};
	
	private ElexisEventListenerImpl eeli_at =
		new ElexisUiEventListenerImpl(AccountTransaction.class) {
			
			public void runInUi(ElexisEvent ev){
				removePaymentAction.setEnabled(ev.getType() == ElexisEvent.EVENT_SELECTED);
			}
		};
	
	public void createPartControl(Composite parent){
		initializeJobs();
		
		parent.setLayout(new FillLayout());
		tk = UiDesk.getToolkit();
		form = tk.createForm(parent);
		form.getBody().setLayout(new GridLayout(1, false));
		
		// account infos
		Composite accountArea = tk.createComposite(form.getBody());
		accountArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		accountArea.setLayout(new GridLayout(3, false));
		tk.createLabel(accountArea, Messages.AccountView_account); //$NON-NLS-1$
		tk.createLabel(accountArea, Messages.AccountView_accountAmount); //$NON-NLS-1$
		balanceLabel = tk.createLabel(accountArea, ""); //$NON-NLS-1$
		balanceLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(accountArea, ""); // dummy //$NON-NLS-1$
		tk.createLabel(accountArea, Messages.AccountView_goodFromBills); //$NON-NLS-1$
		excessLabel = tk.createLabel(accountArea, ""); //$NON-NLS-1$
		excessLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		// account entries
		accountViewer = new TableViewer(form.getBody(), SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = accountViewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tk.adapt(table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		SelectionAdapter sortListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TableColumn col = (TableColumn) e.getSource();
				Integer colNo = (Integer) col.getData();
				if (colNo == sortColumn) {
					sortReverse = !sortReverse;
				} else {
					sortReverse = false;
					sortColumn = colNo;
				}
				accountViewer.getTable().setSortDirection(sortReverse ? SWT.DOWN : SWT.UP);
				accountViewer.getTable().setSortColumn(col);
				accountViewer.refresh();
			}
		};
		
		// columns
		TableColumn[] tc = new TableColumn[COLUMN_TEXT.length];
		for (int i = 0; i < COLUMN_TEXT.length; i++) {
			tc[i] = new TableColumn(table, SWT.NONE);
			tc[i].setText(COLUMN_TEXT[i]);
			tc[i].setWidth(COLUMN_WIDTH[i]);
			tc[i].setData(new Integer(i));
			tc[i].addSelectionListener(sortListener);
		}
		
		accountViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement){
				if (actPatient == null) {
					return new Object[] {
						Messages.AccountView_NoPatientSelected
					};
				}
				Query<AccountTransaction> qa =
					new Query<AccountTransaction>(AccountTransaction.class);
				qa.add(AccountTransaction.FLD_PATIENT_ID, Query.EQUALS, actPatient.getId());
				qa.orderBy(true, new String[] {
					AccountTransaction.FLD_DATE
				});
				return qa.execute().toArray();
				
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
		});
		accountViewer.setLabelProvider(new ITableLabelProvider() {
			public void addListener(ILabelProviderListener listener){
				// nothing to do
			}
			
			public void removeListener(ILabelProviderListener listener){
				// nothing to do
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public String getColumnText(Object element, int columnIndex){
				if (!(element instanceof AccountTransaction)) {
					return "";
				}
				
				AccountTransaction entry = (AccountTransaction) element;
				String text = "";
				
				Account account = null;
				switch (columnIndex) {
				case DATE:
					text = entry.get("Datum");
					break;
				case AMOUNT:
					text = entry.getAmount().getAmountAsString();
					break;
				case BILL:
					Rechnung rechnung = entry.getRechnung();
					if (rechnung != null && rechnung.exists()) {
						text = rechnung.getNr();
					} else {
						text = ""; //$NON-NLS-1$
					}
					break;
				case REMARKS:
					text = entry.getRemark();
					break;
				case ACCOUNT:
					account = entry.getAccount();
					if (account != null && account != Account.UNKNOWN) {
						text = account.getName();
					}
					break;
				}
				
				return text;
			}
			
			public Image getColumnImage(Object element, int columnIndex){
				return null;
			}
			
			public boolean isLabelProperty(Object element, String property){
				return false;
			}
		});
		
		accountViewer.setSorter(new AccountTransactionSorter());
		// viewer.setSorter(new NameSorter());
		accountViewer.setInput(getViewSite());
		
		/*
		 * makeActions(); hookContextMenu(); hookDoubleClickAction(); contributeToActionBars();
		 */
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(addPaymentAction /*
											 * do not use yet ,removePaymentAction
											 */);
		removePaymentAction.setEnabled(false);
		GlobalEventDispatcher.addActivationListener(this, this);
		accountViewer
			.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
		
		if (sortColumn == DATE) {
			sortReverse = true;
		}
		
	}
	
	private void initializeJobs(){
		accountExcessJob = new AccountExcessJob(ACCOUNT_EXCESS_JOB_NAME);
		accountExcessJob.addListener(new BackgroundJobListener() {
			public void jobFinished(BackgroundJob j){
				setKontoText();
			}
		});
		accountExcessJob.schedule();
	}
	
	private void finishJobs(){
		accountExcessJob.cancel();
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus(){
		accountViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose(){
		finishJobs();
		GlobalEventDispatcher.removeActivationListener(this, this);
		accountViewer.removeSelectionChangedListener(
			GlobalEventDispatcher.getInstance().getDefaultListener());
		super.dispose();
	}
	
	private void setPatient(Patient patient){
		actPatient = patient;
		
		// start calculating account excess
		accountExcessJob.invalidate();
		accountExcessJob.schedule();
		
		String title = ""; //$NON-NLS-1$
		if (actPatient != null) {
			title = actPatient.getLabel();
		} else {
			title = Messages.AccountView_NoPatientSelected; //$NON-NLS-1$
		}
		form.setText(title);
		
		setKontoText();
		accountViewer.refresh();
		
		form.layout();
	}
	
	// maybe called from foreign thread
	private void setKontoText(){
		// check wheter the labels are valid, since we may be called
		// from a different thread
		if (balanceLabel.isDisposed() || excessLabel.isDisposed()) {
			return;
		}
		
		String balanceText = ""; //$NON-NLS-1$
		String excessText = "..."; //$NON-NLS-1$
		
		if (actPatient != null) {
			balanceText = actPatient.getKontostand().getAmountAsString();
			
			if (accountExcessJob.isValid()) {
				Object jobData = accountExcessJob.getData();
				if (jobData instanceof Money) {
					Money accountExcess = (Money) jobData;
					excessText = accountExcess.getAmountAsString();
				}
			}
		}
		
		balanceLabel.setText(balanceText);
		excessLabel.setText(excessText);
	}
	
	/*
	 * SelectionListener methods
	 */
	
	/*
	 * ActivationListener
	 */
	
	public void activation(boolean mode){
		// nothing to do
	}
	
	public void visible(boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_at, eeli_pat);
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			setPatient(patient);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_at, eeli_pat);
			setPatient(null);
		}
	};
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
	/*
	 * class AccountEntry { TimeTool date; Money amount; String remarks;
	 * 
	 * AccountEntry(TimeTool date, Money amount, String remarks) { this.date = date; this.amount =
	 * amount; this.remarks = remarks;
	 * 
	 * if (remarks == null) { remarks = ""; } } }
	 */
	
	private void makeActions(){
		addPaymentAction = new Action(Messages.AccountView_addBookingCaption) { //$NON-NLS-1$
			{
				setToolTipText(Messages.AccountView_addBookingBody); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (new AddBuchungDialog(getViewSite().getShell(), actPatient)
					.open() == Dialog.OK) {
					setPatient(actPatient);
				}
			}
		};
		removePaymentAction = new Action(Messages.AccountView_deleteBookingAction) { //$NON-NLS-1$
			{
				setToolTipText(Messages.AccountView_deleteBookingTooltip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
			}
			
			@Override
			public void run(){
				AccountTransaction at = (AccountTransaction) ElexisEventDispatcher
					.getSelected(AccountTransaction.class);
				if (at != null) {
					if (SWTHelper.askYesNo(Messages.AccountView_deleteBookingConfirmCaption, //$NON-NLS-1$
						Messages.AccountView_deleteBookingConfirmBody)) { //$NON-NLS-1$
						at.delete();
						setPatient(actPatient);
					}
				}
			}
			
		};
	}
	
	class AccountExcessJob extends BackgroundJob {
		public AccountExcessJob(String name){
			super(name);
		}
		
		public IStatus execute(IProgressMonitor monitor){
			if (AccountView.this.actPatient != null) {
				result = actPatient.getAccountExcess();
			} else {
				result = null;
			}
			
			// return new Status(IStatus.OK, Hub.PLUGIN_ID, IStatus.OK,
			// "Daten geladen", null);
			return Status.OK_STATUS;
		}
		
		public int getSize(){
			return 1;
		}
	}
	
	class AccountTransactionSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			
			if ((e1 instanceof AccountTransaction) && (e2 instanceof AccountTransaction)) {
				AccountTransaction accountTransaction1 = (AccountTransaction) e1;
				AccountTransaction accountTransaction2 = (AccountTransaction) e2;
				int retVal = 0;
				switch (sortColumn) {
				case DATE:
					retVal = ObjectUtils.compare(new TimeTool(accountTransaction1.getDate()),
						new TimeTool(accountTransaction2.getDate()));
					break;
				case AMOUNT:
					retVal = ObjectUtils.compare(accountTransaction1.getAmount(),
						accountTransaction2.getAmount());
					break;
				case BILL:
					Rechnung rechnung1 = accountTransaction1.getRechnung();
					Rechnung rechnung2 = accountTransaction2.getRechnung();
					if (rechnung1 == null)
						retVal = -1;
					else if (rechnung2 == null)
						retVal = 1;
					else
						retVal = ObjectUtils.compare(NumberUtils.toInt(rechnung1.getNr()),
							NumberUtils.toInt(rechnung2.getNr()));
					break;
				case REMARKS:
					retVal = ObjectUtils.compare(accountTransaction1.getRemark(),
						accountTransaction2.getRemark());
					break;
				case ACCOUNT:
					retVal = ObjectUtils.compare(accountTransaction1.getAccount().getName(),
						accountTransaction2.getAccount().getName());
					break;
				}
				return sortReverse ? retVal * -1 : retVal;
			}
			return 0;
		}
	}
}