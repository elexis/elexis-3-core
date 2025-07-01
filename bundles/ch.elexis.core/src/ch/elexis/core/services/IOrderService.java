package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
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
	 * Adds the specified article quantities to existing open entries within the
	 * provided orders, or creates new entries in the given order if no matching
	 * open entry is found.
	 * <p>
	 * For each article in {@code entriesToAdd}, the method attempts to find an open
	 * {@link IOrderEntry} with the same article in {@code existingOrders}. If
	 * found, the quantity is increased. If not, a new entry is created in
	 * {@code createOrder}.
	 * <p>
	 * If a {@code mandator} is provided, it is used to determine the preferred
	 * stock entry when creating a new order entry.
	 *
	 * @param existingOrders the list of existing orders to search for matching open
	 *                       entries
	 * @param createOrder    the order in which new entries are created when no
	 *                       matching open entry exists
	 * @param entriesToAdd   a map of articles and their quantities to be added
	 * @param mandator       the active mandator, used to resolve preferred stock
	 *                       entries (may be {@code null})
	 */
	public void addOrCreateOrderEntries(List<IOrder> existingOrders, IOrder createOrder,
			Map<IArticle, Integer> entriesToAdd, IMandator mandator);

	/**
	 * Finds all orders that were modified on the given date and contain at least
	 * one entry with the state {@code OPEN} or {@code ORDERED}.
	 * <p>
	 * The filtering is based on the {@code lastupdate} timestamp and the state of
	 * the associated order entries.
	 *
	 * @param date the date to search for (from start to end of day, inclusive)
	 * @return list of {@link IOrder} objects that contain open or ordered entries
	 *         and were modified on the given date
	 */
	public List<IOrder> findOpenOrdersByDate(LocalDate date);

	/**
	 * Calculates the differences between the consumed articles and the already
	 * ordered ones for a specific date.
	 * <p>
	 * This method internally combines:
	 * <ul>
	 * <li>{@link #calculateDailyConsumption(LocalDate, List)}</li>
	 * <li>{@link #findOpenOrdersByDate(LocalDate)}</li>
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

	/**
	 * Checks whether the given order contains at least one entry assigned to the
	 * specified supplier.
	 * <p>
	 * This method iterates over all entries in the given order and compares the
	 * supplier (provider) of each entry with the given {@code supplier}. If at
	 * least one match is found, the method returns {@code true}.
	 * </p>
	 *
	 * @param order    the order to check (may be {@code null})
	 * @param supplier the supplier to look for (may be {@code null})
	 * @return {@code true} if the order contains at least one entry with the given
	 *         supplier; {@code false} otherwise
	 */
	boolean containsSupplier(IOrder order, IContact supplier);

}
