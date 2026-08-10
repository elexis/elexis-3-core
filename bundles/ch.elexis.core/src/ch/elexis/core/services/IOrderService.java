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
import ch.elexis.core.model.IOutputLog;
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

	/**
	 * Retrieves a list of all currently open orders.
	 * 
	 * @return list of open {@link IOrder} objects
	 */
	List<IOrder> getOpenOrders();

	/**
	 * Retrieves a list of open orders up to the specified limit.
	 * <p>
	 * Similar to {@link #getOpenOrders()}, but stops the classification process
	 * as soon as the {@code limit} is reached. This is significantly faster for 
	 * large databases as it avoids loading items for all orders.
	 *
	 * @param limit maximum number of open orders to retrieve; <= 0 acts like
	 * {@link #getOpenOrders()} (returns all)
	 * @return list of the first open {@link IOrder} objects, sorted descending by last modification
	 */
	List<IOrder> getOpenOrders(int limit);

	/**
	 * Retrieves a list of completed orders.
	 *
	 * @param showAllYears if true, returns orders from all years. If false, limits
	 *                     the result to the last 2 years.
	 * @return list of completed {@link IOrder} objects
	 */
	List<IOrder> getCompletedOrders(boolean showAllYears);

	/**
	 * Retrieves all years in which orders exist (independent of their state),
	 * sorted in descending order.
	 * <p>
	 * This is a lightweight query that does not load order entries. It serves to 
	 * build the year sections for completed orders without classifying all orders upfront.
	 *
	 * @return descending sorted list of years containing orders
	 */
	List<Integer> getOrderYears();

	/**
	 * Retrieves the completed orders for a specific year.
	 * <p>
	 * Allows lazy loading of a year's content upon expansion, preventing the 
	 * classification of all years at once when opening the view.
	 *
	 * @param year the year to retrieve orders for
	 * @return list of completed {@link IOrder} objects for the specified year, newest first
	 */
	List<IOrder> getCompletedOrdersForYear(int year);

	/**
	 * Searches for orders directly in the database based on a search text.
	 * <p>
	 * Searches via {@code id LIKE %search%} (since name and timestamp are part of the ID) 
	 * without loading order entries. Returns a mix of open and completed matches, 
	 * sorted descending by last modification.
	 *
	 * @param search the search text (name or date, e.g., year "2025"); empty means no restriction
	 * @param limit  maximum number of results to return (<= 0 means unlimited)
	 * @return list of matching {@link IOrder} objects
	 */
	List<IOrder> searchOrders(String search, int limit);

	/**
	 * Saves a partial delivery for a specific order entry and updates the stock.
	 * 
	 * @param entry           the order entry to update
	 * @param partialDelivery the amount that was delivered
	 */
	void saveSingleDelivery(IOrderEntry entry, int partialDelivery);

	/**
	 * Iterates over the provided list of entries and saves all pending deliveries.
	 * 
	 * @param entries list of order entries to process
	 */
	void saveAllDeliveries(List<IOrderEntry> entries);

	/**
	 * Adds a list of articles to an existing order. If an article is already
	 * present, its quantity is increased.
	 * 
	 * @param actOrder        the active order to add items to
	 * @param articlesToOrder list of articles to add
	 * @param mandator        the active mandator to determine the default stock, or
	 *                        null
	 * @return the updated {@link IOrder}
	 */
	IOrder addItemsToExistingOrder(IOrder actOrder, List<IArticle> articlesToOrder, @Nullable IMandator mandator);

	/**
	 * Retrieves the output log entry associated with a specific order.
	 * 
	 * @param order the order to fetch the log for
	 * @return the {@link IOutputLog} entry, or null if none exists
	 */
	IOutputLog getOrderLogEntry(IOrder order);

	/**
	 * Updates the current stock for a given article based on a delivery.
	 * 
	 * @param stock       the stock to update
	 * @param entry       the order entry containing the article
	 * @param amountToAdd the quantity to add to the stock
	 */
	void updateStockEntry(IStock stock, IOrderEntry entry, int amountToAdd);

	/**
	 * Checks if all entries within an order have the state DONE.
	 * 
	 * @param order the order to check
	 * @return true if completely delivered, false otherwise
	 */
	boolean isOrderCompletelyDelivered(IOrder order);
}
