package ch.elexis.core.ui.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
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
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.dialogs.DailyConsumptionOrderDialog;
import ch.elexis.core.ui.dialogs.HistoryDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.dnd.OrderDropReceiver;
import ch.elexis.core.ui.views.BestellBlatt;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.OrderManagementView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Bestellung;
import ch.rgw.tools.ExHandler;

public class OrderManagementActionFactory {
	private static final Logger logger = LoggerFactory.getLogger(OrderManagementActionFactory.class);
	private final OrderManagementView view;

	private Action dailyWizardAction;
	private Action wizardAction;
	private Action newAction;
	private Action printAction;
	private Action exportClipboardAction;

	private IOrderService orderService;

	private IOrder actOrder;

	public OrderManagementActionFactory(OrderManagementView view, IOrder actOrder, IOrderService orderService) {
		this.view = Objects.requireNonNull(view);
		this.actOrder = actOrder;
		this.orderService = orderService;
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
		boolean reuseExistingOrder = isDailyOrder(actOrder) && isUnsent(actOrder)
				&& actOrder.getTimestamp().toLocalDate().equals(LocalDate.now());

		IOrder orderToUse;
		if (reuseExistingOrder) {
			orderToUse = actOrder;
		} else {
			orderToUse = CoreModelServiceHolder.get().create(IOrder.class);
			orderToUse.setTimestamp(LocalDateTime.now());
			orderToUse.setName(Messages.BestellView_AutomaticDaily);
		}

		DailyConsumptionOrderDialog doDlg = new DailyConsumptionOrderDialog(view.getSite().getShell(), orderToUse);
		int result = doDlg.open();

		if (result == Window.OK) {
			actOrder = orderToUse;
			view.setActOrder(actOrder);
			view.reload();
			view.updateCheckIn();
		}
	}

	private boolean isDailyOrder(IOrder order) {
		return order != null && Messages.BestellView_AutomaticDaily.equals(order.getName());
	}

