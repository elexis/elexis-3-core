package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;

public interface IOrderService {

	/**
	 * Return open order entry for the given stock entry.
	 * 
	 * @param ise stock entry
	 * @return {@link IOrderEntry} or <code>null</code> if not found
	 */
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

	/**
	 * Calculate the number of consumed articles (billables) on a given date.
	 * 
	 * @param date      the selected date
	 * @param mandators optional list of mandators to limit the query
	 * @return a map of article to total consumed amount
	 */
	public Map<IArticle, Integer> calculateDailyConsumption(LocalDate date, List<IMandator> mandators);

	/**
	 * Reduce open order entries for the given article if it was over-ordered.
	 * 
	 * @param order    the current order
	 * @param article  the article to reduce
	 * @param reduceBy amount to reduce
	 */
	public void reduceOpenEntries(List<IOrder> order, IArticle article, int reduceBy);

	/**
	 * Creates and saves order entries for the given articles and amounts.
	 *
	 * @param order        the order to which entries should be added
	 * @param entriesToAdd map of article to amount
	 * @param mandator     the active mandator (for finding preferred stock)
	 */
	public void createOrderEntries(List<IOrder> existingOrders, IOrder fallbackOrder,
			Map<IArticle, Integer> entriesToAdd, IMandator mandator);

	/**
	 * Find all orders that were modified on the given date.
	 *
	 * @param date the day to search for
	 * @return list of {@link IOrder} objects with changes on the given date
	 */
	public List<IOrder> findOrderByDate(LocalDate date);

	/**
	 * Calculates the differences between the consumed articles and the already
	 * ordered ones for a specific date.
	 * <p>
	 * This method internally combines:
	 * <ul>
	 * <li>{@link #calculateDailyConsumption(LocalDate, List)}</li>
	 * <li>{@link #findOrderByDate(LocalDate)}</li>
	 * <li>Difference logic based on open and ordered entries</li>
	 * </ul>
	 *
	 * @param date      the target date
	 * @param mandators optional list of mandators to filter the calculation
	 * @return map of article to difference amount (positive if needs ordering,
	 *         negative if over-ordered)
	 */
	public Map<IArticle, Integer> calculateDailyDifferences(LocalDate date, List<IMandator> mandators);

	/**
	 * Returns the order history service used by this order service.
	 *
	 * @return instance of IOrderHistoryService
	 */
	public IOrderHistoryService getHistoryService();

}
