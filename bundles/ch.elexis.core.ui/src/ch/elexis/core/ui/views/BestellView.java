/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Joerg M. Sigle (js, jsigle) - bug fixes
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.DailyOrderDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.SelectBestellungDialog;
import ch.elexis.core.ui.editors.KontaktSelektorDialogCellEditor;
import ch.elexis.core.ui.editors.PersistentObjectEditingSupport;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.BestellungEntry;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Stock;
import ch.elexis.data.StockEntry;
import ch.rgw.tools.ExHandler;

public class BestellView extends ViewPart implements ISaveablePart2 {
	
	public static final String ID = "ch.elexis.BestellenView"; //$NON-NLS-1$
	
	private Form form;
	private FormToolkit tk = UiDesk.getToolkit();
	private TableViewer tv;
	private Bestellung actBestellung;
	private ViewMenus viewmenus;
	private IAction removeAction, dailyWizardAction, wizardAction, loadAction, printAction,
			sendAction, newAction;
	private IAction exportClipboardAction, checkInAction;
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new FillLayout());
		form = tk.createForm(parent);
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		Table table = new Table(body, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		tv = new TableViewer(table);
		
		TableViewerColumn tvc0 = new TableViewerColumn(tv, SWT.CENTER);
		tvc0.getColumn().setText(Messages.BestellView_Number);
		tvc0.getColumn().setWidth(40);
		PersistentObjectEditingSupport poes =
			new PersistentObjectEditingSupport(tv, BestellungEntry.FLD_COUNT);
		tvc0.setEditingSupport(poes);
		TableViewerColumn tvc1 = new TableViewerColumn(tv, SWT.LEFT);
		tvc1.getColumn().setText(Messages.BestellView_Article);
		tvc1.getColumn().setWidth(280);
		TableViewerColumn tvc2 = new TableViewerColumn(tv, SWT.LEFT);
		tvc2.getColumn().setText(Messages.BestellView_Dealer);
		tvc2.getColumn().setWidth(250);
		tvc2.setEditingSupport(new EditingSupport(tv) {
			
			@Override
			protected void setValue(Object element, Object value){
				BestellungEntry se = (BestellungEntry) element;
				if (se == null) {
					return;
				}
				se.setProvider((Kontakt) value);
				getViewer().refresh();
			}
			
			@Override
			protected Object getValue(Object element){
				BestellungEntry se = (BestellungEntry) element;
				if (se == null) {
					return null;
				}
				return (Kontakt) se.getProvider();
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return new KontaktSelektorDialogCellEditor(((TableViewer) getViewer()).getTable(),
					"Lieferant auswählen", "Bitte selektieren Sie den Lieferant");
			}
			
			@Override
			protected boolean canEdit(Object element){
				BestellungEntry be = (BestellungEntry) element;
				return (be != null);
			}
		});
		TableViewerColumn tvc3 = new TableViewerColumn(tv, SWT.LEFT);
		tvc3.getColumn().setText("Lager"); //$NON-NLS-1$
		tvc3.getColumn().setWidth(50);
		
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(final Object inputElement){
				if (actBestellung != null) {
					return actBestellung.getEntries().toArray();
				}
				return new Object[0];
			}
			
			public void dispose(){}
			
			public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput){}
			
		});
		tv.setLabelProvider(new BestellungLabelProvider());
		tv.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				BestellungEntry be1 = (BestellungEntry) e1;
				BestellungEntry be2 = (BestellungEntry) e2;
				String s1 = be1.getArticle().getName();
				String s2 = be2.getArticle().getName();
				return s1.compareTo(s2);
			}
		});
		Transfer[] types = new Transfer[] {
			TextTransfer.getInstance()
		};
		tv.addDropSupport(DND.DROP_COPY, types, new DropTargetAdapter() {
			
			@Override
			public void dragEnter(final DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			
			@Override
			public void drop(final DropTargetEvent event){
				String drp = (String) event.data;
				String[] dl = drp.split(StringConstants.COMMA);
				
				if (actBestellung == null) {
					NeueBestellungDialog nbDlg = new NeueBestellungDialog(getViewSite().getShell(),
						Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
					if (nbDlg.open() == Dialog.OK) {
						setBestellung(new Bestellung(nbDlg.getTitle(), CoreHub.actUser));
					} else {
						return;
					}
				}
				
				List<StockEntry> stockEntriesToOrder = new ArrayList<StockEntry>();
				
				for (String obj : dl) {
					PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
					if (dropped instanceof StockEntry) {
						stockEntriesToOrder.add((StockEntry) dropped);
					} else if (dropped instanceof Artikel) {
						Artikel art = (Artikel) dropped;
						if (art.isProduct()) {
							// TODO user message?
							return;
						}
						// use StockEntry if possible
						if (CoreHub.getStockService() != null) {
							IStockEntry se = CoreHub.getStockService()
								.findPreferredStockEntryForArticle(obj, null);
							if (se != null) {
								stockEntriesToOrder.add((StockEntry) se);
								continue;
							}
						}
						// SINGLE SHOT ORDER
						actBestellung.addBestellungEntry((Artikel) dropped, null, null, 1);
					}
				}
				
				for (StockEntry se : stockEntriesToOrder) {
					int current = se.getCurrentStock();
					int max = se.getMaximumStock();
					if (max == 0) {
						max = se.getMinimumStock();
					}
					int toOrder = max - current;
					
					actBestellung.addBestellungEntry(se.getArticle(), se.getStock(),
						se.getProvider(), toOrder);
				}
				
				tv.refresh();
			}
			
		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createToolbar(newAction, dailyWizardAction, wizardAction, loadAction, printAction,
			sendAction);
		viewmenus.createMenu(newAction, dailyWizardAction, wizardAction, loadAction, printAction,
			sendAction, exportClipboardAction);
		viewmenus.createViewerContextMenu(tv, new IAction[] {
			removeAction
		});
		form.getToolBarManager().add(checkInAction);
		form.updateToolBar();
		setBestellung(null);
		tv.setInput(getViewSite());
	}
	
	private void setBestellung(final Bestellung b){
		actBestellung = b;
		if (b != null && !form.isDisposed()) {
			form.setText(b.getLabel());
			tv.refresh();
			updateCheckIn();
		} else {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.BestellView_NoOrder);
		}
	}
	
	private void updateCheckIn(){
		if (actBestellung.isDone()) {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.BestellView_OrderIsClosed);
		} else {
			checkInAction.setEnabled(true);
			checkInAction.setToolTipText(Messages.BestellView_CheckInCaption);
		}
	}
	
	@Override
	public void setFocus(){
		
	}
	
	private List<BestellungEntry> prepareOrderList(Kontakt receiver){
		ArrayList<BestellungEntry> best = new ArrayList<BestellungEntry>();
		List<BestellungEntry> list = actBestellung.getEntries();
		for (BestellungEntry bestellungEntry : list) {
			if (receiver == null) {
				receiver = bestellungEntry.getProvider();
				if (!receiver.exists()) {
					receiver = null;
					continue;
				}
			}
			if (bestellungEntry.getProvider() != null
				&& bestellungEntry.getProvider().getId().equals(receiver.getId())) {
				best.add(bestellungEntry);
			}
		}
		best.sort((BestellungEntry left, BestellungEntry right) -> {
			String s1 = left.getArticle().getName();
			String s2 = right.getArticle().getName();
			return s1.compareTo(s2);
		});
		return best;
	}
	
	class BestellungLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(final Object element, final int columnIndex){
			return null;
		}
		
		public String getColumnText(final Object element, final int columnIndex){
			BestellungEntry be = (BestellungEntry) element;
			switch (columnIndex) {
			case 0:
				return Integer.toString(be.getCount());
			case 1:
				return be.getArticle().getLabel();
			case 2:
				Kontakt k = be.getProvider();
				return (k != null) ? k.getLabel() : Messages.BestellView_Unknown; //$NON-NLS-1$
			case 3:
				Stock s = be.getStock();
				return (s != null) ? s.getCode() : StringConstants.EMPTY;
			default:
				return "?"; //$NON-NLS-1$
			}
		}
	}
	
	private void makeActions(){
		removeAction = new Action(Messages.BestellView_RemoveArticle) { //$NON-NLS-1$
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					if (actBestellung != null) {
						@SuppressWarnings("unchecked")
						List<BestellungEntry> selections = sel.toList();
						for (BestellungEntry entry : selections) {
							actBestellung.removeEntry(entry);
						}
						tv.refresh();
					}
				}
			}
		};
		dailyWizardAction = new Action(Messages.BestellView_AutomaticDailyOrder) {
			{
				setToolTipText(Messages.BestellView_CreateAutomaticDailyOrder);
				setImageDescriptor(Images.IMG_WIZ_DAY.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (actBestellung == null) {
					setBestellung(
						new Bestellung(Messages.BestellView_AutomaticDaily, CoreHub.actUser)); //$NON-NLS-1$
				} else {
					if (!actBestellung.getTime().toLocalDate().equals(LocalDate.now())) {
						if (MessageDialog.openQuestion(getSite().getShell(),
							Messages.BestellView_Title, Messages.BestellView_WizardAskNewOrder)) {
							setBestellung(
								new Bestellung(Messages.BestellView_Automatic, CoreHub.actUser));
						}
					}
				}
				
				DailyOrderDialog doDlg = new DailyOrderDialog(UiDesk.getTopShell(), actBestellung);
				doDlg.open();
				updateCheckIn();
				tv.refresh(true);
			}
		};
		
		wizardAction = new Action(Messages.BestellView_AutomaticOrder) { //$NON-NLS-1$
			{
				setToolTipText(Messages.BestellView_CreateAutomaticOrder); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (actBestellung == null) {
					setBestellung(new Bestellung(Messages.BestellView_Automatic, CoreHub.actUser));
				} else {
					if (!actBestellung.getTime().toLocalDate().equals(LocalDate.now())) {
						if (MessageDialog.openQuestion(getSite().getShell(),
							Messages.BestellView_Title, Messages.BestellView_WizardAskNewOrder)) {
							setBestellung(
								new Bestellung(Messages.BestellView_Automatic, CoreHub.actUser));
						}
					}
				}
				
				int trigger = CoreHub.globalCfg.get(
					ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER,
					ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
				boolean isInventoryBelow =
					trigger == ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_BELOW;
				
				boolean excludeAlreadyOrderedItems = CoreHub.globalCfg.get(
					Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
					Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);
				
				Query<StockEntry> qbe = new Query<StockEntry>(StockEntry.class, null, null,
					StockEntry.TABLENAME, new String[] {
						StockEntry.FLD_CURRENT, StockEntry.FLD_MAX
				});
				qbe.add(StockEntry.FLD_CURRENT, isInventoryBelow ? Query.LESS : Query.LESS_OR_EQUAL,
					StockEntry.FLD_MIN);
				List<StockEntry> stockEntries = qbe.execute();
				for (StockEntry se : stockEntries) {
					if (se.getArticle() != null) {
						if (excludeAlreadyOrderedItems) {
							IOrderEntry open =
								CoreHub.getOrderService().findOpenOrderEntryForStockEntry(se);
							// only add if not on an open order
							if (open != null) {
								continue;
							}
						} 
						CoreHub.getOrderService().addRefillForStockEntryToOrder(se,
							actBestellung);
					} else {
						LoggerFactory.getLogger(getClass())
							.warn("Could not resolve article " + se.get(StockEntry.FLD_ARTICLE_TYPE)
								+ se.get(StockEntry.FLD_ARTICLE_ID) + " of stock entry "
								+ se.getId());
					}
				}
				updateCheckIn();
				tv.refresh(true);
			}
		};
		newAction = new Action(Messages.BestellView_CreateNewOrder) { //$NON-NLS-1$
			@Override
			public void run(){
				NeueBestellungDialog nbDlg = new NeueBestellungDialog(getViewSite().getShell(),
					Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
				if (nbDlg.open() == Dialog.OK) {
					setBestellung(new Bestellung(nbDlg.getTitle(), CoreHub.actUser));
				} else {
					return;
				}
				tv.refresh();
			}
		};
		printAction = new Action(Messages.BestellView_PrintOrder) { //$NON-NLS-1$
			@Override
			public void run(){
				if (actBestellung != null) {
					
					Kontakt receiver = null;
					List<BestellungEntry> best = prepareOrderList(receiver);
					
					try {
						BestellBlatt bb =
							(BestellBlatt) getViewSite().getPage().showView(BestellBlatt.ID);
						bb.createOrder(receiver, best);
						tv.refresh();
						
						Bestellung.markAsOrdered(best);
					} catch (PartInitException e) {
						ExHandler.handle(e);
						
					}
				}
			}
		};
		sendAction = new Action(Messages.BestellView_SendOrder) {
			@Override
			public void run(){
				if (actBestellung == null)
					return;
				
				// organise items in supplier and non-supplier lists
				List<BestellungEntry> orderableItems = new ArrayList<BestellungEntry>();
				List<BestellungEntry> noSupplierItems = new ArrayList<BestellungEntry>();
				for (BestellungEntry item : actBestellung.getEntries()) {
					Kontakt supplier = item.getProvider();
					if (supplier != null && supplier.exists()) {
						orderableItems.add(item);
					} else {
						noSupplierItems.add(item);
					}
				}
				
				boolean runOrder = true;
				if (!noSupplierItems.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (BestellungEntry noSupItem : noSupplierItems) {
						sb.append(noSupItem.getArticle().getLabel());
						sb.append("\n");
					}
					runOrder =
						SWTHelper.askYesNo(Messages.BestellView_NoSupplierArticle, MessageFormat
							.format(Messages.BestellView_NoSupplierArticleMsg, sb.toString()));
				}
				
				if (runOrder) {
					List<IConfigurationElement> list =
						Extensions.getExtensions(ExtensionPointConstantsUi.TRANSPORTER);
					for (IConfigurationElement ic : list) {
						String handler = ic.getAttribute("type");
						if (handler != null && handler.contains(Bestellung.class.getName())) {
							try {
								IDataSender sender = (IDataSender) ic.createExecutableExtension(
									ExtensionPointConstantsUi.TRANSPORTER_EXPC);
								
								sender.store(actBestellung);
								sender.finalizeExport();
								SWTHelper.showInfo(Messages.BestellView_OrderSentCaption,
									Messages.BestellView_OrderSentBody);
								tv.refresh();
								
								Bestellung.markAsOrdered(orderableItems);
								
							} catch (CoreException ex) {
								ExHandler.handle(ex);
							} catch (XChangeException xx) {
								SWTHelper.showError(Messages.BestellView_OrderNotPossible,
									Messages.BestellView_NoAutomaticOrderAvailable
										+ xx.getLocalizedMessage());
								
							}
						}
					}
				}
			}
		};
		loadAction = new Action(Messages.BestellView_OpenOrder) {
			@Override
			public void run(){
				
				SelectBestellungDialog dlg = new SelectBestellungDialog(getViewSite().getShell());
				dlg.setMessage(Messages.BestellView_SelectOrder); //$NON-NLS-1$
				dlg.setTitle(Messages.BestellView_ReadOrder); //$NON-NLS-1$
				
				if (dlg.open() == Dialog.OK) {
					if (dlg.getResult().length > 0) {
						Bestellung res = (Bestellung) dlg.getResult()[0];
						setBestellung(res);
					}
				}
			}
		};
		printAction.setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
		printAction.setToolTipText(Messages.BestellView_PrintOrder);
		newAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
		newAction.setToolTipText(Messages.BestellView_CreateNewOrder);
		sendAction.setImageDescriptor(Images.IMG_NETWORK.getImageDescriptor());
		sendAction.setToolTipText(Messages.BestellView_transmitOrder);
		loadAction.setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
		loadAction.setToolTipText(Messages.BestellView_loadEarlierOrder);
		
		exportClipboardAction = new Action(Messages.BestellView_copyToClipboard) {
			{
				setToolTipText(Messages.BestellView_copyToClipBioardForGalexis);
			}
			
			@Override
			public void run(){
				if (actBestellung != null) {
					Kontakt receiver = null;
					List<BestellungEntry> best = prepareOrderList(receiver);
					
					StringBuffer export = new StringBuffer();
					for (BestellungEntry item : best) {
						String pharmaCode = item.getArticle().getPharmaCode();
						int num = item.getCount();
						String name = item.getArticle().getName();
						String line = pharmaCode + ", " + num + ", " + name; //$NON-NLS-1$ //$NON-NLS-2$
						
						export.append(line);
						export.append(System.getProperty("line.separator")); //$NON-NLS-1$
					}
					
					String clipboardText = export.toString();
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
			}
		};
		checkInAction = new Action(Messages.BestellView_CheckInCaption) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_TICK.getImageDescriptor());
				setToolTipText(Messages.BestellView_CheckInBody); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				if (actBestellung != null && actBestellung.exists()) {
					OrderImportDialog dialog =
						new OrderImportDialog(getSite().getShell(), actBestellung);
					dialog.open();
					updateCheckIn();
				} else {
					SWTHelper.alert(Messages.BestellView_NoOrder, //$NON-NLS-1$
						Messages.BestellView_NoOrderLoaded); //$NON-NLS-1$
				}
			}
			
		};
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(final IProgressMonitor monitor){ /* leer */
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
	
	public void addItemsToOrder(List<Artikel> articlesToOrder){
		if (actBestellung == null) {
			NeueBestellungDialog nbDlg = new NeueBestellungDialog(getViewSite().getShell(),
				Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
			if (nbDlg.open() == Dialog.OK) {
				setBestellung(new Bestellung(nbDlg.getTitle(), CoreHub.actUser));
			} else {
				return;
			}
		}
		
		for (Artikel arti : articlesToOrder) {
			// SINGLE SHOT ORDER
			actBestellung.addBestellungEntry((Artikel) arti, null, null, 1);
		}
		if (tv != null && !tv.getControl().isDisposed()) {
			tv.refresh();
		}
	}
	
	/**
	 * @return the current defined {@link Bestellung} in this view
	 */
	public Bestellung getActBestellung(){
		return actBestellung;
	}
	
	/**
	 * Find the default supplier. Shows a warning if supplier is null or inexisting
	 * 
	 * @param cfgSupplier
	 *            value delivered from the plugins configured supplier field
	 * @param selDialogTitle
	 *            title of the dialog
	 * @return the supplier or null if none could be resolved.
	 */
	public static Kontakt resolveDefaultSupplier(String cfgSupplier, String selDialogTitle){
		Kontakt supplier = null;
		if (cfgSupplier != null && !cfgSupplier.isEmpty()) {
			supplier = Kontakt.load(cfgSupplier);
		}
		
		//warn that there is no supplier
		if (supplier == null || !supplier.exists()) {
			MessageDialog.openWarning(UiDesk.getTopShell(), selDialogTitle,
				Messages.BestellView_CantOrderNoSupplier);
		}
		return supplier;
	}
}
