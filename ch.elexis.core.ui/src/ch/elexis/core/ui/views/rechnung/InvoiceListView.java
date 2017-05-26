/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceActions;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListBottomComposite;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider.InvoiceEntry;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListHeaderComposite;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.rgw.io.Settings;
import ch.rgw.tools.Money;

public class InvoiceListView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.rechnung.InvoiceListView"; //$NON-NLS-1$
	
	private Settings rnStellerSettings;
	private TableViewer tableViewerInvoiceList;
	private InvoiceListHeaderComposite invoiceListHeaderComposite;
	private InvoiceListBottomComposite invoiceListBottomComposite;
	private InvoiceListContentProvider invoiceListContentProvider;
	
	/**
	 * @param rnStellerSettings
	 * @wbp.parser.constructor
	 * @wbp.eval.method.parameter rnStellerSettings new ch.rgw.io.InMemorySettings()
	 */
	InvoiceListView(Settings rnStellerSettings){
		this.rnStellerSettings = rnStellerSettings;
	}
	
	public InvoiceListView(){
		Mandant currMandant = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		rnStellerSettings = CoreHub.getUserSetting(currMandant.getRechnungssteller());
	}
	
	private Action reloadViewAction = new Action(Messages.RnActions_reloadAction) { //$NON-NLS-1$
		{
			setToolTipText(Messages.RnActions_reloadTooltip); //$NON-NLS-1$
			setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
		}
		
		@Override
		public void run(){
			refresh();
		}
	};
	
	public void refresh() {
		if (invoiceListContentProvider != null) {
			invoiceListContentProvider.reload();
		}
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(1, false));
		
		invoiceListHeaderComposite = new InvoiceListHeaderComposite(parent, SWT.NONE, this);
		
		Composite compositeInvoiceList = new Composite(parent, SWT.NONE);
		compositeInvoiceList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_compositeInvoiceList = new TableColumnLayout();
		compositeInvoiceList.setLayout(tcl_compositeInvoiceList);
		
		tableViewerInvoiceList =
			new TableViewer(compositeInvoiceList, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		tableViewerInvoiceList.addSelectionChangedListener(selection -> {
			StructuredSelection ss = (StructuredSelection) selection.getSelection();
			if (!ss.isEmpty()) {
				InvoiceEntry firstElement = (InvoiceEntry) ss.getFirstElement();
				Rechnung load = Rechnung.load(firstElement.getInvoiceId());
				ElexisEventDispatcher.fireSelectionEvent(load);
			}
		});
		Table tableInvoiceList = tableViewerInvoiceList.getTable();
		tableInvoiceList.setHeaderVisible(true);
		tableInvoiceList.setLinesVisible(false);
		
		InvoiceActions invoiceActions = new InvoiceActions(tableViewerInvoiceList);
		ViewMenus viewMenu = new ViewMenus(getViewSite());
		viewMenu.createToolbar(reloadViewAction, invoiceActions.rnExportAction, invoiceActions.printListeAction, invoiceActions.addAccountExcessAction);
		
		tableViewerInvoiceList.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e){
				if (e.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		
		TableViewerColumn tvcInvoiceNo = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnInvoiceNo = tvcInvoiceNo.getColumn();
		tblclmnInvoiceNo.setData(Rechnung.BILL_NUMBER);
		tcl_compositeInvoiceList.setColumnData(tblclmnInvoiceNo,
			new ColumnPixelData(35, true, true));
		tblclmnInvoiceNo.setText(Messages.InvoiceListView_tblclmnInvoiceNo_text);
		tvcInvoiceNo.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getInvoiceNumber();
				}
				return super.getText(element);
			}
		});
		tblclmnInvoiceNo.addSelectionListener(selectionAdapter);
		
		TableViewerColumn tvcInvoiceState = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnInvoiceState = tvcInvoiceState.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnInvoiceState,
			new ColumnPixelData(90, true, true));
		tblclmnInvoiceState.setText(Messages.InvoiceListView_tblclmnInvoiceState_text);
		tvcInvoiceState.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getInvoiceState().getLocaleText();
				}
				return super.getText(element);
			}
		});
		
		TableViewerColumn tvcPatient = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnPatient = tvcPatient.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnPatient, new ColumnWeightData(10, 100, true));
		tblclmnPatient.setText(Messages.InvoiceListView_tblclmnPatient_text);
		tblclmnPatient.setData(Kontakt.FLD_NAME1);
		tvcPatient.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getPatientName();
				}
				return super.getText(element);
			}
		});
		tblclmnPatient.addSelectionListener(selectionAdapter);
		
		TableViewerColumn tvcLaw = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnLaw = tvcLaw.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnLaw, new ColumnPixelData(20, true, true));
		tblclmnLaw.setText(Messages.InvoiceListView_tblclmnLaw_text);
		tvcLaw.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getLaw();
				}
				return super.getText(element);
			}
		});
		
		TableViewerColumn tvcPayerType = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnType = tvcPayerType.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnType, new ColumnPixelData(20, true, true));
		tblclmnType.setText(Messages.InvoiceListView_tblclmnType_text);
		tvcPayerType.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getPayerType();
				}
				return super.getText(element);
			}
		});
		
		TableViewerColumn tvcReceiver = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnReceiver = tvcReceiver.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnReceiver,
			new ColumnPixelData(150, true, true));
		tblclmnReceiver.setText(Messages.InvoiceListView_tblclmnReceiver_text);
		tvcReceiver.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getReceiverLabel();
				}
				return super.getText(element);
			}
		});
		
		TableViewerColumn tvcTreatmentPeriod =
			new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnTreatmentperiod = tvcTreatmentPeriod.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnTreatmentperiod,
			new ColumnPixelData(100, true, true));
		tblclmnTreatmentperiod.setText(Messages.InvoiceListView_tblclmnTreatmentperiod_text);
		tblclmnTreatmentperiod.setData(Rechnung.BILL_DATE_FROM);
		tvcTreatmentPeriod.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getTreatmentPeriod();
				}
				return super.getText(element);
			}
		});
		tblclmnTreatmentperiod.addSelectionListener(selectionAdapter);
		
		TableViewerColumn tvcOpenAmount = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnOpenAmount = tvcOpenAmount.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnOpenAmount,
			new ColumnPixelData(50, true, true));
		tblclmnOpenAmount.setText(Messages.InvoiceListView_tblclmnOpenAmount_text);
		tvcOpenAmount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					int openAmount = ((InvoiceEntry) element).getOpenAmount();
					return new Money(openAmount).getAmountAsString();
				}
				return super.getText(element);
			}
		});
		
		TableViewerColumn tvcTotalAmount = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnTotalAmount = tvcTotalAmount.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnTotalAmount,
			new ColumnPixelData(50, true, true));
		tblclmnTotalAmount.setText(Messages.InvoiceListView_tblclmnTotalAmount_text);
		tvcTotalAmount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					int totalAmountInCents = ((InvoiceEntry) element).getTotalAmount();
					return new Money(totalAmountInCents).getAmountAsString();
				}
				return super.getText(element);
			}
		});
		
		invoiceListBottomComposite =
			new InvoiceListBottomComposite(parent, SWT.NONE, rnStellerSettings);
		
		invoiceListContentProvider = new InvoiceListContentProvider(tableViewerInvoiceList,
			invoiceListHeaderComposite, invoiceListBottomComposite);
		tableViewerInvoiceList.setContentProvider(invoiceListContentProvider);
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(invoiceActions.rnExportAction);
		menuManager.add(invoiceActions.addPaymentAction);
		menuManager.add(invoiceActions.addExpenseAction);
		menuManager.add(invoiceActions.increaseLevelAction);
		menuManager.add(new Separator());
		menuManager.add(invoiceActions.changeStatusAction);
		menuManager.add(invoiceActions.stornoAction);

		Menu contextMenu = menuManager.createContextMenu(tableViewerInvoiceList.getTable());
		tableInvoiceList.setMenu(contextMenu);
		getSite().registerContextMenu(menuManager, tableViewerInvoiceList);
		getSite().setSelectionProvider(tableViewerInvoiceList);
	
		setSortOrder(tblclmnPatient, SWT.UP);
		
		refresh();
	}
	
	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e){
			TableColumn sortColumn = tableViewerInvoiceList.getTable().getSortColumn();
			TableColumn selectedColumn = (TableColumn) e.widget;
			int sortDirection = tableViewerInvoiceList.getTable().getSortDirection();
			if (sortColumn == selectedColumn) {
				sortDirection = sortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				tableViewerInvoiceList.getTable().setSortColumn(selectedColumn);
				sortDirection = SWT.UP;
			}
			
			setSortOrder(selectedColumn, sortDirection);
		}


	};
	
	private void setSortOrder(TableColumn selectedColumn, int sortDirection){
		tableViewerInvoiceList.getTable().setSortColumn(selectedColumn);
		tableViewerInvoiceList.getTable().setSortDirection(sortDirection);
		invoiceListContentProvider.setSortOrderAndDirection(selectedColumn.getData(), sortDirection);
	}

	
	@Override
	public void setFocus(){
		tableViewerInvoiceList.getTable().setFocus();
		
	}
	
	public InvoiceListContentProvider getInvoiceListContentProvider(){
		return invoiceListContentProvider;
	}
	
}
