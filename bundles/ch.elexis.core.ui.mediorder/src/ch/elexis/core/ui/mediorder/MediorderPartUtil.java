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

	public static MediorderEntryState determineState(IStockEntry stockEntry) {
		int maximumStock = stockEntry.getMaximumStock();
		int minimumStock = stockEntry.getMinimumStock();
		int currentStock = stockEntry.getCurrentStock();

		MediorderEntryState state = MediorderEntryState.INVALID;
		if (maximumStock == 0 && minimumStock == 0 && currentStock == 0) {
			state = MediorderEntryState.ENABLED_FOR_PEA;
		}
		if (maximumStock > 0 && minimumStock > 0 && currentStock > 0) {
			state = currentStock == minimumStock ? MediorderEntryState.IN_STOCK
					: MediorderEntryState.PARTIALLY_IN_STOCK;
		} else if (maximumStock > 0 && minimumStock > 0 && currentStock == 0) {
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
				state = minimumStock == maximumStock ? MediorderEntryState.REQUESTED
						: MediorderEntryState.PARTIALLY_REQUESTED;
			}
		} else if (maximumStock == 0 && minimumStock > 0 && currentStock == 0) {
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
