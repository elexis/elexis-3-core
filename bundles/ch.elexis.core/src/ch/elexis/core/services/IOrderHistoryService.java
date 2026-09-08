package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.OrderHistoryEntry;

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

	/**
	 * Log that a patient's medication order was billed.
	 *
	 * @param patient  the patient whose order was billed
	 * @param articles labels of the billed articles, may be empty
	 */
	void logMediorderBilled(IPatient patient, List<String> articles);

	/**
	 * Log that a patient picked up their medication order. This closes the current
	 * mediorder process.
	 *
	 * @param patient  the patient who picked up the order
	 * @param articles labels of the picked up articles, may be empty
	 */
	void logMediorderPickedUp(IPatient patient, List<String> articles);

	/**
	 * Log that an article was manually added to a patient's mediorder stock.
	 *
	 * @param patient the patient whose stock was extended
	 * @param article the added article
	 */
	void logMediorderArticleAdded(IPatient patient, IArticle article);

	/**
	 * Log that an article was manually removed from a patient's mediorder stock.
	 *
	 * @param patient the patient whose stock was reduced
	 * @param article the removed article
	 */
	void logMediorderArticleRemoved(IPatient patient, IArticle article);

	/**
	 * Log that one of the amounts of a patient's mediorder stock entry was changed.
	 * <p>
	 *
	 * @param patient     the patient whose stock entry changed
	 * @param article     the affected article
	 * @param amountLabel display label of the changed amount, e.g. the column title
	 *                    the user edited
	 * @param oldValue    value before the change
	 * @param newValue    value after the change
	 */
	void logMediorderAmountChanged(IPatient patient, IArticle article, String amountLabel, int oldValue, int newValue);

	/**
	 * Read all logged mediorder events of a patient, oldest first.
	 *
	 * @param patient the patient
	 * @return the logged entries, never <code>null</code>
	 */
	List<OrderHistoryEntry> getMediorderHistory(IPatient patient);
}
