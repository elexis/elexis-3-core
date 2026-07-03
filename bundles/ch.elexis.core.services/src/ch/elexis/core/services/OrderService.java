package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		return getOpenOrders(-1);
	}

	@Override
	public List<IOrder> getOpenOrders(int limit) {
		IQuery<IOrder> query = modelService.getQuery(IOrder.class);
		query.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
		List<IOrder> orders = query.execute();

		List<IOrder> open = new ArrayList<>();
		for (IOrder order : orders) {
			if (!isCompleted(order)) {
				open.add(order);
				if (limit > 0 && open.size() >= limit) {
					break;
				}
			}
		}
		return open;
	}

	@Override
	public List<IOrder> getCompletedOrders(boolean showAllYears) {
		return getOrders(true, showAllYears);
	}

	@Override
	public List<Integer> getOrderYears() {
		List<IOrder> orders = modelService.getQuery(IOrder.class).execute();
		return orders.stream().map(IOrder::getTimestamp).filter(Objects::nonNull).map(LocalDateTime::getYear)
				.distinct().sorted(Collections.reverseOrder()).collect(Collectors.toList());
	}

	@Override
	public List<IOrder> getCompletedOrdersForYear(int year) {
		List<IOrder> orders = modelService.getQuery(IOrder.class).execute();
		return orders.stream().filter(o -> o.getTimestamp() != null && o.getTimestamp().getYear() == year)
				.filter(this::isCompleted).sorted(this::compareTimestampsDesc).collect(Collectors.toList());
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

		return orders.stream().filter(order -> completed ? isCompleted(order) : !isCompleted(order))
				.collect(Collectors.toList());
	}

	private boolean isCompleted(IOrder order) {
		List<IOrderEntry> entries = order.getEntries();
		if (entries.isEmpty()) {
			return false;
		}
		return entries.stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
	}

	@Override
	public List<IOrder> searchOrders(String search, int limit) {
		if (search == null || search.isBlank()) {
			IQuery<IOrder> query = modelService.getQuery(IOrder.class);
			query.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
			if (limit > 0) {
				query.limit(limit);
			}
			return query.execute();
		}

		String trimmed = search.trim();
		DateQuery dateQuery = parseDateQuery(trimmed);
		if (dateQuery != null) {
			List<IOrder> all = modelService.getQuery(IOrder.class).execute();
			List<IOrder> filtered = all.stream().filter(o -> matchesDate(o, dateQuery))
					.sorted((a, b) -> compareTimestampsDesc(a, b)).collect(Collectors.toList());
			if (limit > 0 && filtered.size() > limit) {
				filtered = filtered.subList(0, limit);
			}
			return filtered;
		}

		IQuery<IOrder> query = modelService.getQuery(IOrder.class);
		query.and("id", COMPARATOR.LIKE, "%" + trimmed + "%", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		query.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
		if (limit > 0) {
			query.limit(limit);
		}
		return query.execute();
	}

	private int compareTimestampsDesc(IOrder a, IOrder b) {
		LocalDateTime ta = a.getTimestamp();
		LocalDateTime tb = b.getTimestamp();
		if (ta == null && tb == null) {
			return 0;
		}
		if (ta == null) {
			return 1;
		}
		if (tb == null) {
			return -1;
		}
		return tb.compareTo(ta);
	}

	private boolean matchesDate(IOrder order, DateQuery dateQuery) {
		LocalDateTime ts = order.getTimestamp();
		if (ts == null) {
			return false;
		}
		if (dateQuery.year != null && ts.getYear() != dateQuery.year) {
			return false;
		}
		if (dateQuery.month != null && ts.getMonthValue() != dateQuery.month) {
			return false;
		}
		if (dateQuery.day != null && ts.getDayOfMonth() != dateQuery.day) {
			return false;
		}
		return true;
	}

	private DateQuery parseDateQuery(String search) {
		boolean onlyDateChars = search.chars()
				.allMatch(c -> Character.isDigit(c) || c == '.' || c == '/' || c == '-' || Character.isWhitespace(c));
		if (!onlyDateChars) {
			return null;
		}
		boolean hasSeparator = search.matches(".*[./\\-\\s].*"); //$NON-NLS-1$
		List<String> tokens = new ArrayList<>();
		for (String p : search.split("[./\\-\\s]+")) { //$NON-NLS-1$
			if (!p.isEmpty() && p.chars().allMatch(Character::isDigit)) {
				tokens.add(p);
			}
		}
		if (tokens.isEmpty()) {
			return null;
		}
		if (!hasSeparator && !(tokens.size() == 1 && tokens.get(0).length() == 4)) {
			return null;
		}

		Integer year = null;
		List<Integer> dayMonth = new ArrayList<>();
		for (String token : tokens) {
			int value = Integer.parseInt(token);
			if (token.length() == 4 && year == null) {
				year = value;
			} else {
				dayMonth.add(value);
			}
		}
		Integer day = null;
		Integer month = null;
		if (dayMonth.size() == 1) {
			month = dayMonth.get(0);
		} else if (dayMonth.size() >= 2) {
			day = dayMonth.get(0);
			month = dayMonth.get(1);
		}
		if (year == null && month == null && day == null) {
			return null;
		}
		if ((month != null && (month < 1 || month > 12)) || (day != null && (day < 1 || day > 31))) {
			return null;
		}
		return new DateQuery(day, month, year);
	}

	private static final class DateQuery {
		final Integer day;
		final Integer month;
		final Integer year;

		DateQuery(Integer day, Integer month, Integer year) {
			this.day = day;
			this.month = month;
			this.year = year;
		}
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
		List<IOutputLog> result = query.execute();
		return result.isEmpty() ? null : result.get(0);
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