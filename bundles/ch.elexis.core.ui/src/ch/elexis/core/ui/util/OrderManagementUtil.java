package ch.elexis.core.ui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
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
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.OrderManagementView;

public class OrderManagementUtil {

	private static final Logger logger = LoggerFactory.getLogger(OrderManagementUtil.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

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

		if (allDelivered)
			return Messages.OrderManagement_FullyDelivered;
		if (anyDelivered)
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
			logger.error("Error: Invalid partialDelivery value: " + partialDelivery, e);
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
		String COMMAND_ID = "ch.elexis.base.barcode.scanner.ListenerProcess";
		try {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
					.getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);

			Command scannerCommand = commandService.getCommand(COMMAND_ID);
			Boolean isActive = false;
			if (scannerCommand.getState("org.eclipse.jface.commands.ToggleState") != null) {
				isActive = (Boolean) scannerCommand.getState("org.eclipse.jface.commands.ToggleState").getValue();
			}

			if (!isActive) {
				handlerService.executeCommand(COMMAND_ID, null);
				OrderManagementView.setBarcodeScannerActivated(true);

			} else {
				OrderManagementView.setBarcodeScannerActivated(true);
			}
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Barcode-Scanner Fehler",
					"Barcode-Scanner konnte nicht aktiviert werden:\n" + e.getMessage());
		}
	}
}
