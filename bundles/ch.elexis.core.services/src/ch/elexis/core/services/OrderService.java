package ch.elexis.core.services;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

@Component
public class OrderService implements IOrderService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry stockEntry) {
		IArticle article = stockEntry.getArticle();
		if (article != null) {
			String[] articleStoreToStringParts = StoreToStringServiceHolder.getStoreToString(article)
					.split(IStoreToStringContribution.DOUBLECOLON);

			IQuery<IOrderEntry> query = modelService.getQuery(IOrderEntry.class);
			query.and("stockid", COMPARATOR.EQUALS, stockEntry.getStock().getId());
			query.and("articleType", COMPARATOR.EQUALS, articleStoreToStringParts[0]);
			query.and("articleId", COMPARATOR.EQUALS, articleStoreToStringParts[1]);
			query.and("state", COMPARATOR.NOT_EQUALS, OrderEntryState.DONE.getValue());
			List<IOrderEntry> orderEntries = query.execute();
			if (!orderEntries.isEmpty()) {
				return orderEntries.get(0);
			}
		}
		return null;
	}

	public List<IOrderEntry> findOrderEntryForStock(IStock stock) {
		IQuery<IOrderEntry> query = modelService.getQuery(IOrderEntry.class);
		query.and("stockid", COMPARATOR.EQUALS, stock.getId());
		return query.execute();
	}

	@Override
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order) {
		int current = ise.getCurrentStock();
		int max = ise.getMaximumStock();
		if (max == 0) {
			max = ise.getMinimumStock();
		}
		int toOrder = max - current;
		if (toOrder > 0) {
			IOrderEntry orderEntry = order.addEntry(ise.getArticle(), ise.getStock(), ise.getProvider(), toOrder);
			modelService.save(orderEntry);
			return orderEntry;
		}
		return null;
	}
}
