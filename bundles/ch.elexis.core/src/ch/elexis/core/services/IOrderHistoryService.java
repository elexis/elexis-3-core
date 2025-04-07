package ch.elexis.core.services;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;

/**
 * Service interface for managing the history of orders and order entries.
 * Provides methods for logging various actions such as creation, editing,
 * delivery, deletion, and supplier assignment.
 *
 * Implementations should persist changes using {@link IOutputLog}.
 */
public interface IOrderHistoryService {

	/**
	 * Log the creation of a new order.
	 *
	 * @param order the newly created order
	 */
	void logCreateOrder(IOrder order);

	/**
	 * Log changes to an order entry's quantity.
	 *
	 * @param order    the parent order
	 * @param entry    the modified entry
	 * @param oldValue previous quantity
	 * @param newValue new quantity
	 */
	void logEdit(IOrder order, IOrderEntry entry, int oldValue, int newValue);

	/**
	 * Log the delivery of items for a given entry.
	 *
	 * @param order           the order being delivered
	 * @param entry           the delivered entry
	 * @param deliveredAmount amount delivered
	 * @param orderAmount     originally ordered amount
	 */
	void logDelivery(IOrder order, IOrderEntry entry, int deliveredAmount, int orderAmount);

	/**
	 * Log the creation of a new entry in the order.
	 *
	 * @param order    the order
	 * @param entry    the new entry
	 * @param quantity the ordered quantity
	 */
	void logCreateEntry(IOrder order, IOrderEntry entry, int quantity);

	/**
	 * Log that the order was submitted to a supplier.
	 *
	 * @param order the order being submitted
	 */
	void logOrder(IOrder order);

	/**
	 * Log that the order was deleted.
	 *
	 * @param order the deleted order
	 */
	void logDelete(IOrder order);

	/**
	 * Log a change in the amount of an article (increase, decrease, added).
	 *
	 * @param order     the order
	 * @param entry     the entry that changed
	 * @param oldAmount previous amount
	 * @param newAmount new amount
	 */
	void logChangedAmount(IOrder order, IOrderEntry entry, int oldAmount, int newAmount);

	/**
	 * Log that the order has been completely delivered and is now closed.
	 *
	 * @param order the completed order
	 */
	void logCompleteDelivery(IOrder order);

	/**
	 * Log that an entry has been removed from the order.
	 *
	 * @param order the order
	 * @param entry the removed entry
	 */
	void logRemove(IOrder order, IOrderEntry entry);

	/**
	 * Log that the order has been either printed or sent.
	 *
	 * @param order the order
	 * @param sent  true if sent, false if printed
	 */
	void logOrderSent(IOrder order, boolean sent);

	/**
	 * Log that a supplier was assigned to an entry.
	 *
	 * @param order    the order
	 * @param entry    the entry with supplier
	 * @param supplier the supplier's name
	 */
	void logSupplierAdded(IOrder order, IOrderEntry entry, String supplier);
}
