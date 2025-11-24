package ch.elexis.core.ui.util;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.constants.OrderConstants;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.OrderManagementView;

public class OrderManagementUtil {

	private static final Logger logger = LoggerFactory.getLogger(OrderManagementUtil.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$
	public static final String BarcodeScanner_COMPORT = "barcode/Symbol/port"; //$NON-NLS-1$
	public static List<IOrder> getOpenOrders() {
		return getOrders(false, true);
	}

	public static List<IOrder> getCompletedOrders(boolean showAllYears) {
		return getOrders(true, showAllYears);
	}

	private static List<IOrder> getOrders(boolean completed, boolean showAllYears) {
		IQuery<IOrder> query = CoreModelServiceHolder.get().getQuery(IOrder.class);
		List<IOrder> orders = query.execute();
		if (!showAllYears) {
			LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);
			orders = orders.stream().filter(order -> {
				LocalDateTime orderTimestamp = order.getTimestamp();
				return orderTimestamp != null && orderTimestamp.isAfter(twoYearsAgo);
			}).collect(Collectors.toList());
		}
		return orders.stream()
				.filter(order -> (completed && order.isDone() && !order.getEntries().isEmpty())
						|| (!completed && (!order.isDone() || order.getEntries().isEmpty())))
				.sorted((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp())).collect(Collectors.toList());
	}

	public static IOrder createOrder(String name, IOrderService orderService) {
		IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName(name);
		CoreModelServiceHolder.get().save(order);
		orderService.getHistoryService().logCreateOrder(order);
		return order;
	}

