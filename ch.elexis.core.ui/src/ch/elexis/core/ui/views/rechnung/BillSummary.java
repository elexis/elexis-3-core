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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.rgw.tools.Money;

/**
 * This view shows the current patient's account
 */

public class BillSummary extends ViewPart implements IActivationListener, ElexisEventListener,
		ISaveablePart2 {
	
	public static final String ID = "ch.elexis.views.rechnung.BillSummary"; //$NON-NLS-1$
	
	// command from org.eclipse.ui
	private static final String COMMAND_COPY = "org.eclipse.ui.edit.copy"; //$NON-NLS-1$
	
	private FormToolkit tk;
	private Form form;
	private Label totalLabel;
	private Label paidLabel;
	private Label openLabel;
	private TableViewer billsViewer;
	
	private Patient actPatient;
	
	private Action exportToClipboardAction;
	
	// column indices
	private static final int NUMBER = 0;
	private static final int DATE = 1;
	private static final int AMOUNT = 2;
	private static final int AMOUNT_DUE = 3;
	private static final int STATUS = 4;
	private static final int GARANT = 5;
	private static boolean bReverse = true;
	
	private static final String[] COLUMN_TEXT = {
		Messages.BillSummary_number, // NUMBER //$NON-NLS-1$
		Messages.BillSummary_date, // DATE //$NON-NLS-1$
		Messages.BillSummary_amount, // AMOUNT //$NON-NLS-1$
		Messages.BillSummary_open, // AMOUNT_DUE //$NON-NLS-1$
		Messages.BillSummary_state, // STATUS //$NON-NLS-1$
		Messages.BillSummary_receiver, // GARANT //$NON-NLS-1$
	};
	
	private static final int[] COLUMN_WIDTH = {
		80, // NUMBER
		80, // DATE
		80, // AMOUNT
		80, // AMOUNT_DUE
		80, // STATUS
		80, // GARANT
	};

	class SortListener extends SelectionAdapter {
	
		@Override
		public void widgetSelected(SelectionEvent e){
			bReverse = !bReverse;
			billsViewer.refresh();
		}
	}
	
	private List<Rechnung> getRechnungen(Patient patient){
		List<Rechnung> rechnungen = patient.getRechnungen();
		Collections.sort(rechnungen, new Comparator<Rechnung>() {
			// compare on bill number
			public int compare(Rechnung r1, Rechnung r2){
				// both null, consider as equal
				if (r1 == null && r2 == null) {
					return 0;
				}
				
				// r1 is null, r2 not. sort r2 before r1
				if (r1 == null) {
					return bReverse ? -1 : 1;
				}
				
				// r2 is null, r1 not. sort r1 before r2
				if (r2 == null) {
					return bReverse ? 1 : -1;
				}
				
				try {
					Integer number1 = new Integer(r1.getNr());
					Integer number2 = new Integer(r2.getNr());
					return bReverse ? number2.compareTo(number1) : number1.compareTo(number2);
				} catch (NumberFormatException ex) {
					// error, consider equal
					return 0;
				}
			}
			
			// compare on id
			public boolean equals(Object obj){
				return (this == obj);
			}
		});
		
		return rechnungen;
	}
	
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		tk = UiDesk.getToolkit();
		form = tk.createForm(parent);
		form.getBody().setLayout(new GridLayout(1, false));
		
		// general infos
		Composite generalArea = tk.createComposite(form.getBody());
		generalArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		generalArea.setLayout(new GridLayout(2, false));
		
		tk.createLabel(generalArea, Messages.BillSummary_total); //$NON-NLS-1$
		totalLabel = tk.createLabel(generalArea, ""); //$NON-NLS-1$
		totalLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		tk.createLabel(generalArea, Messages.BillSummary_paid); //$NON-NLS-1$
		paidLabel = tk.createLabel(generalArea, ""); //$NON-NLS-1$
		paidLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		tk.createLabel(generalArea, Messages.BillSummary_open2); //$NON-NLS-1$
		openLabel = tk.createLabel(generalArea, ""); //$NON-NLS-1$
		openLabel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		// bills
		billsViewer = new TableViewer(form.getBody(), SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = billsViewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tk.adapt(table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		// columns
		TableColumn[] tc = new TableColumn[COLUMN_TEXT.length];
		for (int i = 0; i < COLUMN_TEXT.length; i++) {
			tc[i] = new TableColumn(table, SWT.NONE);
			tc[i].setText(COLUMN_TEXT[i]);
			tc[i].setWidth(COLUMN_WIDTH[i]);
		}
		
		// Allow sorting on date ascending and descending
		SortListener sortListener = new SortListener();
		tc[1].addSelectionListener(sortListener);
		
		billsViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement){
				if (actPatient == null) {
					return new Object[] {
						Messages.BillSummary_NoPatientSelected
					};
				}
				
				return getRechnungen(actPatient).toArray();
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
		});
		billsViewer.setLabelProvider(new ITableLabelProvider() {
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
				if (!(element instanceof Rechnung)) {
					return ""; //$NON-NLS-1$
				}
				
				Rechnung rechnung = (Rechnung) element;
				String text = ""; //$NON-NLS-1$
				
				switch (columnIndex) {
				case NUMBER:
					text = rechnung.get(Rechnung.BILL_NUMBER);
					break;
				case DATE:
					text = rechnung.get(Rechnung.BILL_DATE);
					break;
				case AMOUNT:
					text = rechnung.getBetrag().toString();
					break;
				case AMOUNT_DUE:
					text = rechnung.getOffenerBetrag().toString();
					break;
				case STATUS:
					text = RnStatus.getStatusText(rechnung.getStatus());
					break;
				case GARANT:
					text = rechnung.getFall().getGarant().getLabel();
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
		
		billsViewer.setInput(getViewSite());
		
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createMenu(exportToClipboardAction);
		GlobalEventDispatcher.addActivationListener(this, this);
		billsViewer.addSelectionChangedListener(GlobalEventDispatcher.getInstance()
			.getDefaultListener());
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus(){
		billsViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		billsViewer.removeSelectionChangedListener(GlobalEventDispatcher.getInstance()
			.getDefaultListener());
		super.dispose();
	}
	
	private void setPatient(Patient patient){
		actPatient = patient;
		
		String title = ""; //$NON-NLS-1$
		if (actPatient != null) {
			title = actPatient.getLabel();
		} else {
			title = Messages.BillSummary_NoPatientSelected; //$NON-NLS-1$
		}
		form.setText(title);
		
		setGeneralText();
		billsViewer.refresh();
		
		form.layout();
	}
	
	// maybe called from foreign thread
	private void setGeneralText(){
		// check wheter the labels are valid, since we may be called
		// from a different thread
		if (totalLabel.isDisposed() || paidLabel.isDisposed() || openLabel.isDisposed()) {
			return;
		}
		
		String totalText = ""; //$NON-NLS-1$
		String paidText = ""; //$NON-NLS-1$
		String openText = ""; //$NON-NLS-1$
		
		if (actPatient != null) {
			Money total = new Money(0);
			Money paid = new Money(0);
			
			List<Rechnung> rechnungen = actPatient.getRechnungen();
			for (Rechnung rechnung : rechnungen) {
				// don't consider canceled bills
				if (rechnung.getStatus() != RnStatus.STORNIERT) {
					total.addMoney(rechnung.getBetrag());
					for (Zahlung zahlung : rechnung.getZahlungen()) {
						paid.addMoney(zahlung.getBetrag());
					}
				}
			}
			
			Money open = new Money(total);
			open.subtractMoney(paid);
			
			totalText = total.toString();
			paidText = paid.toString();
			openText = open.toString();
		}
		
		totalLabel.setText(totalText);
		paidLabel.setText(paidText);
		openLabel.setText(openText);
	}
	
	/*
	 * ActivationListener
	 */
	
	public void activation(boolean mode){
		// nothing to do
	}
	
	public void visible(boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(this);
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			setPatient(patient);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(this);
			setPatient(null);
		}
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
	
	/*
	 * class AccountEntry { TimeTool date; Money amount; String remarks;
	 * 
	 * AccountEntry(TimeTool date, Money amount, String remarks) { this.date = date; this.amount =
	 * amount; this.remarks = remarks;
	 * 
	 * if (remarks == null) { remarks = ""; } } }
	 */
	
	private void makeActions(){
		exportToClipboardAction = new Action(Messages.BillSummary_exportToClipboard) { //$NON-NLS-1$
				{
					setToolTipText(Messages.BillSummary_SummaryToClipboard); //$NON-NLS-1$
				}
				
				public void run(){
					exportToClipboard();
				}
			};
		exportToClipboardAction.setActionDefinitionId(COMMAND_COPY);
		GlobalActions.registerActionHandler(this, exportToClipboardAction);
	}
	
	private void exportToClipboard(){
		String clipboardText = ""; //$NON-NLS-1$
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		
		if (actPatient != null) {
			List<Rechnung> rechnungen = getRechnungen(actPatient);
			StringBuffer sbTable = new StringBuffer();
			StringBuffer sbHeader = new StringBuffer();
			
			sbHeader.append(COLUMN_TEXT[NUMBER]);
			sbHeader.append("\t"); //$NON-NLS-1$
			sbHeader.append(COLUMN_TEXT[DATE]);
			sbHeader.append("\t"); //$NON-NLS-1$
			sbHeader.append(COLUMN_TEXT[AMOUNT]);
			sbHeader.append("\t"); //$NON-NLS-1$
			sbHeader.append(COLUMN_TEXT[AMOUNT_DUE]);
			sbHeader.append("\t"); //$NON-NLS-1$
			sbHeader.append(COLUMN_TEXT[STATUS]);
			sbHeader.append("\t"); //$NON-NLS-1$
			sbHeader.append(COLUMN_TEXT[GARANT]);
			sbHeader.append(lineSeparator);
			sbTable.append(sbHeader);
			
			for (Rechnung rechnung : rechnungen) {
				StringBuffer sbLine = new StringBuffer();
				sbLine.append(rechnung.get(Rechnung.BILL_NUMBER));
				sbLine.append("\t"); //$NON-NLS-1$
				sbLine.append(rechnung.get(Rechnung.BILL_DATE));
				sbLine.append("\t"); //$NON-NLS-1$
				sbLine.append(rechnung.getBetrag().toString());
				sbLine.append("\t"); //$NON-NLS-1$
				sbLine.append(rechnung.getOffenerBetrag().toString());
				sbLine.append("\t"); //$NON-NLS-1$
				sbLine.append(RnStatus.getStatusText(rechnung.getStatus()));
				sbLine.append("\t"); //$NON-NLS-1$
				sbLine.append(rechnung.getFall().getGarant().getLabel());
				sbLine.append(lineSeparator);
				sbTable.append(sbLine);
			}
			
			clipboardText = sbTable.toString();
		} else {
			clipboardText = Messages.BillSummary_noBillsAvailable; //$NON-NLS-1$
		}
		
		Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] transfers = new Transfer[] {
			textTransfer
		};
		Object[] data = new Object[] {
			clipboardText
		};
		clipboard.setContents(data, transfers);
		clipboard.dispose();
	}
	
	public void catchElexisEvent(final ElexisEvent ev){
		UiDesk.asyncExec(new Runnable() {
			public void run(){
				if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
					setPatient((Patient) ev.getObject());
				} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
					setPatient(null);
				}
			}
		});
	}
	
	private final ElexisEvent eetmpl = new ElexisEvent(null, Patient.class,
		ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED);
	
	public ElexisEvent getElexisEventFilter(){
		return eetmpl;
	}
	
}