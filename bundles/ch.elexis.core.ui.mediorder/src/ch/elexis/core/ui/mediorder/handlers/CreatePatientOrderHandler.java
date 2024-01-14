package ch.elexis.core.ui.mediorder.handlers;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;

public class CreatePatientOrderHandler {

	@Execute
	public void execute() {
		IOrder order = CoreModelServiceHolder.get().create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName("Patientenbestellung");
		CoreModelServiceHolder.get().save(order);

		boolean excludeAlreadyOrderedItems = ConfigServiceHolder.get().get(
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);

		IQuery<IStock> queryStock = CoreModelServiceHolder.get().getQuery(IStock.class);
		queryStock.and("id", COMPARATOR.LIKE, "PatientStock-%");
		List<IStock> lPatientStocks = queryStock.execute();

		IQuery<IStockEntry> query = CoreModelServiceHolder.get().getQuery(IStockEntry.class);
		for (IStock stock : lPatientStocks) {
			query.and(ModelPackage.Literals.IORDER_ENTRY__STOCK, COMPARATOR.EQUALS, stock);
		}
		List<IStockEntry> stockEntries = query.execute();

		for (IStockEntry stockEntry : stockEntries) {
			if (stockEntry.getArticle() != null) {
				if (excludeAlreadyOrderedItems) {
					IOrderEntry open = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
					if (open != null) {
						continue;
					}
				}
				OrderServiceHolder.get().addRefillForStockEntryToOrder(stockEntry, order);
			} else {
				LoggerFactory.getLogger(getClass()).warn("Could not resolve article " + stockEntry.getLabel() //$NON-NLS-1$
						+ " of stock entry " + stockEntry.getId()); //$NON-NLS-1$
			}
		}
	}
}
