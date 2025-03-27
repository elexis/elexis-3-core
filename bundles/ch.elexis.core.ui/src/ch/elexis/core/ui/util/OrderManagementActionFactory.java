package ch.elexis.core.ui.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.dialogs.DailyOrderDialog;
import ch.elexis.core.ui.dialogs.HistoryDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.dnd.DropReceiver;
import ch.elexis.core.ui.views.BestellBlatt;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.OrderManagementView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Bestellung;
import ch.rgw.tools.ExHandler;

public class OrderManagementActionFactory {
	private static final Logger logger = LoggerFactory.getLogger(OrderManagementActionFactory.class);
	private final OrderManagementView view;

	// Action-Felder:
	private Action dailyWizardAction;
	private Action wizardAction;
	private Action newAction;
	private Action printAction;
	private Action exportClipboardAction;
	private final OrderHistoryManager orderHistoryManager = new OrderHistoryManager();

	private IOrder actOrder;

	public OrderManagementActionFactory(OrderManagementView view, IOrder actOrder) {
		this.view = Objects.requireNonNull(view);
		this.actOrder = actOrder;
	}

	public void initActions() {

		dailyWizardAction = createAction(Messages.BestellView_AutomaticDailyOrder,
				Images.IMG_WIZ_DAY.getImageDescriptor(), Messages.BestellView_CreateAutomaticDailyOrder,
				IAction.AS_PUSH_BUTTON, () -> handleDailyOrder());

		wizardAction = createAction(Messages.BestellView_AutomaticOrder, Images.IMG_WIZARD.getImageDescriptor(),
				Messages.BestellView_CreateAutomaticOrder, IAction.AS_PUSH_BUTTON,
				() -> handleAutomaticOrder());

		newAction = createAction(Messages.BestellView_CreateNewOrder, Images.IMG_NEW.getImageDescriptor(),
				Messages.BestellView_CreateNewOrder, IAction.AS_PUSH_BUTTON, () -> createNewOrder());

		printAction = createAction(Messages.BestellView_PrintOrder, Images.IMG_PRINTER.getImageDescriptor(),
				Messages.BestellView_PrintOrder, IAction.AS_PUSH_BUTTON, () -> printOrder());

		exportClipboardAction = createAction(Messages.BestellView_copyToClipboard,
				Images.IMG_CLIPBOARD.getImageDescriptor(), Messages.BestellView_copyToClipBioardForGalexis,
				IAction.AS_PUSH_BUTTON, () -> copyOrderToClipboard());
	}

	private Action createAction(String text, ImageDescriptor imageDescriptor, String toolTipText, int style,
			Runnable runMethod) {
		Action action = new Action(text, style) {
			@Override
			public void run() {
				runMethod.run();
			}
		};
		if (imageDescriptor != null) {
			action.setImageDescriptor(imageDescriptor);
		}
		action.setToolTipText(toolTipText);
		return action;
	}

	public Action getDailyWizardAction() {
		return dailyWizardAction;
	}

	public Action getWizardAction() {
		return wizardAction;
	}

	public Action getNewAction() {
		return newAction;
	}

	public Action getPrintAction() {
		return printAction;
	}

	public Action getExportClipboardAction() {
		return exportClipboardAction;
	}

	private void handleDailyOrder() {
		if (actOrder == null) {
			IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
			order.setTimestamp(LocalDateTime.now());
			order.setName(Messages.BestellView_AutomaticDaily);
			CoreModelServiceHolder.get().save(order);
			orderHistoryManager.logCreateOrder(order);
			actOrder = order;
		} else {
			if (!actOrder.getTimestamp().toLocalDate().equals(LocalDate.now())) {
				if (MessageDialog.openQuestion(view.getSite().getShell(), Messages.Core_Areas,
						Messages.BestellView_WizardAskNewOrder)) {
					actOrder = OrderManagementUtil.createOrder(Messages.Core_Automatic);
				}
			}
		}

		if (actOrder == null) {
			logger.warn("No active order found. Aborting."); //$NON-NLS-1$
			return;
		}

		DailyOrderDialog doDlg = new DailyOrderDialog(view.getSite().getShell(), actOrder);
		doDlg.open();

		for (IOrderEntry entry : actOrder.getEntries()) {
			orderHistoryManager.logCreateEntry(actOrder, entry, entry.getAmount());
		}

		view.loadOpenOrders();
		view.loadCompletedOrders();
		view.updateCheckIn();
		view.loadOrderDetails(actOrder);
		view.updateOrderDetails(actOrder);
	}

