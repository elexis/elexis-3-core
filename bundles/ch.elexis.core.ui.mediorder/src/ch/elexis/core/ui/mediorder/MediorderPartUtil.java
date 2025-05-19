package ch.elexis.core.ui.mediorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.mediorder.Constants;
import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.MedicationServiceHolder;
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
			return MediorderUtil.determineState(stockEntry).getLocaleText();
		}
		return "?";
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

	public static void updateStockImageState(Map<IStock, Integer> imageStockStates, IStock stock) {
		int state = MediorderUtil.calculateStockState(stock);
		imageStockStates.put(stock, state);
	}

	public static int getImageForStock(Map<IStock, Integer> imageStockStates, IStock stock) {
		return imageStockStates.computeIfAbsent(stock, MediorderUtil::calculateStockState);
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
			map.computeIfAbsent(stock, MediorderUtil::calculateStockState);
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
		if (MediorderEntryState.REQUESTED.equals(MediorderUtil.determineState(entry))) {
			int amount = Math.min(defaultStockEntry.getCurrentStock(), entry.getMinimumStock());
			useFromDefaultStock(entry, defaultStockEntry, amount, stockService, coreModelService, contextService);
		}
	}

	protected static String[] createValuesArray(IStockEntry entry, IStockService stockService) {
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

	public static void removeMailSticker(IModelService coreModelService, IStockService stockService,
			IStickerService stickerService, IPatient patient) {
		if (stickerService.hasSticker(patient,
				coreModelService.load(Constants.MEDIORDER_MAIL_STICKER_ID, ISticker.class).get())) {
			Optional<IStock> stock = stockService.getPatientStock(patient);
			if (stock.isEmpty()
					|| MediorderUtil.calculateStockState(stockService.getPatientStock(patient).get()) != 1) {
				stickerService.removeSticker(
						coreModelService.load(Constants.MEDIORDER_MAIL_STICKER_ID, ISticker.class).get(), patient);
			}
		}
	}
}