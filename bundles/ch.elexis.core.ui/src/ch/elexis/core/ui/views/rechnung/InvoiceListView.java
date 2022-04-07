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

import static ch.elexis.core.ui.views.rechnung.invoice.InvoiceListSqlQuery.VIEW_FLD_INVOICENO;
import static ch.elexis.core.ui.views.rechnung.invoice.InvoiceListSqlQuery.VIEW_FLD_INVOICETOTAL;
import static ch.elexis.core.ui.views.rechnung.invoice.InvoiceListSqlQuery.VIEW_FLD_OPENAMOUNT;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceActions;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListBottomComposite;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider.InvoiceEntry;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListHeaderComposite;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListSqlQuery;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.io.Settings;
import ch.rgw.tools.Money;

public class InvoiceListView extends ViewPart implements IRefreshablePart {
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
	
	private Action reloadViewAction = new Action(Messages.RnActions_reloadAction) {
		{
			setToolTipText(Messages.RnActions_reloadTooltip);
			setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
		}
		
		@Override
		public void run(){
			refresh();
		}
	};
	
	@Override
	public void refresh(Map<Object, Object> filterParameters){
		if (invoiceListContentProvider != null) {
			if (filterParameters.containsKey(ch.elexis.core.model.IPatient.class)) {
				ch.elexis.core.model.IPatient patient =
					(ch.elexis.core.model.IPatient) filterParameters
						.get(ch.elexis.core.model.IPatient.class);
				invoiceListHeaderComposite.setSelectedPatientId(Patient.load(patient.getId()));
			}
			// reset comparator, do not comparing lazy loaded fields
			if (tableViewerInvoiceList != null && !tableViewerInvoiceList.getTable().isDisposed()
				&& tableViewerInvoiceList.getComparator() != null) {
				tableViewerInvoiceList.setComparator(null);
				setSortOrder(tableViewerInvoiceList.getTable().getColumn(3), SWT.UP);
			}
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
		
		tableViewerInvoiceList = new TableViewer(compositeInvoiceList,
			SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.VIRTUAL);
		tableViewerInvoiceList.addSelectionChangedListener(selection -> {
			StructuredSelection ss = (StructuredSelection) selection.getSelection();
			if (!ss.isEmpty()) {
				InvoiceEntry firstElement = (InvoiceEntry) ss.getFirstElement();
				Rechnung load = Rechnung.load(firstElement.getInvoiceId());
				ElexisEventDispatcher.fireSelectionEvent(load);
				Fall f = load.getFall();
				if (f != null) {
					ElexisEventDispatcher.fireSelectionEvent(f.getPatient());
					ElexisEventDispatcher.fireSelectionEvent(f);
				}
			}
		});
		Table tableInvoiceList = tableViewerInvoiceList.getTable();
		tableInvoiceList.setHeaderVisible(true);
		tableInvoiceList.setLinesVisible(false);
		
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
		tblclmnInvoiceNo.setData(VIEW_FLD_INVOICENO);
		tcl_compositeInvoiceList.setColumnData(tblclmnInvoiceNo,
			new ColumnPixelData(50, true, true));
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
		tblclmnInvoiceNo.addSelectionListener(sortAdapter);
		
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
		
		TableViewerColumn tvcInvoiceStateDateSince =
			new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnInvoiceStateDateSince = tvcInvoiceStateDateSince.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnInvoiceStateDateSince,
			new ColumnPixelData(50, true, true));
		tblclmnInvoiceStateDateSince
			.setText(Messages.InvoiceListView_tblclmnInvoiceStateDateSince_text);
		tvcInvoiceStateDateSince.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getInvoiceStateSinceDays() + "d";
				}
				return super.getText(element);
			}
		});
		tblclmnInvoiceStateDateSince.setData(InvoiceListSqlQuery.VIEW_FLD_INVOICESTATEDATE);
		tblclmnInvoiceStateDateSince.addSelectionListener(sortAdapter);
		
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
		tblclmnPatient.addSelectionListener(sortAdapter);
		
		TableViewerColumn tvcBillingSystem =
			new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnLaw = tvcBillingSystem.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnLaw, new ColumnPixelData(50, true, true));
		tblclmnLaw.setText(Messages.InvoiceListView_tblclmnLaw_text);
		tvcBillingSystem.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					return ((InvoiceEntry) element).getBillingSystem();
				}
				return super.getText(element);
			}
		});
		tblclmnLaw.addSelectionListener(sortViewerAdapter);
		
		TableViewerColumn tvcPayerType = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnType = tvcPayerType.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnType, new ColumnPixelData(50, true, true));
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
		tblclmnType.addSelectionListener(sortViewerAdapter);
		
		TableViewerColumn tvcReceiver = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnReceiver = tvcReceiver.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnReceiver,
			new ColumnPixelData(150, true, true));
		tblclmnReceiver.setText(Messages.InvoiceListView_tblclmnReceiver_text);
		tvcReceiver.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof InvoiceEntry) {
					String receiverLabel = ((InvoiceEntry) element).getReceiverLabel();
					if (((InvoiceEntry) element).isResolved()) {
						return (receiverLabel != null) ? receiverLabel
								: Messages.ContactNotAvailable;
					}
					return null;
				}
				return super.getText(element);
			}
			
			@Override
			public Color getBackground(Object element){
				if (element instanceof InvoiceEntry) {
					String receiverLabel = ((InvoiceEntry) element).getReceiverLabel();
					if (((InvoiceEntry) element).isResolved()) {
						return (receiverLabel != null) ? null : UiDesk.getColor(UiDesk.COL_RED);
					}
					return null;
				}
				return super.getBackground(element);
			}
		});
		tblclmnReceiver.addSelectionListener(sortViewerAdapter);
		
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
		tblclmnTreatmentperiod.addSelectionListener(sortAdapter);
		
		TableViewerColumn tvcOpenAmount = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnOpenAmount = tvcOpenAmount.getColumn();
		tcl_compositeInvoiceList.setColumnData(tblclmnOpenAmount,
			new ColumnPixelData(60, true, true));
		tblclmnOpenAmount.setText(Messages.InvoiceListView_tblclmnOpenAmount_text);
		tblclmnOpenAmount.setData(VIEW_FLD_OPENAMOUNT);
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
		tblclmnOpenAmount.addSelectionListener(sortAdapter);
		
		TableViewerColumn tvcTotalAmount = new TableViewerColumn(tableViewerInvoiceList, SWT.NONE);
		TableColumn tblclmnTotalAmount = tvcTotalAmount.getColumn();
		tblclmnTotalAmount.setData(VIEW_FLD_INVOICETOTAL);
		tcl_compositeInvoiceList.setColumnData(tblclmnTotalAmount,
			new ColumnPixelData(60, true, true));
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
		tblclmnTotalAmount.addSelectionListener(sortAdapter);
		
		invoiceListBottomComposite =
			new InvoiceListBottomComposite(parent, SWT.NONE, rnStellerSettings);
		
		invoiceListContentProvider = new InvoiceListContentProvider(tableViewerInvoiceList,
			invoiceListHeaderComposite, invoiceListBottomComposite);
		tableViewerInvoiceList.setContentProvider(invoiceListContentProvider);
		
		InvoiceActions invoiceActions = new InvoiceActions(tableViewerInvoiceList, getViewSite());
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(reloadViewAction);
		tbm.add(invoiceActions.mahnWizardAction);
		tbm.add(invoiceActions.exportListAction);
		tbm.add(invoiceListContentProvider.rnFilterAction);
		tbm.add(new Separator());
		tbm.add(invoiceActions.rnExportAction);
		
		IMenuManager viewMenuManager = getViewSite().getActionBars().getMenuManager();
		viewMenuManager.add(invoiceActions.printListeAction);
		viewMenuManager.add(invoiceActions.addAccountExcessAction);
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(invoiceActions.rnExportAction);
		menuManager.add(invoiceActions.addPaymentAction);
		menuManager.add(invoiceActions.addExpenseAction);
		menuManager.add(invoiceActions.increaseLevelAction);
		menuManager.add(new Separator());
		menuManager.add(invoiceActions.changeStatusAction);
		menuManager.add(invoiceActions.stornoAction);
		menuManager.add(new Separator());
		menuManager.add(invoiceActions.deleteAction);
		menuManager.add(invoiceActions.reactivateAction);
		
		menuManager.addMenuListener((mats) -> {
			@SuppressWarnings("unchecked")
			List<InvoiceEntry> selectedElements =
				((StructuredSelection) tableViewerInvoiceList.getSelection()).toList();
			
			boolean allDefective = selectedElements.stream()
				.allMatch(f -> InvoiceState.DEFECTIVE == f.getInvoiceState());
			
			invoiceActions.deleteAction.setEnabled(allDefective);
			invoiceActions.reactivateAction.setEnabled(allDefective);
		});
		
		Menu contextMenu = menuManager.createContextMenu(tableViewerInvoiceList.getTable());
		tableInvoiceList.setMenu(contextMenu);
		getSite().registerContextMenu(menuManager, tableViewerInvoiceList);
		getSite().setSelectionProvider(tableViewerInvoiceList);
		
		setSortOrder(tblclmnPatient, SWT.UP);
		
		refresh();
	}
	
	private SelectionAdapter sortAdapter = new SelectionAdapter() {
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
			tableViewerInvoiceList.setComparator(null);
			setSortOrder(selectedColumn, sortDirection);
		}
		
	};
	
	private SelectionAdapter sortViewerAdapter = new SelectionAdapter() {
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
			tableViewerInvoiceList.getTable().setSortDirection(sortDirection);
			tableViewerInvoiceList.setComparator(sortViewerComparator);
			tableViewerInvoiceList.refresh();
		}
	};
	
	private ViewerComparator sortViewerComparator = new ViewerComparator() {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			TableColumn sortColumn = ((TableViewer) viewer).getTable().getSortColumn();
			int sortDirection = ((TableViewer) viewer).getTable().getSortDirection();
			if (tableViewerInvoiceList.getTable().indexOf(sortColumn) == 6) {
				InvoiceEntry l = (InvoiceEntry) e1;
				InvoiceEntry r = (InvoiceEntry) e2;
				resolve(l);
				resolve(r);
				String s1 = l.getReceiverLabel();
				String s2 = r.getReceiverLabel();
				return getComparator().compare(s1, s2) * (sortDirection == SWT.UP ? 1 : -1);
			} else if (tableViewerInvoiceList.getTable().indexOf(sortColumn) == 5) {
				InvoiceEntry l = (InvoiceEntry) e1;
				InvoiceEntry r = (InvoiceEntry) e2;
				resolve(l);
				resolve(r);
				String s1 = l.getPayerType();
				String s2 = r.getPayerType();
				return getComparator().compare(s1, s2) * (sortDirection == SWT.UP ? 1 : -1);
			} else if (tableViewerInvoiceList.getTable().indexOf(sortColumn) == 4) {
				InvoiceEntry l = (InvoiceEntry) e1;
				InvoiceEntry r = (InvoiceEntry) e2;
				resolve(l);
				resolve(r);
				String s1 = l.getBillingSystem();
				String s2 = r.getBillingSystem();
				return getComparator().compare(s1, s2) * (sortDirection == SWT.UP ? 1 : -1);
			}
			return super.compare(viewer, e1, e2);
		}
		
		private void resolve(InvoiceEntry entry){
			while (!entry.isResolved()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	};
	
	private void setSortOrder(TableColumn selectedColumn, int sortDirection){
		tableViewerInvoiceList.getTable().setSortColumn(selectedColumn);
		tableViewerInvoiceList.getTable().setSortDirection(sortDirection);
		invoiceListContentProvider.setSortOrderAndDirection(selectedColumn.getData(),
			sortDirection);
	}
	
	@Override
	public void setFocus(){
		tableViewerInvoiceList.getTable().setFocus();
	}
	
	public InvoiceListContentProvider getInvoiceListContentProvider(){
		return invoiceListContentProvider;
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
}
