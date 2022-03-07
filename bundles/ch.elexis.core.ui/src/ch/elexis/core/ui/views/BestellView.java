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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.DailyOrderDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.SelectBestellungDialog;
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import ch.elexis.core.ui.editors.ReflectiveEditingSupport;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.TableViewerSorter;
import ch.elexis.core.ui.util.TableViewerSorter.IColumnContentProvider;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Bestellung;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.ExHandler;

public class BestellView extends ViewPart {
	
	public static final String ID = "ch.elexis.BestellenView"; //$NON-NLS-1$
	
	private Form form;
	private FormToolkit tk = UiDesk.getToolkit();
	private TableViewer tv;
	private IOrder actOrder;
	private ViewMenus viewmenus;
	private IAction removeAction, dailyWizardAction, wizardAction, loadAction, printAction,
			sendAction, newAction;
	private IAction exportClipboardAction, checkInAction;
	
	private BestellungLabelProvider blp;
	
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
		
		TableViewerColumn tvcNumber = new TableViewerColumn(tv, SWT.CENTER);
		tvcNumber.getColumn().setText(Messages.BestellView_Number);
		tvcNumber.getColumn().setWidth(40);
		ReflectiveEditingSupport poes =
			new ReflectiveEditingSupport(tv, ModelPackage.Literals.IORDER_ENTRY__AMOUNT.getName());
		poes.setModelService(CoreModelServiceHolder.get());
		tvcNumber.setEditingSupport(poes);
		
		TableViewerColumn tvcArticle = new TableViewerColumn(tv, SWT.LEFT);
		tvcArticle.getColumn().setText(Messages.BestellView_Article);
		tvcArticle.getColumn().setWidth(280);
		
		TableViewerColumn tvcDealer = new TableViewerColumn(tv, SWT.LEFT);
		tvcDealer.getColumn().setText(Messages.BestellView_Dealer);
		tvcDealer.getColumn().setWidth(250);
		tvcDealer.setEditingSupport(new EditingSupport(tv) {
			
			@Override
			protected void setValue(Object element, Object value){
				IOrderEntry se = (IOrderEntry) element;
				if (se == null) {
					return;
				}
				se.setProvider((IContact) value);
				ch.elexis.core.services.holder.CoreModelServiceHolder.get().save(se);
				getViewer().refresh(element, true);
			}
			
			@Override
			protected Object getValue(Object element){
				IOrderEntry se = (IOrderEntry) element;
				if (se == null) {
					return null;
				}
				return (IContact) se.getProvider();
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return new ContactSelectionDialogCellEditor(((TableViewer) getViewer()).getTable(),
					"Lieferant auswählen", "Bitte selektieren Sie den Lieferant");
			}
			
			@Override
			protected boolean canEdit(Object element){
				IOrderEntry be = (IOrderEntry) element;
				return (be != null);
			}
		});
		TableViewerColumn tvc3 = new TableViewerColumn(tv, SWT.LEFT);
		tvc3.getColumn().setText("Lager"); //$NON-NLS-1$
		tvc3.getColumn().setWidth(50);
		
		tv.setContentProvider(new BestellungContentProvider());
		blp = new BestellungLabelProvider();
		tv.setLabelProvider(blp);
		
