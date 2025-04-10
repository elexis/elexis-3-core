package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

@Component
public class OrderService implements IOrderService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	private IOrderHistoryService orderHistoryService = ch.elexis.core.utils.OsgiServiceUtil
			.getService(IOrderHistoryService.class).orElse(null);

	@Reference
	void setOrderHistoryService(IOrderHistoryService service) {
		this.orderHistoryService = service;
	}

	@Override
	public IOrderHistoryService getHistoryService() {
		return this.orderHistoryService;
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
			orderHistoryService.logChangedAmount(order, entry, 0, toOrder);
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
					orderHistoryService.logRemove(order, entry);
					modelService.delete(entry);
				} else {
					entry.setAmount(amount - reduceBy);
					modelService.save(entry);
					orderHistoryService.logChangedAmount(order, entry, amount, entry.getAmount());
					reduceBy = 0;
				}
			}
		}
	}

	@Override
	public void createOrderEntries(List<IOrder> existingOrders, IOrder fallbackOrder,
			Map<IArticle, Integer> entriesToAdd, @Nullable IMandator mandator) {
			orderHistoryService.logCreateOrder(fallbackOrder);
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
						oe.setAmount(oe.getAmount() + amountToAdd);
						modelService.save(oe);
						orderHistoryService.logChangedAmount(order, oe, old, oe.getAmount());
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
						? fallbackOrder.addEntry(se.getArticle(), se.getStock(), se.getProvider(), amountToAdd)
						: fallbackOrder.addEntry(article, null, null, amountToAdd);
				modelService.save(newEntry);
				orderHistoryService.logChangedAmount(fallbackOrder, newEntry, 0, amountToAdd);
			}
		}
		if (!fallbackOrder.getEntries().isEmpty()) {
			modelService.save(fallbackOrder);
			modelService.refresh(fallbackOrder, true);
		}
	}

	@Override
	public List<IOrder> findOrderByDate(LocalDate date) {

		long startMillis = date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
		long endMillis = date.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
				.toEpochMilli() - 1;
		IQuery<IOrder> query = modelService.getQuery(IOrder.class);
		query.and("lastupdate", COMPARATOR.GREATER_OR_EQUAL, startMillis);
		query.and("lastupdate", COMPARATOR.LESS_OR_EQUAL, endMillis);
		List<IOrder> allOrders = query.execute();
		List<IOrder> withOpenEntries = allOrders.stream()
				.filter(order -> order.getEntries().stream().anyMatch(entry -> {
					var state = entry.getState();
					return state == OrderEntryState.OPEN || state == OrderEntryState.ORDERED;
				})).collect(Collectors.toList());
		return withOpenEntries;
	}

	@Override
	public Map<IArticle, Integer> calculateDailyDifferences(LocalDate date, List<IMandator> mandators) {
		Map<IArticle, Integer> consumptionMap = calculateDailyConsumption(date, mandators);
		List<IOrder> relevantOrders = findOrderByDate(date);
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

}