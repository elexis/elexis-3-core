package ch.elexis.core.ui.mediorder;

import java.util.List;

import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;

public class MediorderPartUtil {

	public static String createMediorderEntryOutreachLabel(Object object) {
		if (object instanceof IStockEntry stockEntry) {
			Double resultDays = null;
			IPatient patient = stockEntry.getStock().getOwner().asIPatient();
			List<IPrescription> lMedication = patient.getMedication(null);
			for (IPrescription prescription : lMedication) {
				if (prescription.getArticle().equals(stockEntry.getArticle())) {
					float dailyDosageAsFloat = MedicationServiceHolder.get().getDailyDosageAsFloat(prescription);
					int maximumStock = stockEntry.getMaximumStock();
					resultDays = Math
							.floor((stockEntry.getArticle().getPackageSize() * maximumStock) / dailyDosageAsFloat);
				}
			}
			return resultDays != null ? String.valueOf(resultDays.intValue()) + " Tage" : "";
		}
		return "?";
	}

	public static String createMediorderEntryStateLabel(Object object) {
		if (object instanceof IStockEntry stockEntry) {
			return determineState(stockEntry).getLocaleText();
		}
		return "?";
	}

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
			IOrderEntry order = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
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
					state = MediorderEntryState.INVALID;
				} else {
					state = MediorderEntryState.PARTIALLY_REQUESTED;
				}
			}
		} else if (minimumStock == 0 && maximumStock > 0 && currentStock == 0) {
			state = MediorderEntryState.AWAITING_REQUEST;
		}
		state.setStockEntry(stockEntry);
		return state;
	}
	
	public static void removeStockEntry(IStockEntry entry, IModelService coreModelService) {
		coreModelService.remove(entry);
		IStock stock = entry.getStock();
		if (stock.getStockEntries().isEmpty()) {
			coreModelService.remove(stock);
		}
	}

}
