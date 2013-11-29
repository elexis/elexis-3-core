/*******************************************************************************
 * Copyright (c) 2006-2010, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.Money;

/**
 * This view shows the current patient's account
 * 
 * TODO reloading the list is not yet possible
 */

public class AccountListView extends ViewPart implements IActivationListener, ISaveablePart2 {
	
	public static final String ID = "ch.elexis.views.rechnung.AccountListView"; //$NON-NLS-1$
	
	private FormToolkit tk;
	private Form form;
	private TableViewer accountListViewer;
	
	// column indices
	private static final int NAME = 0;
	private static final int FIRSTNAME = 1;
	private static final int BIRTHDATE = 2;
	private static final int SALDO = 3;
	
	private static final String[] COLUMN_TEXT = {
		Messages.AccountListView_name, // NAME //$NON-NLS-1$
		Messages.AccountListView_firstname, // FIRSTNAME //$NON-NLS-1$
		Messages.AccountListView_bithdate, // BIRTHDATE //$NON-NLS-1$
		Messages.AccountListView_balance, // SALDO //$NON-NLS-1$
	};
	
	private static final int[] COLUMN_WIDTH = {
		150, // NAME
		150, // FIRSTNAME
		100, // BIRTHDATE
		100, // SALDO
	};
	
	private DataLoader loader;
	
	public void createPartControl(Composite parent){
		loader = new DataLoader();
		
		parent.setLayout(new FillLayout());
		tk = UiDesk.getToolkit();
		form = tk.createForm(parent);
		form.getBody().setLayout(new GridLayout(1, false));
		
		// account list
		tk.createLabel(form.getBody(), Messages.AccountListView_accountList); //$NON-NLS-1$
		accountListViewer = new TableViewer(form.getBody(), SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = accountListViewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn[] tc = new TableColumn[COLUMN_TEXT.length];
		for (int i = 0; i < COLUMN_TEXT.length; i++) {
			tc[i] = new TableColumn(table, SWT.NONE);
			tc[i].setText(COLUMN_TEXT[i]);
			tc[i].setWidth(COLUMN_WIDTH[i]);
		}
		
		accountListViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement){
				if (loader.isValid()) {
					Object result = loader.getData();
					if (result instanceof Object[]) {
						return (Object[]) result;
					} else {
						// invalid data
						return new Object[0];
					}
				} else {
					loader.schedule();
					return new Object[] {
						Messages.AccountListView_loadingData
					}; //$NON-NLS-1$
				}
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
		});
		accountListViewer.setLabelProvider(new ITableLabelProvider() {
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
				if (!(element instanceof AccountListEntry)) {
					return ""; //$NON-NLS-1$
				}
				
				AccountListEntry entry = (AccountListEntry) element;
				String text = ""; //$NON-NLS-1$
				
				switch (columnIndex) {
				case NAME:
					text = entry.name;
					break;
				case FIRSTNAME:
					text = entry.vorname;
					break;
				case BIRTHDATE:
					text = entry.geburtsdatum;
					break;
				case SALDO:
					text = entry.saldo.toString();
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
		
		// viewer.setSorter(new NameSorter());
		accountListViewer.setInput(getViewSite());
		
		/*
		 * makeActions(); hookContextMenu(); hookDoubleClickAction(); contributeToActionBars();
		 */
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus(){
		accountListViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	/*
	 * ActivationListener
	 */
	
	public void activation(boolean mode){
		// nothing to do
	}
	
	public void visible(boolean mode){
		
	};
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	class DataLoader extends BackgroundJob implements BackgroundJobListener {
		Integer size = null;
		
		DataLoader(){
			super("AccountListView"); //$NON-NLS-1$
			addListener(this);
		}
		
		public IStatus execute(IProgressMonitor monitor){
			List<AccountListEntry> entries = new ArrayList<AccountListEntry>();
			
			Query<Patient> query = new Query<Patient>(Patient.class);
			query.orderBy(false, Patient.FLD_NAME, Patient.FLD_FIRSTNAME);
			List<Patient> patients = query.execute();
			if (patients == null) {
				result = new Object[0];
			} else {
				for (Patient patient : patients) {
					AccountListEntry entry = new AccountListEntry(patient);
					entries.add(entry);
				}
				
				result = entries.toArray();
			}
			return Status.OK_STATUS;
		}
		
		public int getSize(){
			// dummy size
			return 1;
		}
		
		public void jobFinished(BackgroundJob j){
			accountListViewer.refresh();
		}
	}
	
	class AccountListEntry {
		Patient patient;
		String name;
		String vorname;
		String geburtsdatum;
		Money saldo;
		
		AccountListEntry(Patient patient){
			this.patient = patient;
			
			String[] values = new String[3];
			patient.get(new String[] {
				Patient.FLD_NAME, Patient.FLD_FIRSTNAME, Patient.BIRTHDATE
			}, values);
			this.name = values[0];
			this.vorname = values[1];
			this.geburtsdatum = values[2];
			this.saldo = patient.getKontostand();
		}
	}
}