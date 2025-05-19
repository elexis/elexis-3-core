package ch.elexis.core.mediorder;

import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class MediorderUtil {
	
	/**
	 * Determines the current state of a patient's stock entry
	 * 
	 * <p>
	 * The conditions considered include:
	 * </p>
	 * <ul>
	 * <li>Minimum: The number of items approved for ordering.</li>
	 * <li>Maximum: The maximum number of items authorized for ordering.</li>
	 * <li>Current: The number of items currently available in the patient's
	 * stock.</li>
	 * </ul>
	 * 
	 * <p>
	 * Possible return states:
	 * </p>
	 * <ul>
	 * <li>{@code INVALID}: If the stock configuration is invalid (e.g., minimum
	 * stock exceeds maximum stock, or current stock exceeds maximum stock).</li>
	 * <li>0/0/1 {@code AWAITING_REQUEST}: If the stock has no current items but is
	 * approved for ordering.</li>
	 * <li>1/1/0 {@code REQUESTED} / {@code ORDERED}: If the stock has been
	 * requested or ordered but is not yet received.</li>
	 * <li>2/5/0 {@code PARTIALLY_REQUESTED} / {@code PARTIALLY_ORDERED}: If part of
	 * the stock is requested or ordered.</li>
	 * <li>1/1/1 {@code IN_STOCK}: If the current stock equals the minimum
	 * stock.</li>
	 * <li>5/5/3 {@code PARTIALLY_IN_STOCK}:If part of the stock is available</li>
	 * </ul>
	 * 
	 * @param stockEntry
	 * @return a {@link MediorderEntryState} representing the current state of the
	 *         stock entry.
	 */
	public static MediorderEntryState determineState(IStockEntry stockEntry) {
		int minimumStock = stockEntry.getMinimumStock();
		int maximumStock = stockEntry.getMaximumStock();
		int currentStock = stockEntry.getCurrentStock();

		MediorderEntryState state = MediorderEntryState.INVALID;
		if (minimumStock > 0 && maximumStock > 0 && currentStock > 0) {
			if (currentStock > maximumStock) {
				state = MediorderEntryState.INVALID;
			} else if (currentStock == minimumStock) {
				state = MediorderEntryState.IN_STOCK;
			} else {
				state = MediorderEntryState.PARTIALLY_IN_STOCK;
			}
		} else if (minimumStock > 0 && maximumStock > 0 && currentStock == 0) {
			// determine if already ordered
			IOrderService orderService = OsgiServiceUtil.getService(IOrderService.class)
					.orElseThrow(() -> new IllegalStateException("no order service found"));
			IOrderEntry order = orderService.findOpenOrderEntryForStockEntry(stockEntry);
			OsgiServiceUtil.ungetService(orderService);
			if (order != null) {
				if (minimumStock == maximumStock) {
					state = MediorderEntryState.ORDERED;
				} else {
					state = MediorderEntryState.PARTIALLY_ORDERED;
				}
				state.setOrderEntry(order);

			} else {
				if (minimumStock == maximumStock) {
					state = MediorderEntryState.REQUESTED;
				} else if (minimumStock > maximumStock) {
					state = MediorderEntryState.PARTIALLY_REQUESTED;
				} else {
					state = MediorderEntryState.INVALID;
				}
			}
		} else if (minimumStock == 0 && maximumStock > 0 && currentStock == 0) {
			state = MediorderEntryState.AWAITING_REQUEST;
		}
		state.setStockEntry(stockEntry);
		return state;
	}

	/**
	 * Calculates the overall state of the order based on the status of individual
	 * {@link IStockEntry} objects in the provided {@link IStock}. The stock state
	 * is derived from the {@link MediorderEntryState} of each entry, and the method
	 * returns one of the following status codes:
	 * 
	 * <ul>
	 * <li>0 - ENABLED_FOR_PEA: All stock entries have the state
	 * {@code AWAITING_REQUEST}.</li>
	 * <li>1 - READY: All stock entries are in the state {@code IN_STOCK}</li>
	 * <li>2 - PARTIALLY_READY: Some stock entries are in the state
	 * {@code IN_STOCK}, while others may have different statuses.</li>
	 * <li>3 - IN_PROGRESS: Changes are still required, as not all entries are
	 * ready.</li>
	 * </ul>
	 * 
	 * @param
	 * @return
	 */
	public static int calculateStockState(IStock stock) {
		boolean allEnabledForPea = true;
		boolean hasInStock = false;
		boolean hasPartiallyInStock = false;
		boolean hasOtherStatus = false;

		for (IStockEntry entry : stock.getStockEntries()) {
			MediorderEntryState entryState = determineState(entry);

			switch (entryState) {
			case AWAITING_REQUEST -> {
			}
			case IN_STOCK -> hasInStock = true;
			case PARTIALLY_IN_STOCK -> {
				hasPartiallyInStock = true;
				allEnabledForPea = false;
			}
			case ORDERED, PARTIALLY_ORDERED, INVALID -> {
				allEnabledForPea = false;
				hasOtherStatus = true;
			}
			default -> {
				allEnabledForPea = false;
				hasOtherStatus = true;
			}
			}
		}

		if (hasPartiallyInStock)
			return 2;
		if (hasInStock && allEnabledForPea) {
			IModelService coreModelService = OsgiServiceUtil
					.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
					.orElseThrow(() -> new IllegalStateException("no model service found"));

			IStickerService stickerService = OsgiServiceUtil.getService(IStickerService.class)
					.orElseThrow(() -> new IllegalStateException("no stock service found"));
			try {
				boolean hasSticker = stickerService.hasSticker(stock.getOwner().asIPatient(),
						coreModelService.load(Constants.MEDIORDER_MAIL_STICKER_ID, ISticker.class)
								.orElseThrow(() -> new IllegalStateException("no mediorderMailSend sticker found")));
				return hasSticker ? 4 : 1;
			} finally {
				OsgiServiceUtil.ungetService(coreModelService);
				OsgiServiceUtil.ungetService(stickerService);
			}
		}
		if (hasInStock && hasOtherStatus)
			return 2;
		if (allEnabledForPea)
			return 0;
		return 3;
	}
}