		new TableViewerSorter(tv);
		
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
				if (event.data instanceof String) {
					String[] parts = ((String) event.data).split(StringConstants.COMMA);
					
					if (actOrder == null) {
						NeueBestellungDialog nbDlg = new NeueBestellungDialog(
							getViewSite().getShell(), Messages.BestellView_CreateNewOrder,
							Messages.BestellView_EnterOrderTitle);
						if (nbDlg.open() == Dialog.OK) {
							IOrder created = CoreModelServiceHolder.get().create(IOrder.class);
							created.setTimestamp(LocalDateTime.now());
							created.setName(nbDlg.getTitle());
							CoreModelServiceHolder.get().save(created);
							setOrder(created);
						} else {
							return;
						}
					}
					
					List<IStockEntry> stockEntriesToOrder = new ArrayList<IStockEntry>();
					
					for (String storeToString : parts) {
						Optional<Identifiable> dropped =
							StoreToStringServiceHolder.get().loadFromString(storeToString);
						if (dropped.isPresent()) {
							if (dropped.get() instanceof IStockEntry) {
								stockEntriesToOrder.add((IStockEntry) dropped.get());
							} else if (dropped.get() instanceof IArticle) {
								IArticle art = (IArticle) dropped.get();
								if (art.isProduct()) {
									// TODO user message?
									return;
								}
								// use StockEntry if possible
								IStockEntry stockEntry =
									StockServiceHolder.get().findPreferredStockEntryForArticle(
										StoreToStringServiceHolder.getStoreToString(art), null);
								
								if (stockEntry != null) {
									stockEntriesToOrder.add(stockEntry);
									continue;
								}
								// SINGLE SHOT ORDER
								IOrderEntry orderEntry = actOrder.addEntry(art, null, null, 1);
								CoreModelServiceHolder.get().save(orderEntry);
							}
						}
					}
					if (!stockEntriesToOrder.isEmpty()) {
						for (IStockEntry iStockEntry : stockEntriesToOrder) {
							IOrderEntry orderEntry = actOrder.addEntry(iStockEntry.getArticle(),
								iStockEntry.getStock(), iStockEntry.getProvider(), 1);
							CoreModelServiceHolder.get().save(orderEntry);
						}
					}
					
					tv.refresh();
				}
			}
			
		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createToolbar(newAction, dailyWizardAction, wizardAction, loadAction, printAction,
			sendAction);
		viewmenus.createMenu(exportClipboardAction);
		viewmenus.createViewerContextMenu(tv, new IAction[] {
			removeAction
		});
		form.getToolBarManager().add(checkInAction);
		form.updateToolBar();
		setOrder(null);
		tv.setInput(getViewSite());
	}
	
	private void setOrder(final IOrder order){
		actOrder = order;
		if (order != null && !form.isDisposed()) {
			form.setText(order.getName());
			tv.refresh();
			updateCheckIn();
		} else {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.BestellView_NoOrder);
		}
	}
	
	private void updateCheckIn(){
		if (actOrder.isDone()) {
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
	
	private Map<IContact, List<IOrderEntry>> prepareOrderMap(){
		Map<IContact, List<IOrderEntry>> ret = new HashMap<>();
		
		List<IOrderEntry> list = actOrder.getEntries();
		for (IOrderEntry iOrderEntry : list) {
			list = ret.get(iOrderEntry.getProvider());
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(iOrderEntry);
			ret.put(iOrderEntry.getProvider(), list);
		}
		for (List<IOrderEntry> best : ret.values()) {
			best.sort((IOrderEntry left, IOrderEntry right) -> {
				String s1 = left.getArticle().getName();
				String s2 = right.getArticle().getName();
				return s1.compareTo(s2);
			});
		}
		return ret;
	}
	
	private List<IOrderEntry> prepareOrderList(IContact receiver){
		ArrayList<IOrderEntry> best = new ArrayList<IOrderEntry>();
		List<IOrderEntry> list = actOrder.getEntries();
		for (IOrderEntry iOrderEntry : list) {
			if (receiver == null) {
				receiver = iOrderEntry.getProvider();
				if (receiver == null) {
					continue;
				}
			}
			if (iOrderEntry.getProvider() != null
				&& iOrderEntry.getProvider().getId().equals(receiver.getId())) {
				best.add(iOrderEntry);
			}
		}
		best.sort((IOrderEntry left, IOrderEntry right) -> {
			String s1 = left.getArticle().getName();
			String s2 = right.getArticle().getName();
			return s1.compareTo(s2);
		});
		return best;
	}
	
	private class BestellungLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		@Override
		public Image getColumnImage(final Object element, final int columnIndex){
			return null;
		}
		
		@Override
		public String getColumnText(final Object element, final int columnIndex){
			IOrderEntry entry = (IOrderEntry) element;
			switch (columnIndex) {
			case 0:
				return Integer.toString(entry.getAmount());
			case 1:
				return entry.getArticle().getLabel();
			case 2:
				IContact k = entry.getProvider();
				return (k != null) ? k.getLabel() : Messages.BestellView_Unknown;
			case 3:
				IStock s = entry.getStock();
				return (s != null) ? s.getCode() : StringConstants.EMPTY;
			default:
				return "?"; //$NON-NLS-1$
			}
		}
	}
	
	private class BestellungContentProvider
			implements IStructuredContentProvider, IColumnContentProvider {
		
		@Override
		public Comparable<?> getValue(Object element, int columnIndex){
			if (columnIndex == 0) {
				return ((IOrderEntry) element).getAmount();
			}
			return blp.getColumnText(element, columnIndex);
		}
		
		@Override
		public Object[] getElements(Object inputElement){
			if (actOrder != null) {
				return actOrder.getEntries().toArray();
			}
			return new Object[0];
		}
	}
	
	private IOrder createOrder(String name){
		IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName(name);
		CoreModelServiceHolder.get().save(order);
		return order;
	}
	
	private void makeActions(){
		removeAction = new Action(Messages.BestellView_RemoveArticle) { //$NON-NLS-1$
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					if (actOrder != null) {
						@SuppressWarnings("unchecked")
						List<IOrderEntry> selections = sel.toList();
						for (IOrderEntry entry : selections) {
							CoreModelServiceHolder.get().remove(entry);
							CoreModelServiceHolder.get().refresh(actOrder, true);
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
				if (actOrder == null) {
					IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
					order.setTimestamp(LocalDateTime.now());
					order.setName(Messages.BestellView_AutomaticDaily);
					CoreModelServiceHolder.get().save(order);
					setOrder(order);
				} else {
					if (!actOrder.getTimestamp().toLocalDate().equals(LocalDate.now())) {
						if (MessageDialog.openQuestion(getSite().getShell(),
							Messages.BestellView_Title, Messages.BestellView_WizardAskNewOrder)) {
							setOrder(createOrder(Messages.BestellView_Automatic));
						}
					}
				}
				DailyOrderDialog doDlg = new DailyOrderDialog(UiDesk.getTopShell(), actOrder);
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
				if (actOrder == null) {
					setOrder(createOrder(Messages.BestellView_Automatic));
				} else {
					if (!actOrder.getTimestamp().toLocalDate().equals(LocalDate.now())) {
						if (MessageDialog.openQuestion(getSite().getShell(),
							Messages.BestellView_Title, Messages.BestellView_WizardAskNewOrder)) {
							setOrder(createOrder(Messages.BestellView_Automatic));
						}
					}
				}
				
				int trigger = ConfigServiceHolder.get().get(
					ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER,
					ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
				boolean isInventoryBelow =
					trigger == ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_BELOW;
				
				boolean excludeAlreadyOrderedItems = ConfigServiceHolder.get().get(
					Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
					Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);
				
				IQuery<IStockEntry> query =
					CoreModelServiceHolder.get().getQuery(IStockEntry.class);
				query.andFeatureCompare(ModelPackage.Literals.ISTOCK_ENTRY__CURRENT_STOCK,
					isInventoryBelow ? COMPARATOR.LESS : COMPARATOR.LESS_OR_EQUAL,
					ModelPackage.Literals.ISTOCK_ENTRY__MINIMUM_STOCK);
				List<IStockEntry> stockEntries = query.execute();
				for (IStockEntry stockEntry : stockEntries) {
					if (stockEntry.getArticle() != null) {
						if (excludeAlreadyOrderedItems) {
							IOrderEntry open = OrderServiceHolder.get()
								.findOpenOrderEntryForStockEntry(stockEntry);
							// only add if not on an open order
							if (open != null) {
								continue;
							}
						}
						OrderServiceHolder.get().addRefillForStockEntryToOrder(stockEntry,
							actOrder);
					} else {
						LoggerFactory.getLogger(getClass()).warn("Could not resolve article "
							+ stockEntry.getLabel() + " of stock entry " + stockEntry.getId());
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
					setOrder(createOrder(nbDlg.getTitle()));
				} else {
					return;
				}
				tv.refresh();
			}
		};
		printAction = new Action(Messages.BestellView_PrintOrder) { //$NON-NLS-1$
			@Override
			public void run(){
				if (actOrder != null) {
					Map<IContact, List<IOrderEntry>> orderMap = prepareOrderMap();
					
					for (IContact receiver : orderMap.keySet()) {
						List<IOrderEntry> entries = orderMap.get(receiver);
						if (receiver == null) {
							KontaktSelektor ksel = new KontaktSelektor(getViewSite().getShell(),
								Kontakt.class,
								ch.elexis.core.ui.text.Messages.TextContainer_SelectDestinationHeader,
								"Addressat für Einträge ohne Lieferanten", Kontakt.DEFAULT_SORT);
							if (ksel.open() == Dialog.OK) {
								receiver =
									NoPoUtil.loadAsIdentifiable((Kontakt) ksel.getSelection(),
										IContact.class).orElse(null);
							}
						}
						if (receiver != null) {
							try {
								BestellBlatt bb =
									(BestellBlatt) getViewSite().getPage().showView(BestellBlatt.ID,
										receiver.getId(), IWorkbenchPage.VIEW_CREATE);
								bb.createOrder(receiver, entries);
								entries.stream()
									.forEach(oe -> oe.setState(OrderEntryState.ORDERED));
								tv.refresh();
							} catch (Exception e) {
								LoggerFactory.getLogger(getClass()).error("Error printing order",
									e);
								MessageDialog.openError(getViewSite().getShell(), "Fehler",
									"Beim Druck der Bestellung an " + receiver.getLabel()
										+ " ist ein Fehler aufgetren.");
							}
						}
					}
				}
			}
		};
		sendAction = new Action(Messages.BestellView_SendOrder) {
			@Override
			public void run(){
				if (actOrder == null)
					return;
				
				// organise items in supplier and non-supplier lists
				List<IOrderEntry> orderableItems = new ArrayList<IOrderEntry>();
				List<IOrderEntry> noSupplierItems = new ArrayList<IOrderEntry>();
				for (IOrderEntry orderEntry : actOrder.getEntries()) {
					IContact supplier = orderEntry.getProvider();
					if (supplier != null) {
						orderableItems.add(orderEntry);
					} else {
						noSupplierItems.add(orderEntry);
					}
				}
				
				boolean runOrder = true;
				if (!noSupplierItems.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (IOrderEntry noSupItem : noSupplierItems) {
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
								
								sender.store(actOrder);
								sender.finalizeExport();
								SWTHelper.showInfo(Messages.BestellView_OrderSentCaption,
									Messages.BestellView_OrderSentBody);
								tv.refresh();
								
								orderableItems.stream()
									.forEach(oe -> oe.setState(OrderEntryState.ORDERED));
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
						setOrder((IOrder) dlg.getResult()[0]);
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
				if (actOrder != null) {
					List<IOrderEntry> toOrder = prepareOrderList(null);
					
					StringBuffer export = new StringBuffer();
					for (IOrderEntry orderEntry : toOrder) {
						String code = orderEntry.getArticle().getCode();
						int num = orderEntry.getAmount();
						String name = orderEntry.getArticle().getName();
						String line = code + ", " + num + ", " + name; //$NON-NLS-1$ //$NON-NLS-2$
						
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
				if (actOrder != null) {
					OrderImportDialog dialog =
						new OrderImportDialog(getSite().getShell(), actOrder);
					dialog.open();
					updateCheckIn();
				} else {
					SWTHelper.alert(Messages.BestellView_NoOrder, //$NON-NLS-1$
						Messages.BestellView_NoOrderLoaded); //$NON-NLS-1$
				}
			}
		};
	}
	
	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
	public void addItemsToOrder(List<IArticle> articlesToOrder){
		if (actOrder == null) {
			NeueBestellungDialog nbDlg = new NeueBestellungDialog(getViewSite().getShell(),
				Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
			if (nbDlg.open() == Dialog.OK) {
				setOrder(createOrder(nbDlg.getTitle()));
			} else {
				return;
			}
		}
		
		for (IArticle article : articlesToOrder) {
			// SINGLE SHOT ORDER
			IOrderEntry orderEntry = actOrder.addEntry(article, null, null, 1);
			CoreModelServiceHolder.get().save(orderEntry);
		}
		if (tv != null && !tv.getControl().isDisposed()) {
			tv.refresh();
		}
	}
	
	/**
	 * @return the current defined {@link IOrder} in this view
	 */
	public IOrder getOrder(){
		return actOrder;
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
	public static IContact resolveDefaultSupplier(String cfgSupplier, String selDialogTitle){
		IContact supplier = null;
		if (cfgSupplier != null && !cfgSupplier.isEmpty()) {
			supplier = CoreModelServiceHolder.get().load(cfgSupplier, IContact.class).orElse(null);
		}
		
		//warn that there is no supplier
		if (supplier == null) {
			MessageDialog.openWarning(UiDesk.getTopShell(), selDialogTitle,
				Messages.BestellView_CantOrderNoSupplier);
		}
		return supplier;
	}
}
