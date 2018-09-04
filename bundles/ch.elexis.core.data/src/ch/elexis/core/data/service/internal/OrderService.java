package ch.elexis.core.data.service.internal;

import java.util.List;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStoreToStringContribution;

@Component
public class OrderService implements IOrderService {
	
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry stockEntry){
		IArticle article = stockEntry.getArticle();
		if (article != null) {
			String[] articleStoreToStringParts = StoreToStringServiceHolder
				.getStoreToString(article)
				.split(IStoreToStringContribution.DOUBLECOLON);
			
			IQuery<IOrderEntry> query = CoreModelServiceHolder.get().getQuery(IOrderEntry.class);
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

	@Override
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order){
		int current = ise.getCurrentStock();
		int max = ise.getMaximumStock();
		if (max == 0) {
			max = ise.getMinimumStock();
		}
		int toOrder = max - current;
		
		return order.addEntry(ise.getArticle(), ise.getStock(), ise.getProvider(), toOrder);
	}
}
