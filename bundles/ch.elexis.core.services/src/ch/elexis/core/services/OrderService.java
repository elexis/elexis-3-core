package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
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

		IQuery<IOrderEntry> entryQuery = modelService.getQuery(IOrderEntry.class);
		entryQuery.and(ModelPackage.Literals.IORDER_ENTRY__STATE, COMPARATOR.IN,
				List.of(OrderEntryState.OPEN.ordinal(), OrderEntryState.ORDERED.ordinal()));
		entryQuery.and("lastupdate", COMPARATOR.GREATER_OR_EQUAL, startMillis);
		entryQuery.and("lastupdate", COMPARATOR.LESS_OR_EQUAL, endMillis);

		List<IOrderEntry> matchingEntries = entryQuery.execute();
		return matchingEntries.stream().map(IOrderEntry::getOrder).filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());
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

}