package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Component
public class OrderService implements IOrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);
	private static final int RECENT_ORDERS_YEARS = 2;

	@Inject
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService modelService;

	@Override
	public IOrderHistoryService getHistoryService() {
		return new OrderHistoryService();
	}

	@Override
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry stockEntry) {
		IArticle article = stockEntry.getArticle();
		if (article != null) {
			String[] parts = StoreToStringServiceHolder.getStoreToString(article)
					.split(IStoreToStringContribution.DOUBLECOLON);
			IQuery<IOrderEntry> query = modelService.getQuery(IOrderEntry.class);
			query.and("stockid", COMPARATOR.EQUALS, stockEntry.getStock().getId());
			query.and("articleType", COMPARATOR.EQUALS, parts[0]);
			query.and("articleId", COMPARATOR.EQUALS, parts[1]);
			query.and("state", COMPARATOR.NOT_EQUALS, OrderEntryState.DONE.getValue());
			List<IOrderEntry> results = query.execute();
			if (!results.isEmpty()) {
				return results.get(0);
			}
		}
		return null;
	}

	@Override
	public List<IOrderEntry> findOrderEntryForStock(IStock stock) {
		IQuery<IOrderEntry> query = modelService.getQuery(IOrderEntry.class);
		query.and("stockid", COMPARATOR.EQUALS, stock.getId());
		return query.execute();
	}

	@Override
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order) {
		int current = ise.getCurrentStock();
		int max = ise.getMaximumStock();
		if (max == 0)
			max = ise.getMinimumStock();
		int toOrder = max - current;
		if (toOrder > 0) {
			IOrderEntry entry = order.addEntry(ise.getArticle(), ise.getStock(), ise.getProvider(), toOrder);
			modelService.save(entry);
			getHistoryService().logChangedAmount(order, entry, 0, toOrder);
			return entry;
		}
		return null;
	}

	@Override
	public Map<IArticle, Integer> calculateDailyConsumption(LocalDate date, List<IMandator> mandators) {
		Map<IArticle, Integer> result = new LinkedHashMap<>();
		IQuery<IEncounter> query = modelService.getQuery(IEncounter.class);
		query.and(ModelPackage.Literals.IENCOUNTER__DATE, COMPARATOR.EQUALS, date);
		if (mandators != null && !mandators.isEmpty()) {
			query.startGroup();
			for (IMandator m : mandators) {
				query.or(ModelPackage.Literals.IENCOUNTER__MANDATOR, COMPARATOR.EQUALS, m);
			}
			query.andJoinGroups();
		}
		for (IEncounter encounter : query.execute()) {
			for (IBilled billed : encounter.getBilled()) {
				IBillable billable = billed.getBillable();
				if (billable instanceof IArticle art) {
					int amount = (int) billed.getAmount();
					result.merge(art, amount, Integer::sum);
				}
			}
		}
		return result;
	}

	@Override
	public void reduceOpenEntries(List<IOrder> orders, IArticle article, int reduceBy) {
		for (IOrder order : orders) {
			List<IOrderEntry> openEntries = order.getEntries().stream().filter(e -> e.getArticle().equals(article))
					.filter(e -> e.getState() == OrderEntryState.OPEN).collect(Collectors.toList());
			for (IOrderEntry entry : openEntries) {
				if (reduceBy <= 0)
					return;
				int amount = entry.getAmount();
				if (amount <= reduceBy) {
					reduceBy -= amount;
					getHistoryService().logRemove(order, entry);
					modelService.delete(entry);
				} else {
					entry.setAmount(amount - reduceBy);
					modelService.save(entry);
					getHistoryService().logChangedAmount(order, entry, amount, entry.getAmount());
					reduceBy = 0;
				}
			}
		}
	}

	@Override
	public void addOrCreateOrderEntries(List<IOrder> existingOrders, IOrder createOrder,
			Map<IArticle, Integer> entriesToAdd, @Nullable IMandator mandator) {
		getHistoryService().logCreateOrder(createOrder);
		for (Map.Entry<IArticle, Integer> entry : entriesToAdd.entrySet()) {
			if (entry.getValue() <= 0)
				continue;
			IArticle article = entry.getKey();
			int amountToAdd = entry.getValue();
			boolean appended = false;
			for (IOrder order : existingOrders) {
				for (IOrderEntry oe : order.getEntries()) {
					if (oe.getArticle().equals(article) && oe.getState() == OrderEntryState.OPEN) {
						int old = oe.getAmount();
						oe.setAmount(old + amountToAdd);
						modelService.save(oe);
						getHistoryService().logChangedAmount(order, oe, old, oe.getAmount());
						appended = true;
						break;
					}
				}
				if (appended)
					break;
			}
			if (!appended) {
				IStockEntry se = null;
				if (mandator != null) {
					String articleStr = StoreToStringServiceHolder.getStoreToString(article);
					se = StockServiceHolder.get().findPreferredStockEntryForArticle(articleStr, mandator.getId());
				}
				IOrderEntry newEntry = (se != null)
						? createOrder.addEntry(se.getArticle(), se.getStock(), se.getProvider(), amountToAdd)
						: createOrder.addEntry(article, null, null, amountToAdd);
				modelService.save(newEntry);
				getHistoryService().logChangedAmount(createOrder, newEntry, 0, amountToAdd);
			}
		}
		if (!createOrder.getEntries().isEmpty()) {
			modelService.save(createOrder);
			modelService.refresh(createOrder, true);
		}
	}

	@Override
	public List<IOrder> findOpenOrdersByDate(LocalDate date) {
		long startMillis = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		long endMillis = date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
		IQuery<IOrder> orderQuery = modelService.getQuery(IOrder.class);
		orderQuery.and(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, COMPARATOR.GREATER_OR_EQUAL, startMillis);
		orderQuery.and(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, COMPARATOR.LESS_OR_EQUAL, endMillis);
		List<IOrder> ordersInRange = orderQuery.execute();
		return ordersInRange.stream().filter(this::hasOpenOrOrderedEntries)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if the given order contains any entries that are either OPEN or
	 * ORDERED.
	 *
	 * @param order The order to check
	 * @return true if open or ordered entries exist, false otherwise
	 */
	private boolean hasOpenOrOrderedEntries(IOrder order) {
		if (order == null || order.getEntries() == null) {
			return false;
		}

		return order.getEntries().stream().anyMatch(
				entry -> entry.getState() == OrderEntryState.OPEN || entry.getState() == OrderEntryState.ORDERED);
	}

	@Override
	public Map<IArticle, Integer> calculateDailyDifferences(LocalDate date, List<IMandator> mandators) {
		Map<IArticle, Integer> consumptionMap = calculateDailyConsumption(date, mandators);
		List<IOrder> relevantOrders = findOpenOrdersByDate(date);
		Map<IArticle, Integer> orderedArticles = new HashMap<>();
		for (IOrder order : relevantOrders) {
			for (IOrderEntry entry : order.getEntries()) {
				if (entry.getState() == OrderEntryState.OPEN || entry.getState() == OrderEntryState.ORDERED) {
					orderedArticles.merge(entry.getArticle(), entry.getAmount(), Integer::sum);
				}
			}
		}
		Map<IArticle, Integer> differences = new LinkedHashMap<>();
		for (Map.Entry<IArticle, Integer> entry : consumptionMap.entrySet()) {
			int alreadyOrdered = orderedArticles.getOrDefault(entry.getKey(), 0);
			int diff = entry.getValue() - alreadyOrdered;
			differences.put(entry.getKey(), diff);
		}
		return differences;
	}

	@Override
	public boolean containsSupplier(IOrder order, IContact supplier) {
		if (order == null || supplier == null) {
			return false;
		}

		String supplierId = supplier.getId();
		for (IOrderEntry entry : order.getEntries()) {
			IContact provider = entry.getProvider();
			if (provider != null && supplierId.equals(provider.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<IOrder> getOpenOrders() {
		return getOrders(false, true);
	}

	@Override
	public List<IOrder> getCompletedOrders(boolean showAllYears) {
		return getOrders(true, showAllYears);
	}

	private List<IOrder> getOrders(boolean completed, boolean showAllYears) {
		IQuery<IOrder> query = modelService.getQuery(IOrder.class);
		if (!showAllYears) {
			LocalDateTime timeThreshold = LocalDateTime.now().minusYears(RECENT_ORDERS_YEARS);
			long thresholdMillis = timeThreshold.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			query.and(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, COMPARATOR.GREATER_OR_EQUAL, thresholdMillis);
		}
		query.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
		List<IOrder> orders = query.execute();
		return orders.stream()
				.filter(order -> matchesCompletionState(order, completed))
				.collect(Collectors.toList());
	}

	private boolean matchesCompletionState(IOrder order, boolean completed) {
		boolean isDone = order.isDone();
		boolean hasEntries = !order.getEntries().isEmpty();
		if (completed) {
			return isDone && hasEntries;
		}
		return !isDone || !hasEntries;
	}

	@Override
	public IOrder createOrder(String name) {
		IOrder order = modelService.create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName(name);
		modelService.save(order);
		getHistoryService().logCreateOrder(order);
		return order;
	}

	@Override
	public void saveSingleDelivery(IOrderEntry entry, int partialDelivery) {
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
			getHistoryService().logDelivery(entry.getOrder(), entry, newDelivered, orderAmount);
			entry.setDelivered(newDelivered);

			if (newDelivered >= entry.getAmount()) {
				entry.setState(OrderEntryState.DONE);
			} else if (newDelivered > 0) {
				entry.setState(OrderEntryState.PARTIAL_DELIVER);
			} else {
				entry.setState(OrderEntryState.ORDERED);
			}
			modelService.save(entry);

			IOrder order = entry.getOrder();
			boolean allDelivered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
			if (allDelivered) {
				getHistoryService().logCompleteDelivery(order);
			}

		} catch (RuntimeException e) {
			log.error("Failed to save partial delivery. Value: {}", partialDelivery, e);
		}
	}

	@Override
	public void saveAllDeliveries(List<IOrderEntry> entries) {
		for (IOrderEntry entry : entries) {
			int partialDelivery = entry.getAmount() - entry.getDelivered();
			if (partialDelivery > 0) {
				saveSingleDelivery(entry, partialDelivery);
			}
		}
	}

	@Override
	public IOrder addItemsToExistingOrder(IOrder actOrder, List<IArticle> articlesToOrder,
			@Nullable IMandator mandator) {
		if (actOrder == null) {
			return null;
		}

		String mandatorId = (mandator != null) ? mandator.getId() : null;
		IStock currentStock = StockServiceHolder.get().getMandatorDefaultStock(mandatorId);

		for (IArticle article : articlesToOrder) {
			int quantity = 1;

			Optional<IOrderEntry> existingEntry = actOrder.getEntries().stream().filter(
					e -> e.getArticle().equals(article) && e.getStock() != null && e.getStock().equals(currentStock))
					.findFirst();

			if (existingEntry.isPresent()) {
				IOrderEntry orderEntry = existingEntry.get();
				int oldQuantity = orderEntry.getAmount();
				int newQuantity = oldQuantity + quantity;
				orderEntry.setAmount(newQuantity);
				modelService.save(orderEntry);

				getHistoryService().logEdit(actOrder, orderEntry, oldQuantity, newQuantity);
			} else {
				IOrderEntry newOrderEntry = actOrder.addEntry(article, currentStock, null, quantity);
				getHistoryService().logChangedAmount(actOrder, newOrderEntry, 0, quantity);
				modelService.save(newOrderEntry);
			}
		}
		return actOrder;
	}

	@Override
	public IOutputLog getOrderLogEntry(IOrder order) {
		if (order == null) {
			return null;
		}
		IQuery<IOutputLog> query = modelService.getQuery(IOutputLog.class);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.EQUALS, order.getId());
		return query.execute().isEmpty() ? null : query.execute().get(0);
	}

	@Override
	public void updateStockEntry(IStock stock, IOrderEntry entry, int amountToAdd) {
		if (stock == null || entry == null || entry.getArticle() == null) {
			log.error("Failed to update stock entry. Invalid parameters provided.");
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
			modelService.save(se);
		} else {
			int startStock = Math.max(0, amountToAdd);
			IStockEntry newStockEntry = modelService.create(IStockEntry.class);
			newStockEntry.setArticle(entry.getArticle());
			newStockEntry.setStock(stock);
			newStockEntry.setCurrentStock(startStock);
			modelService.save(newStockEntry);
		}
	}

	@Override
	public boolean isOrderCompletelyDelivered(IOrder order) {
		if (order == null || order.getEntries().isEmpty()) {
			return false;
		}
		return order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
	}

}