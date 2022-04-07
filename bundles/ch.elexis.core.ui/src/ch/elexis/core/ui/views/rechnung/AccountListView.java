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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

/**
 * This view shows the current patient's account
 * 
 * TODO reloading the list is not yet possible
 */

public class AccountListView extends ViewPart implements IActivationListener {
	
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
	
	private AccountListEntryComparator comparator;
	
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
			final int columnIndex = i;
			tc[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					comparator.setColumn(columnIndex);
					accountListViewer.refresh();
				}
			});
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
		comparator = new AccountListEntryComparator();
		accountListViewer.setComparator(comparator);
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
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
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
	
	private class AccountListEntryComparator extends ViewerComparator {
		
		private int propertyIndex;
		
		private int direction = 1;
		
		public void setColumn(int column){
			if (column == propertyIndex) {
				direction *= -1;
			}
			this.propertyIndex = column;
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			AccountListEntry a1 = (AccountListEntry) e1;
			AccountListEntry a2 = (AccountListEntry) e2;
			
			switch (propertyIndex) {
			case NAME:
				return a1.name.compareTo(a2.name) * direction;
			case FIRSTNAME:
				return a1.vorname.compareTo(a2.vorname) * direction;
			case BIRTHDATE:
				return new TimeTool(a1.geburtsdatum).compareTo(new TimeTool(a2.geburtsdatum))
					* direction;
			case SALDO:
				return a1.saldo.compareTo(a2.saldo) * direction;
			}
			return super.compare(viewer, e1, e2);
		}
	}
}