	private boolean isUnsent(IOrder order) {
		if (order == null) {
			return false;
		}
		return order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.OPEN);
	}

	private boolean isStockOrder(IOrder order) {
		return order != null && Messages.OrderManagement_StockOrder_DefaultName.equals(order.getName());
	}

	private IOrder findReusableStockOrder() {
		for (IOrder o : OrderManagementUtil.getOpenOrders()) {
			if (isStockOrder(o) && isUnsent(o)) {
				return o;
			}
		}
		return null;
	}


	private void clearOpenEntries(IOrder order) {
		if (order == null) {
			return;
		}
		List<IOrderEntry> toRemove = new ArrayList<>();
		for (IOrderEntry entry : new ArrayList<>(order.getEntries())) {
			if (entry.getState() == OrderEntryState.OPEN) {
				toRemove.add(entry);
			}
		}
		for (IOrderEntry entry : toRemove) {
			order.getEntries().remove(entry);
			CoreModelServiceHolder.get().delete(entry);
		}
	}

	private void handleAutomaticOrder() {
		IOrder reusableStockOrder = findReusableStockOrder();
		if (reusableStockOrder != null) {
			actOrder = reusableStockOrder;
			clearOpenEntries(actOrder);
		} else {
			actOrder = OrderManagementUtil.createOrder(Messages.OrderManagement_StockOrder_DefaultName, orderService);
		}
		int trigger = ConfigServiceHolder.get().get(Preferences.INVENTORY_ORDER_TRIGGER,
				Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
		boolean isInventoryBelow = trigger == Preferences.INVENTORY_ORDER_TRIGGER_BELOW;

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

		if (actOrder != null) {
			view.setActOrder(actOrder);
			view.reload();
		}
		view.updateCheckIn();
	}

	private void createNewOrder() {
		NeueBestellungDialog nbDlg = new NeueBestellungDialog(view.getSite().getShell(),
				Messages.BestellView_CreateNewOrder, Messages.BestellView_EnterOrderTitle);
		if (nbDlg.open() == Dialog.OK) {
			actOrder = OrderManagementUtil.createOrder(nbDlg.getTitle(), orderService);
			view.setActOrder(actOrder);
			view.reload();
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
						orderService.getHistoryService().logOrderSent(actOrder, false);
						view.reload();
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

			runOrder = SWTHelper.askYesNo(Messages.BestellView_NoSupplierArticle,
					MessageFormat.format(Messages.BestellView_NoSupplierArticleMsg, sb.toString()));
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

						if (sender.canHandle(actOrder)) {
							try {
								sender.store(actOrder);
								sender.finalizeExport();
							} catch (XChangeException xe) {
								if ("ABORT_BY_USER".equals(xe.getMessage())) {
									continue;
								}
								logger.error("Error saving or exporting the order: ", xe);
								SWTHelper.showError(Messages.OrderManagement_ExportError_Title,
										Messages.OrderManagement_ExportError_Message);
								continue;
							}

							String pluginName = ic.getAttribute("name");
							if (pluginName == null || pluginName.isEmpty()) {
								pluginName = sender.getClass().getSimpleName();
							}

							Set<String> added = new HashSet<>();
							StringJoiner contactsJoiner = new StringJoiner(", ");

							for (IOrderEntry oe : orderableItems) {
								IContact provider = oe.getProvider();
								if (provider != null) {
									String label = provider.getLabel();
									if (added.add(label)) {
										contactsJoiner.add(label);
									}
								}
							}
							String joinedNames = contactsJoiner.toString();
							String contactNames = !joinedNames.isEmpty() ? joinedNames
									: Messages.OrderManagement_NoSupplierRecipient;
							String title = MessageFormat.format(Messages.BestellView_OrderSentWithPluginTitle,
									pluginName);
							String body = MessageFormat.format(
									Messages.BestellView_OrderSentWithPluginBody, actOrder.getName(), contactNames);
							SWTHelper.showInfo(title, body);
							view.refresh();
							orderService.getHistoryService().logOrderSent(actOrder, true);
							view.reload();
						}
					} catch (CoreException ex) {
						ExHandler.handle(ex);
						logger.error("Error sending the order: ", ex); //$NON-NLS-1$
					}
				}
			}

			if (!handlerFound) {
				logger.warn("No valid supplier plugin found! Please install one or contact support."); //$NON-NLS-1$
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
		if (table.getTable().getMenu() != null && !table.getTable().getMenu().isDisposed()) {
			table.getTable().getMenu().dispose();
		}
		Action removeAction = new Action(Messages.BestellView_RemoveArticle) {
			@Override
			public void run() {
				handleRemoveItem();
			}
		};
		removeAction.setImageDescriptor(Images.IMG_CLEAR.getImageDescriptor());
		Action editAction = new Action(Messages.OrderManagement_EditItem) {
			@Override
			public void run() {
				handleEditItem();
			}
		};
		editAction.setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
		Action addAction = new Action(Messages.OrderManagement_AddItem) {
			@Override
			public void run() {
				handleAddItem();
			}
		};
		addAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				boolean hasOrder = actOrder != null;
				boolean hasEntries = hasOrder && !actOrder.getEntries().isEmpty();
				IOrderEntry selectedEntry = null;
				IStructuredSelection sel = (IStructuredSelection) table.getSelection();
				if (sel != null && !sel.isEmpty() && sel.getFirstElement() instanceof IOrderEntry) {
					selectedEntry = (IOrderEntry) sel.getFirstElement();
				}
				boolean enableEditRemove = hasEntries && selectedEntry != null;
				removeAction.setEnabled(enableEditRemove);
				editAction.setEnabled(enableEditRemove);
				manager.add(removeAction);
				manager.add(editAction);
				manager.add(addAction);
			}
		});
		Menu menu = menuManager.createContextMenu(table.getTable());
		table.getTable().setMenu(menu);
		Table swtTable = table.getTable();
		swtTable.addListener(SWT.MenuDetect, ev -> {
			Point p = swtTable.toControl(ev.x, ev.y);
			TableItem item = swtTable.getItem(p);
			if (item == null) {
				table.setSelection(StructuredSelection.EMPTY, true); // true = reveal (optional)
			}
		});
		swtTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					handleRemoveItem();
				}
			}
		});
		createOrderHistoryMenu(orderTable);
	}

	public void createOrderHistoryMenu(TableViewer orderTableViewer) {
		Action historyAction = new Action(Messages.OrderManagement_ShowOrderHistory) {
			@Override
			public void run() {
				handleShowOrderHistory(orderTableViewer);
			}
		};
		historyAction.setImageDescriptor(Images.IMG_INFO.getImageDescriptor());
		Action deleteAction = new Action(Messages.OrderManagement_DeleteOrder) {
			@Override
			public void run() {
				handleDeleteOrder(orderTableViewer);
			}
		};
		deleteAction.setImageDescriptor(Images.IMG_CLEAR.getImageDescriptor());
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				deleteAction.setEnabled(false);

				IStructuredSelection selection = (IStructuredSelection) orderTableViewer.getSelection();
				Object first = selection.getFirstElement();
				if (first instanceof IOrder selectedOrder) {
					boolean allOpen = selectedOrder.getEntries().stream()
							.allMatch(entry -> entry.getState() == OrderEntryState.OPEN);
					deleteAction.setEnabled(allOpen);
				}
				manager.add(historyAction);
				manager.add(deleteAction);
			}
		});
		Menu menu = menuManager.createContextMenu(orderTableViewer.getTable());
		orderTableViewer.getTable().setMenu(menu);
	}

	private void handleDeleteOrder(TableViewer orderTableViewer) {
		IStructuredSelection selection = (IStructuredSelection) orderTableViewer.getSelection();
		IOrder selectedOrder = (IOrder) selection.getFirstElement();
		if (selectedOrder == null) {
			return;
		}

		String orderName = selectedOrder.getName() != null ? selectedOrder.getName() : Messages.UNKNOWN;
		String orderDate = OrderManagementUtil.formatDate(selectedOrder.getTimestamp());

		String title = Messages.OrderManagement_DeleteOrder_Title;
		String message = MessageFormat.format(Messages.OrderManagement_DeleteOrder_Message, orderName, orderDate);

		boolean confirm = MessageDialog.openQuestion(view.getSite().getShell(), title, message);

		if (!confirm) {
			return;
		}

		List<IOrderEntry> entriesToDelete = new ArrayList<>(selectedOrder.getEntries());
		for (IOrderEntry entry : entriesToDelete) {
			CoreModelServiceHolder.get().delete(entry);
		}
		selectedOrder.getEntries().clear();

		CoreModelServiceHolder.get().delete(selectedOrder);

		if (actOrder != null && actOrder.getId().equals(selectedOrder.getId())) {
			actOrder = null;
			view.setActOrder(null);
		}
		Display.getDefault().asyncExec(() -> {
			view.getTableViewer().setInput(Collections.emptyList());
			view.getTableViewer().refresh();
			view.reload();
			view.clearOrderDetailsView();
		});
	}

	private void handleRemoveItem() {
		IStructuredSelection selection = (IStructuredSelection) view.tableViewer.getSelection();
		if (selection == null || selection.isEmpty()) {
			return;
		}

		List<IOrderEntry> selectedEntries = new ArrayList<>();
		for (Object o : selection.toArray()) {
			if (o instanceof IOrderEntry) {
				selectedEntries.add((IOrderEntry) o);
			}
		}
		if (selectedEntries.isEmpty()) {
			return;
		}

		for (IOrderEntry entry : selectedEntries) {
			if (entry.getState() != OrderEntryState.OPEN) {
				continue; 
			}

			IOrder order = entry.getOrder();
			if (order == null) {
				continue;
			}

			orderService.getHistoryService().logRemove(order, entry);
			CoreModelServiceHolder.get().delete(entry);
			order.getEntries().remove(entry);
		}

		if (actOrder != null && actOrder.getEntries().isEmpty()) {
			CoreModelServiceHolder.get().delete(actOrder);
			actOrder = null;
			view.setActOrder(null);
		}

		Display.getDefault().asyncExec(() -> {
			view.getTableViewer().refresh();
			if (actOrder != null) {
				view.updateOrderDetails(actOrder);
			} else {
				setOrder(null);
			}
		});
		view.reload();
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
							new OrderDropReceiver(view, orderService));
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(view.dropTarget);
				}
				view.dropTarget.registered(false);
			});

		} catch (Exception e) {
			logger.error("Error when dropping the article: ", e); //$NON-NLS-1$

		}
	}

	public void handleCompletedOrderSelection(IOrder order) {
		if (order != null) {
			view.resetEditMode();
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