	private void handleAutomaticOrder() {
		if (actOrder == null) {
			actOrder = OrderManagementUtil.createOrder(Messages.Core_Automatic);
		} else {
			if (!actOrder.getTimestamp().toLocalDate().equals(LocalDate.now())) {
				if (MessageDialog.openQuestion(view.getSite().getShell(), Messages.Core_Areas,
						Messages.BestellView_WizardAskNewOrder)) {
					actOrder = OrderManagementUtil.createOrder(Messages.Core_Automatic);
				}
			}
		}

		int trigger = ConfigServiceHolder.get().get(
				ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER,
				ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
		boolean isInventoryBelow = trigger == ch.elexis.core.constants.Preferences.INVENTORY_ORDER_TRIGGER_BELOW;

		boolean excludeAlreadyOrderedItems = ConfigServiceHolder.get().get(
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);

		IQuery<IStockEntry> query = CoreModelServiceHolder.get().getQuery(IStockEntry.class);
		query.andFeatureCompare(ModelPackage.Literals.ISTOCK_ENTRY__CURRENT_STOCK,
				isInventoryBelow ? COMPARATOR.LESS : COMPARATOR.LESS_OR_EQUAL,
				ModelPackage.Literals.ISTOCK_ENTRY__MINIMUM_STOCK);
		List<IStockEntry> stockEntries = query.execute();
		for (IStockEntry stockEntry : stockEntries) {
			if (stockEntry.getArticle() != null) {
				if (excludeAlreadyOrderedItems) {
					IOrderEntry open = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
					if (open != null) {
						continue;
					}
				}
				OrderServiceHolder.get().addRefillForStockEntryToOrder(stockEntry, actOrder);
			} else {
				LoggerFactory.getLogger(getClass()).warn("Could not resolve article " + stockEntry.getLabel() //$NON-NLS-1$
						+ " of stock entry " + stockEntry.getId()); //$NON-NLS-1$
			}
		}
		for (IOrderEntry entry : actOrder.getEntries()) {
			orderHistoryManager.logCreateEntry(actOrder, entry, entry.getAmount());
		}
		view.loadOpenOrders();
		view.loadCompletedOrders();
		view.updateCheckIn();
		view.loadOrderDetails(actOrder);
		view.updateOrderDetails(actOrder);
	}

	private void createNewOrder() {
		NeueBestellungDialog nbDlg = new NeueBestellungDialog(view.getSite().getShell(),
				Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
		if (nbDlg.open() == Dialog.OK) {
			actOrder = OrderManagementUtil.createOrder(nbDlg.getTitle());
			view.loadOpenOrders();
			view.loadCompletedOrders();
			view.refresh();

		}
	}

	public void printOrder() {

		if (actOrder != null) {
			Map<IContact, List<IOrderEntry>> orderMap = prepareOrderMap();

			for (IContact receiver : orderMap.keySet()) {
				List<IOrderEntry> entries = orderMap.get(receiver);
				if (receiver == null) {
					receiver = ContactSelectionDialog.showInSync(IContact.class,
							ch.elexis.core.ui.text.Messages.TextContainer_SelectDestinationHeader,
							Messages.OrderManagement_NoSupplierRecipient);
				}
				if (receiver != null) {
					try {
						BestellBlatt bb = (BestellBlatt) view.getViewSite().getPage().showView(BestellBlatt.ID,
								receiver.getId(), IWorkbenchPage.VIEW_CREATE);
						bb.createOrder(receiver, entries);
						entries.forEach(oe -> {
							oe.setState(OrderEntryState.ORDERED);
							CoreModelServiceHolder.get().save(oe);
						});
						orderHistoryManager.logOrderSent(actOrder, false);
						view.loadOpenOrders();
					} catch (Exception e) {
						logger.error("Error printing order", e); //$NON-NLS-1$
						MessageDialog.openError(view.getViewSite().getShell(), Messages.Core_Error,
								MessageFormat.format(Messages.OrderManagement_PrintError, receiver.getLabel()));

					}
				}
			}
		}
	}

	private Map<IContact, List<IOrderEntry>> prepareOrderMap() {
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

	public IOrder setOrder(IOrder actOrder) {
		this.actOrder = actOrder;
		return this.actOrder;
	}

	public void sendOrder() {
		if (actOrder == null) {
			logger.warn("No active order to send."); //$NON-NLS-1$
			return;
		}

		List<IOrderEntry> orderableItems = new ArrayList<>();
		List<IOrderEntry> noSupplierItems = new ArrayList<>();

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
				sb.append(noSupItem.getArticle().getLabel()).append("\n"); //$NON-NLS-1$
			}

			runOrder = SWTHelper.askYesNo(ch.elexis.core.ui.views.Messages.BestellView_NoSupplierArticle, MessageFormat
					.format(ch.elexis.core.ui.views.Messages.BestellView_NoSupplierArticleMsg, sb.toString()));
		}

		if (runOrder) {
			List<IConfigurationElement> list = Extensions.getExtensions(ExtensionPointConstantsUi.TRANSPORTER);
			boolean handlerFound = false;

			for (IConfigurationElement ic : list) {
				String handler = ic.getAttribute("type"); //$NON-NLS-1$
				if (handler != null && handler.contains(Bestellung.class.getName())) {
					handlerFound = true;
					try {
						IDataSender sender = (IDataSender) ic
								.createExecutableExtension(ExtensionPointConstantsUi.TRANSPORTER_EXPC);
						try {
							sender.store(actOrder);
							sender.finalizeExport();
						} catch (XChangeException xe) {
							logger.error("Error saving or exporting the order: ", xe); //$NON-NLS-1$
							SWTHelper.showError(Messages.OrderManagement_ExportError_Title,
									Messages.OrderManagement_ExportError_Message);
							return;
						}
						SWTHelper.showInfo(ch.elexis.core.ui.views.Messages.BestellView_OrderSentCaption,
								ch.elexis.core.ui.views.Messages.BestellView_OrderSentBody);
						view.loadOrderDetails(actOrder);
						orderableItems.forEach(oe -> {
							if (oe.getState() == OrderEntryState.OPEN) {
								oe.setState(OrderEntryState.ORDERED);
								CoreModelServiceHolder.get().save(oe);
							}
						});
						orderHistoryManager.logOrderSent(actOrder, true);
						view.loadOpenOrders();
					} catch (CoreException ex) {
						ExHandler.handle(ex);
						logger.error("Error sending the order: ", ex); //$NON-NLS-1$
					}
				}
			}

			if (!handlerFound) {
				logger.warn(
						"No valid supplier plugin found! Please install one or contact support."); //$NON-NLS-1$
				SWTHelper.showError(Messages.OrderManagement_MissingSupplierPlugin_Title,
						Messages.OrderManagement_MissingSupplierPlugin_Message);
			}
		}
	}

	private void copyOrderToClipboard() {
		if (actOrder != null) {
			StringBuilder sb = new StringBuilder();
			for (IOrderEntry orderEntry : actOrder.getEntries()) {
				String code = orderEntry.getArticle().getCode();
				int num = orderEntry.getAmount();
				String name = orderEntry.getArticle().getName();
				sb.append(code).append(", ").append(num).append(", ").append(name).append(System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String clipboardText = sb.toString();
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			TextTransfer textTransfer = TextTransfer.getInstance();
			clipboard.setContents(new Object[] { clipboardText }, new Transfer[] { textTransfer });
			clipboard.dispose();
		}
	}

	public void createContextMenu(TableViewer table, TableViewer orderTable) {
		Menu menu = new Menu(table.getTable());

		MenuItem removeItem = new MenuItem(menu, SWT.NONE);
		removeItem.setImage(Images.IMG_CLEAR.getImage());
		removeItem.setText(Messages.BestellView_RemoveArticle);
		removeItem.addListener(SWT.Selection, event -> handleRemoveItem());

		MenuItem editItem = new MenuItem(menu, SWT.NONE);
		editItem.setImage(Images.IMG_EDIT.getImage());
		editItem.setText(Messages.OrderManagement_EditItem);
		editItem.addListener(SWT.Selection, event -> handleEditItem());

		MenuItem addItem = new MenuItem(menu, SWT.NONE);
		addItem.setImage(Images.IMG_ADDITEM.getImage());
		addItem.setText(Messages.OrderManagement_AddItem);
		addItem.addListener(SWT.Selection, event -> handleAddItem());

		table.getTable().setMenu(menu);
		createOrderHistoryMenu(orderTable);
	}

	public void createOrderHistoryMenu(TableViewer orderTableViewer) {
		Menu menu = new Menu(orderTableViewer.getTable());
		MenuItem historyItem = new MenuItem(menu, SWT.NONE);
		historyItem.setImage(Images.IMG_INFO.getImage());
		historyItem.setText(Messages.OrderManagement_ShowOrderHistory);
		historyItem.addListener(SWT.Selection, event -> handleShowOrderHistory(orderTableViewer));
		orderTableViewer.getTable().setMenu(menu);
	}


	private void handleRemoveItem() {
		IStructuredSelection selection = (IStructuredSelection) view.tableViewer.getSelection();
		IOrderEntry entry = (IOrderEntry) selection.getFirstElement();
		if (entry != null && entry.getState() == OrderEntryState.OPEN) {
			IOrder order = entry.getOrder();
			orderHistoryManager.logRemove(order, entry);
			CoreModelServiceHolder.get().delete(entry);
			order.getEntries().remove(entry);

			if (order.getEntries().isEmpty()) {
				CoreModelServiceHolder.get().delete(order);
				actOrder = null;
			}

			Display.getDefault().asyncExec(() -> {
				view.tableViewer.refresh();
				if (actOrder != null) {
					view.updateOrderDetails(actOrder);
				} else {
					setOrder(null);
				}
			});
			view.loadOpenOrders();
			view.refresh();
		}
	}


	private void handleShowOrderHistory(TableViewer viewer) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object selectedElement = selection.getFirstElement();
		IOrder order = null;
		if (selectedElement instanceof IOrderEntry entry) {
			order = entry.getOrder();
		} else if (selectedElement instanceof IOrder) {
			order = (IOrder) selectedElement;
		}
		if (order != null) {
			new HistoryDialog(UiDesk.getTopShell(), order).open();
		}
	}


	public void handleAddItem() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(LeistungenView.ID);

			Display.getDefault().asyncExec(() -> {
				if (view.dropTarget == null) {
					view.dropTarget = new GenericObjectDropTarget("ArtikelDropTarget", view.tableViewer.getControl(), //$NON-NLS-1$
							new DropReceiver(view));
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(view.dropTarget);
				}
				view.dropTarget.registered(false);
			});

		} catch (Exception e) {
			logger.error("Error when dropping the article: ", e); //$NON-NLS-1$

		}
	}