	public static Image getStatusIcon(IOrder order, boolean forTable) {
		boolean isDone = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean isPartial = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allOrdered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.ORDERED);
		boolean isShoping = order.getEntries().stream()
				.allMatch(e -> e.getState() == OrderEntryState.DONE && e.getOrder().getEntries().isEmpty());
		boolean anyDelivered = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.DONE);

		if (isShoping)
			return forTable ? OrderConstants.OrderImages.SHOPPING : OrderConstants.OrderImages.SHOPPING_64x64;
		if (isDone)
			return OrderConstants.OrderImages.THICK_CHECK;
		if (anyDelivered)
			return forTable ? OrderConstants.OrderImages.DELIVERY_TRUCK
					: OrderConstants.OrderImages.DELIVERY_TRUCK_64x64;
		if (isPartial || allOrdered)
			return forTable ? OrderConstants.OrderImages.DELIVERY_TRUCK
					: OrderConstants.OrderImages.DELIVERY_TRUCK_64x64;
		return forTable ? OrderConstants.OrderImages.SHOPPING_CART : OrderConstants.OrderImages.SHOPPING_CART_64x64;
	}

	public static String getStatusText(IOrder order) {
		if (order == null || order.getEntries().isEmpty()) {
			return Messages.OrderManagement_NoItems;
		}

		boolean allOrdered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.ORDERED);
		boolean allDelivered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean anyDelivered = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean isPartial = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.PARTIAL_DELIVER);
		if (allDelivered)
			return Messages.OrderManagement_FullyDelivered;
		if (anyDelivered)
			return Messages.OrderManagement_PartiallyDelivered;
		if (isPartial)
			return Messages.OrderManagement_PartiallyDelivered;
		if (allOrdered)
			return Messages.OrderManagement_Ordered;
		return Messages.OrderManagement_NotOrdered;
	}

	public static void saveSingleDelivery(IOrderEntry entry, int partialDelivery, IOrderService orderService) {
		if (entry == null || partialDelivery == 0) {
			return;
		}

		try {
			int orderAmount = entry.getAmount();
			int currentDelivered = entry.getDelivered();
			int newDelivered = currentDelivered + partialDelivery;

			if (newDelivered < 0) {
				newDelivered = 0;
			}

			IStock stock = entry.getStock();
			if (stock != null) {
				updateStockEntry(stock, entry, partialDelivery);
			}
			orderService.getHistoryService().logDelivery(entry.getOrder(), entry, newDelivered, orderAmount);
			entry.setDelivered(newDelivered);
			if (newDelivered >= entry.getAmount()) {
				entry.setState(OrderEntryState.DONE);
			} else if (newDelivered > 0) {
				entry.setState(OrderEntryState.PARTIAL_DELIVER);
			} else {
				entry.setState(OrderEntryState.ORDERED);
			}
			CoreModelServiceHolder.get().save(entry);
			IOrder order = entry.getOrder();
			boolean allDelivered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
			if (allDelivered) {
				orderService.getHistoryService().logCompleteDelivery(order);
			}

		} catch (NumberFormatException e) {
			logger.error("Error: Invalid partialDelivery value: " + partialDelivery, e); //$NON-NLS-1$
		}
	}


	public static void saveAllDeliveries(List<IOrderEntry> entries, IOrderService orderService) {
		for (IOrderEntry entry : entries) {

			int partialDelivery = entry.getAmount() - entry.getDelivered();
			if (partialDelivery > 0) {
				saveSingleDelivery(entry, partialDelivery, orderService);
			}
		}
	}

	public static IOrder addItemsToOrder(IOrder actOrder, List<IArticle> articlesToOrder, Shell shell,
			IOrderService orderService) {
		if (actOrder == null) {
			NeueBestellungDialog nbDlg = new NeueBestellungDialog(shell,
					ch.elexis.core.ui.views.Messages.BestellView_CreateNewOrder,
					ch.elexis.core.ui.views.Messages.BestellView_EnterOrderTitle);
			if (nbDlg.open() == Dialog.OK) {
				actOrder = createOrder(nbDlg.getTitle(), orderService);
			} else {
				return null;
			}
		}

		for (IArticle article : articlesToOrder) {
			int quantity = 1;

			Optional<IOrderEntry> existingEntry = actOrder.getEntries().stream()
					.filter(e -> e.getArticle().equals(article)).findFirst();

			if (existingEntry.isPresent()) {

				IOrderEntry orderEntry = existingEntry.get();
				int oldQuantity = orderEntry.getAmount();
				int newQuantity = oldQuantity + quantity;
				orderEntry.setAmount(newQuantity);
				CoreModelServiceHolder.get().save(orderEntry);

				orderService.getHistoryService().logEdit(actOrder, orderEntry, oldQuantity, newQuantity);
			} else {

				String mandatorId = ContextServiceHolder.get().getActiveMandator().map(IMandator::getId).orElse(null);
				IStock stock = StockServiceHolder.get().getMandatorDefaultStock(mandatorId);
				IOrderEntry newOrderEntry = actOrder.addEntry(article, stock, null, quantity);
				orderService.getHistoryService().logChangedAmount(actOrder, newOrderEntry, 0, quantity);
				CoreModelServiceHolder.get().save(newOrderEntry);
			}
		}
		return actOrder;
	}

	public static String formatDate(LocalDateTime dateTime) {
		return dateTime.format(FORMATTER);
	}

	public static IOrder getSelectedOrder(String orderId, boolean isCompleted) {
		IQuery<IOrder> query = CoreModelServiceHolder.get().getQuery(IOrder.class);
		return query.execute().stream()
				.filter(o -> o.getId().equals(orderId) && (o.isDone() == isCompleted || o.getEntries().isEmpty()))
				.findFirst().orElse(null);
	}

	public static Image getEntryStatusIcon(IOrderEntry entry) {
		if (entry == null)
			return null;

		int delivered = entry.getDelivered();
		int ordered = entry.getAmount();

		if (delivered == 0) {
			return Images.IMG_BULLET_RED.getImage();
		} else if (delivered < ordered) {
			return Images.IMG_BULLET_YELLOW.getImage();
		} else {
			return Images.IMG_BULLET_GREEN.getImage();
		}
	}

	public static IOutputLog getOrderLogEntry(IOrder order) {
		if (order == null) {
			return null;
		}
		IQuery<IOutputLog> query = CoreModelServiceHolder.get().getQuery(IOutputLog.class);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.EQUALS, order.getId());
		return query.execute().isEmpty() ? null : query.execute().get(0);
	}

	public static void updateStockEntry(IStock stock, IOrderEntry entry, int amountToAdd) {
		if (stock == null || entry == null || entry.getArticle() == null) {
			logger.error("Error: Invalid parameters in updateStockEntry()"); //$NON-NLS-1$
			return;
		}

		Optional<IStockEntry> existingStockEntry = stock.getStockEntries().stream()
				.filter(se -> se.getArticle().equals(entry.getArticle())).findFirst();

		if (existingStockEntry.isPresent()) {
			IStockEntry se = existingStockEntry.get();
			int current = se.getCurrentStock();
			int newStock = current + amountToAdd;
			if (newStock < 0) {
				newStock = 0;
			}
			se.setCurrentStock(newStock);
			CoreModelServiceHolder.get().save(se);
		} else {
			int startStock = Math.max(0, amountToAdd);
			IStockEntry newStockEntry = CoreModelServiceHolder.get().create(IStockEntry.class);
			newStockEntry.setArticle(entry.getArticle());
			newStockEntry.setStock(stock);
			newStockEntry.setCurrentStock(startStock);
			CoreModelServiceHolder.get().save(newStockEntry);
		}
	}

	public static void activateBarcodeScannerAndFocus() {
		String COMMAND_ID = "ch.elexis.base.barcode.scanner.ListenerProcess"; //$NON-NLS-1$
		try {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
					.getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);

			Command scannerCommand = commandService.getCommand(COMMAND_ID);
			Boolean isActive = false;
			if (scannerCommand.getState("org.eclipse.jface.commands.ToggleState") != null) { //$NON-NLS-1$
				isActive = (Boolean) scannerCommand.getState("org.eclipse.jface.commands.ToggleState").getValue(); //$NON-NLS-1$
			}

			if (!isActive) {
				handlerService.executeCommand(COMMAND_ID, null);
				OrderManagementView.setBarcodeScannerActivated(true);

			} else {
				OrderManagementView.setBarcodeScannerActivated(true);
			}
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Barcode-Scanner Fehler", //$NON-NLS-1$
					"Barcode-Scanner konnte nicht aktiviert werden:\n" + e.getMessage()); //$NON-NLS-1$
		}
	}

	public static void handleOrderButtonClick(OrderManagementView view, IOrderService orderService,
			Map<IOrderEntry, Integer> pendingDeliveredValues, IOrder actOrder) {

		Button orderButton = view.orderButton;
		String buttonText = orderButton.getText();

		if (buttonText.equals(ch.elexis.core.ui.views.Messages.OrderManagement_Button_Order)) {
			boolean confirm = MessageDialog.openQuestion(view.getSite().getShell(),
					ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Title,
					ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Message);
			if (confirm) {
				view.getActionFactory().sendOrder();
				view.loadOpenOrders();
			}
			view.refresh();
			return;
		}

		if (buttonText.equals(ch.elexis.core.ui.views.Messages.OrderManagement_Button_MissingSupplier)) {
			List<IContact> allowedSuppliers = loadConfiguredSuppliers();
			ContactSelectionDialog dialog = new ContactSelectionDialog(view.getSite().getShell(), IContact.class,
					ch.elexis.core.ui.views.Messages.OrderManagement_SelectSupplier_Title,
					ch.elexis.core.ui.views.Messages.OrderManagement_SelectSupplier_Message);
			dialog.setAllowedContacts(allowedSuppliers);
			if (dialog.open() == Dialog.OK) {
				IContact selectedProvider = (IContact) dialog.getSelection();
				if (selectedProvider != null && actOrder != null) {
					for (IOrderEntry entry : actOrder.getEntries()) {
						if (entry.getProvider() == null) {
							entry.setProvider(selectedProvider);
							orderService.getHistoryService().logSupplierAdded(actOrder, entry,
									selectedProvider.getLabel());
							CoreModelServiceHolder.get().save(entry);
						}
					}
					view.refreshTables();
				}
			}
			return;
		}

		if (buttonText.equals(ch.elexis.core.ui.views.Messages.OrderManagement_Button_Book)
				|| buttonText.equals(ch.elexis.core.ui.views.Messages.OmnivoreView_editActionCaption)) {
			if (view.isBarcodePortAvailable()) {
				activateBarcodeScannerAndFocus();
			}
			view.setDeliveryEditMode(true);
			setCheckboxColumnVisible(view, true);

			view.tableViewer.refresh();
			IOrderEntry first = view.findFirstEditableInViewerOrder();
			if (first != null) {
				view.tableViewer.setSelection(new StructuredSelection(first), true);
				view.tableViewer.reveal(first);
				view.tableViewer.editElement(first, OrderConstants.OrderTable.DELIVERED);
				orderButton.setText(ch.elexis.core.ui.views.Messages.MedicationComposite_btnConfirm);
				orderButton.setImage(Images.IMG_TICK.getImage());
			}
			return;
		}

		if (buttonText.equals(ch.elexis.core.ui.views.Messages.MedicationComposite_btnConfirm)) {
		    for (Map.Entry<IOrderEntry, Integer> entry : pendingDeliveredValues.entrySet()) {
		        IOrderEntry orderEntry = entry.getKey();
		        int currentDelivered = orderEntry.getDelivered();
		        int ordered = orderEntry.getAmount();
		        int part = entry.getValue();
		        int newTotal = currentDelivered + part;
		        if (newTotal > ordered) {
					String articleName = orderEntry.getArticle() != null ? orderEntry.getArticle().getLabel()
							: "Unbekannter Artikel";
		            boolean confirm = MessageDialog.openQuestion(
							view.getSite().getShell(),
							ch.elexis.core.ui.views.Messages.OrderManagement_Overdelivery_Title,
							MessageFormat.format(ch.elexis.core.ui.views.Messages.OrderManagement_Overdelivery_Message,
									currentDelivered, part, newTotal, ordered, articleName));
		            if (!confirm) {
		                continue;
		            }
		        }
		        if (newTotal < 0) {
		            MessageDialog.openError(
							view.getSite().getShell(), ch.elexis.core.ui.views.Messages.Cst_Text_ungueltiger_Wert,
							ch.elexis.core.ui.views.Messages.OrderManagement_Error_NegativeDeliveredAmount);
		            continue;
		        }

		        orderService.getHistoryService().logDelivery(orderEntry.getOrder(), orderEntry, part, ordered);
		        saveSingleDelivery(orderEntry, part, orderService);
		    }

		    if (view.isBarcodePortAvailable()) {
		        activateBarcodeScannerAndFocus();
		        OrderManagementView.setBarcodeScannerActivated(false);
		    }

		    pendingDeliveredValues.clear();
		    view.setDeliveryEditMode(false);
		    setCheckboxColumnVisible(view, false);

		    view.selectAllChk.setVisible(false);
		    view.selectAllChk.getParent().layout(true, true);

		    view.tableViewer.refresh();
			final boolean isCompletelyDelivered = isOrderCompletelyDelivered(actOrder);
			final String finishedOrderId = (actOrder != null) ? actOrder.getId() : null;
		    Display.getDefault().asyncExec(() -> {
		        view.loadOpenOrders();
		        view.loadCompletedOrders(view.getCompletedContainer());
				if (finishedOrderId != null) {
					IOrder reloaded = getSelectedOrder(finishedOrderId, isCompletelyDelivered);
					if (reloaded != null) {
						view.setActOrder(reloaded);
						view.selectOrderInHistory(reloaded);
						view.refresh();
					}
				}
				view.updateUI();
		    });

		    return;
		}
	}

	public static List<IContact> loadConfiguredSuppliers() {
		IQuery<IConfig> query = CoreModelServiceHolder.get().getQuery(IConfig.class);
		query.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.LIKE, "%/supplier%");
		List<IConfig> configs = query.execute();
		Set<IContact> result = new LinkedHashSet<>();
		for (IConfig cfg : configs) {
			String contactId = cfg.getValue();
			if (StringUtils.isBlank(contactId)) {
				continue;
			}
			CoreModelServiceHolder.get().load(contactId, IContact.class).ifPresent(result::add);
		}
		return new ArrayList<>(result);
	}

	public static void setCheckboxColumnVisible(OrderManagementView view, boolean visible) {
		Table table = view.checkboxViewer.getTable();
		if (table.isDisposed() || table.getColumnCount() == 0)
			return;
		view.selectAllChk.setVisible(visible);
		TableColumn checkboxCol = table.getColumn(OrderConstants.OrderTable.CHECKBOX);
		checkboxCol.setResizable(visible);
		checkboxCol.setMoveable(visible);
		checkboxCol.setWidth(visible ? 30 : 0);
	}

	public static boolean isOrderCompletelyDelivered(IOrder order) {
		if (order == null || order.getEntries().isEmpty()) {
			return false;
		}
		return order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
	}

}
