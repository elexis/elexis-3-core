package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;

public interface IOrderService {
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry ise);

	/**
	 * Return list of all order entries associated with the specified stock.
	 * 
	 * @param stock
	 * @return
	 */
	public List<IOrderEntry> findOrderEntryForStock(IStock stock);

	/**
	 * Refill the stock for a given stock entry. That is, create an order entry for
	 * the configured stock level (min amount, max amount)
	 *
	 * @param ise
	 * @param order
	 * @return {@link IOrderEntry} or <code>null</code> if the order is not valid
	 *         (e.g. would require to order <= 0 pieces)
	 * @since 3.6 return null on invalid order amount
	 */
	public @Nullable IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order);
}