	public void handleOrderSelection(IOrder order) {
		if (order != null) {
			view.setActOrder(order);
			view.refresh();
			if (order.getEntries().isEmpty()) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(LeistungenView.ID);
				} catch (Exception e) {
					logger.error("Error opening LeistungenView", e);
				}
			} else if (view.dropTarget != null) {
				view.dropTarget.registered(false);
			}
		}
	}


	public void handleCompletedOrderSelection(IOrder order) {
		if (order != null) {
			view.setActOrder(order);
			view.setShowDeliveredColumn(true);
			view.refresh();
		}
	}


	private void handleEditItem() {
		IStructuredSelection selection = (IStructuredSelection) view.tableViewer.getSelection();
		IOrderEntry entry = (IOrderEntry) selection.getFirstElement();
		if (entry != null && entry.getState() == OrderEntryState.OPEN) {
			editOrderEntry(entry, false);
		}
	}


//	public void handleTableDoubleClick() {
//	    IStructuredSelection selection = (IStructuredSelection) view.tableViewer.getSelection();
//	    IOrderEntry entry = (IOrderEntry) selection.getFirstElement();
//	    if (entry != null) {
//
//			int colIndex = view.determineEditableColumn(entry);
//	        if (colIndex >= 0) {
//	        	System.out.println();
//	            view.tableViewer.editElement(entry, colIndex);
//	        }
//	    }
//	}




	private void editOrderEntry(IOrderEntry entry, boolean isDoubleClick) {
		int editableColumn = view.determineEditableColumn(entry);
		if (editableColumn != -1) {
			view.tableViewer.editElement(entry, editableColumn);
		}
	}


	public void handleMouseWheelScroll(Event event, ScrolledComposite scrollComposite) {
		event.doit = false;
		int currentY = scrollComposite.getOrigin().y;
		int newY = currentY - (event.count * 10);
		int maxY = Math.max(0,
				scrollComposite.getContent().getBounds().height - scrollComposite.getClientArea().height);

		scrollComposite.setOrigin(scrollComposite.getOrigin().x, Math.max(0, Math.min(newY, maxY)));
	}

}
