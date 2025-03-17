package ch.elexis.core.ui.mediorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;

public class MediorderPartUtil {

	public static String createMediorderEntryOutreachLabel(Object object) {
		if (object instanceof IStockEntry stockEntry) {
			Double resultDays = null;
			IPatient patient = stockEntry.getStock().getOwner().asIPatient();
			List<IPrescription> lMedication = patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
					EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));
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

	public static void removeStockEntry(IStockEntry entry, IModelService coreModelService,
			IContextService contextService, IStockService stockService) {
		if (entry.getCurrentStock() > 0) {
			String mandatorId = contextService.getActiveMandator().map(IMandator::getId).orElse(null);
			if (mandatorId == null) {
				return;
			}
			stockService.performSingleReturn(entry.getArticle(), entry.getCurrentStock(), mandatorId);
		}
		coreModelService.remove(entry);
		IStock stock = entry.getStock();
		if (stock.getStockEntries().isEmpty()) {
			coreModelService.remove(stock);
		}
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
			MediorderEntryState entryState = MediorderPartUtil.determineState(entry);

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
		if (hasInStock && allEnabledForPea)
			return 1;
		if (hasInStock && hasOtherStatus)
			return 2;
		if (allEnabledForPea)
			return 0;
		return 3;
	}

	public static void updateStockImageState(Map<IStock, Integer> imageStockStates, IStock stock) {
		int state = MediorderPartUtil.calculateStockState(stock);
		imageStockStates.put(stock, state);
	}

	public static int getImageForStock(Map<IStock, Integer> imageStockStates, IStock stock) {
		return imageStockStates.computeIfAbsent(stock, MediorderPartUtil::calculateStockState);
	}

	/**
	 * Filters the list of all available patient stocks based on the current filter
	 * values. The filtering process is performed by calculating the stock state for
	 * each {@link IStock} and comparing it to the current filter values.
	 * 
	 * @return
	 */
	public static List<IStock> calculateFilteredStocks(List<Integer> filterValues) {
		Map<IStock, Integer> map = new HashMap<>();

		List<IStock> stocks = StockServiceHolder.get().getAllPatientStock();
		for (IStock stock : stocks) {
			map.computeIfAbsent(stock, MediorderPartUtil::calculateStockState);
		}

		List<IStock> filteredList = new ArrayList<>();
		for (Map.Entry<IStock, Integer> entry : map.entrySet()) {
			if (entry.getValue() != null && filterValues.contains(entry.getValue())) {
				filteredList.add(entry.getKey());
			}
		}
		return filteredList;
	}

	/**
	 * transfers the given entry from the default stock to the patient stock if
	 * entry is {@link MediorderEntryState#REQUESTED} and enough items are available
	 * 
	 * @param entry
	 * @param stockService
	 * @param coreModelService
	 * @param contextService
	 */
	public static void automaticallyFromDefaultStock(IStockEntry entry, IStockService stockService,
			IModelService coreModelService, IContextService contextService) {
		IStockEntry defaultStockEntry = stockService.findStockEntryForArticleInStock(stockService.getDefaultStock(),
				entry.getArticle());
		if (defaultStockEntry == null || defaultStockEntry.getCurrentStock() == 0) {
			return;
		}
		if (MediorderEntryState.REQUESTED.equals(MediorderPartUtil.determineState(entry))) {
			int amount = Math.min(defaultStockEntry.getCurrentStock(), entry.getMinimumStock());
			useFromDefaultStock(entry, defaultStockEntry, amount, stockService, coreModelService, contextService);
		}
	}

	public static String[] createValuesArray(IStockEntry entry, IStockService stockService) {
		IStockEntry defaultStockEntry = stockService.findStockEntryForArticleInStock(stockService.getDefaultStock(),
				entry.getArticle());
		int maxValue = Math.min(
				(defaultStockEntry != null ? defaultStockEntry.getCurrentStock() : 0) + entry.getCurrentStock(),
				entry.getMaximumStock());

		List<String> values = new ArrayList<>();
		for (int i = 0; i <= maxValue; i++) {
			values.add(String.valueOf(i));
		}
		return values.toArray(new String[0]);
	}

	/**
	 * updating the stock entry by adjusting its current stock based on the given
	 * amount
	 * 
	 * @param entry
	 * @param article
	 * @param amount
	 * @param stockService
	 * @param coreModelService
	 * @param contextService
	 */
	public static void useFromDefaultStock(IStockEntry entry, IStockEntry article, int amount,
			IStockService stockService, IModelService coreModelService, IContextService contextService) {
		String mandatorId = contextService.getActiveMandator().map(IMandator::getId).orElse(null);
		if (mandatorId == null) {
			return;
		}

		int difference = amount - entry.getCurrentStock();
		if (difference > 0) {
			stockService.performSingleDisposal(article.getArticle(), difference, mandatorId);
		} else if (difference < 0) {
			difference *= -1;
			stockService.performSingleReturn(article.getArticle(), difference, mandatorId);
		}

		entry.setCurrentStock(amount);
		coreModelService.save(article);
		coreModelService.save(entry);
	}
}