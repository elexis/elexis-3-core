package ch.elexis.core.ui.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
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
import ch.elexis.core.services.IOrderHistoryService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class OrderManagementUtil {

	private static final Logger logger = LoggerFactory.getLogger(OrderManagementUtil.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

	private static final Image SHOPPING_CART = Images.IMG_SHOPPING_CART.getImage(ImageSize._75x66_TitleDialogIconSize);
	private static final Image DELIVERY_TRUCK = Images.IMG_DELIVERY_TRUCK
			.getImage(ImageSize._75x66_TitleDialogIconSize);
	private static final Image TICK_IMAGE = Images.IMG_TICK.getImage();
	private static final Image SHOPPING = Images.IMG_SHOPPING_CART_WHITE.getImage(ImageSize._75x66_TitleDialogIconSize);

	private static final IOrderHistoryService orderHistoryManager = ch.elexis.core.utils.OsgiServiceUtil
			.getService(IOrderHistoryService.class).orElse(null);

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

	public static IOrder createOrder(String name) {
		IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName(name);
		CoreModelServiceHolder.get().save(order);
		orderHistoryManager.logCreateOrder(order);
		return order;
	}

	public static Image getStatusIcon(IOrder order) {
		boolean isDone = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean isPartial = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allOrdered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.ORDERED);
		boolean isShoping = order.getEntries().stream()
				.allMatch(e -> e.getState() == OrderEntryState.DONE && e.getOrder().getEntries().isEmpty());
		boolean anyDelivered = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.DONE);

		if (isShoping)
			return SHOPPING;
		if (anyDelivered)
			return DELIVERY_TRUCK;
		if (isPartial || allOrdered)
			return DELIVERY_TRUCK;

		if (isDone)
			return TICK_IMAGE;
		return SHOPPING_CART;
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

	public static void saveSingleDelivery(IOrderEntry entry, int partialDelivery) {
		if (entry == null || partialDelivery <= 0) {
			return;
		}

		try {
			int orderAmount = entry.getAmount();
			int newDelivered = entry.getDelivered() + partialDelivery;

			IStock stock = entry.getStock();
			if (stock != null) {
				updateStockEntry(stock, entry, partialDelivery);
			}
			orderHistoryManager.logDelivery(entry.getOrder(), entry, newDelivered, orderAmount);
			entry.setDelivered(newDelivered);
			entry.setState(newDelivered >= entry.getAmount() ? OrderEntryState.DONE : OrderEntryState.PARTIAL_DELIVER);
			CoreModelServiceHolder.get().save(entry);
			IOrder order = entry.getOrder();
			boolean allDelivered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
			if (allDelivered) {
				orderHistoryManager.logCompleteDelivery(order);
			}

		} catch (NumberFormatException e) {
			logger.error("Error: Invalid partialDelivery value: " + partialDelivery, e);
		}
	}


	public static void saveAllDeliveries(List<IOrderEntry> entries) {
		for (IOrderEntry entry : entries) {

			int partialDelivery = entry.getAmount() - entry.getDelivered();
			if (partialDelivery > 0) {
				saveSingleDelivery(entry, partialDelivery);
			}
		}
	}

	public static IOrder addItemsToOrder(IOrder actOrder, List<IArticle> articlesToOrder, Shell shell) {
		if (actOrder == null) {
			NeueBestellungDialog nbDlg = new NeueBestellungDialog(shell,
					ch.elexis.core.ui.views.Messages.BestellView_CreateNewOrder,
					ch.elexis.core.ui.views.Messages.BestellView_EnterOrderTitle);
			if (nbDlg.open() == Dialog.OK) {
				actOrder = createOrder(nbDlg.getTitle());
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

				orderHistoryManager.logEdit(actOrder, orderEntry, oldQuantity, newQuantity);
			} else {

				String mandatorId = ContextServiceHolder.get().getActiveMandator().map(IMandator::getId).orElse(null);
				IStock stock = StockServiceHolder.get().getMandatorDefaultStock(mandatorId);
				IOrderEntry newOrderEntry = actOrder.addEntry(article, stock, null, quantity);
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
			se.setCurrentStock(se.getCurrentStock() + amountToAdd);
			CoreModelServiceHolder.get().save(se);

		} else {
			IStockEntry newStockEntry = CoreModelServiceHolder.get().create(IStockEntry.class);
			newStockEntry.setArticle(entry.getArticle());
			newStockEntry.setStock(stock);
			newStockEntry.setCurrentStock(amountToAdd);
			CoreModelServiceHolder.get().save(newStockEntry);
		}
	}
}
