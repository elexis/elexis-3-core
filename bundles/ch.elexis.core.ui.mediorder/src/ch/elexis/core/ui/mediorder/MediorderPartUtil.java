package ch.elexis.core.ui.mediorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;

public class MediorderPartUtil {

	public static Optional<IPatient> getPatient(IStockEntry entry) {
		return entry != null ? getPatient(entry.getStock()) : Optional.empty();
	}

	public static Optional<IPatient> getPatient(IStock stock) {
		IPerson owner = stock != null ? stock.getOwner() : null;
		if (owner == null || !owner.isPatient()) {
			return Optional.empty();
		}
		return Optional.ofNullable(owner.asIPatient());
	}

	public static String createMediorderEntryOutreachLabel(Object object) {
		if (object instanceof IStockEntry stockEntry) {
			Double resultDays = null;
			List<IPrescription> lMedication = getPatient(stockEntry)
					.map(patient -> patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
							EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION)))
					.orElseGet(Collections::emptyList);
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

	public static void logPickedUp(IOrderService orderService, List<IStockEntry> entries) {
		forEachPatient(entries,
				(patient, articles) -> orderService.getHistoryService().logMediorderPickedUp(patient, articles));
	}

	private static void forEachPatient(List<IStockEntry> entries, BiConsumer<IPatient, List<String>> consumer) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		Map<IPatient, List<String>> articlesByPatient = new LinkedHashMap<>();
		for (IStockEntry entry : entries) {
			IPatient patient = getPatient(entry).orElse(null);
			if (patient == null) {
				continue;
			}
			String label = entry.getArticle() != null ? entry.getArticle().getLabel() : null;
			List<String> articles = articlesByPatient.computeIfAbsent(patient, p -> new ArrayList<>());
			if (StringUtils.isNotBlank(label)) {
				articles.add(label);
			}
		}
		articlesByPatient.forEach(consumer);
	}

	public static void removeStockEntry(IStockEntry entry, IModelService coreModelService,
			IContextService contextService, IStockService stockService, IOrderService orderService) {
		if (entry.getCurrentStock() > 0) {
			String mandatorId = contextService.getActiveMandator().map(IMandator::getId).orElse(null);
			if (mandatorId == null) {
				return;
			}
			stockService.performSingleReturn(entry.getArticle(), entry.getCurrentStock(), mandatorId);
		}
		IStock stock = entry.getStock();
		IPatient patient = getPatient(stock).orElse(null);
		IArticle article = entry.getArticle();

		coreModelService.remove(entry);
		if (patient != null) {
			orderService.getHistoryService().logMediorderArticleRemoved(patient, article);
		}
		if (stock != null && stock.getStockEntries().isEmpty()) {
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
	 * Commissioning systems are excluded, as their current stock is managed by the
	 * system itself.
	 */
	private static List<IStock> getSourceStocks(IStockService stockService, IContextService contextService) {
		List<IStock> stocks = new ArrayList<>();
		String mandatorId = contextService.getActiveMandatorId();
		if (mandatorId != null) {
			stockService.getAllStocks(false, false).stream()
					.filter(stock -> stock.getOwner() != null && mandatorId.equals(stock.getOwner().getId()))
					.forEach(stocks::add);
		}
		IStock defaultStock = stockService.getDefaultStock();
		if (!defaultStock.isCommissioningSystem() && !stocks.contains(defaultStock)) {
			stocks.add(defaultStock);
		}
		return stocks;
	}

	/**
	 * @return the first stock entry containing the amount, if there is none the
	 *         first stock entry of the article even if it contains less,
	 *         <code>null</code> if the article is not stored in any of the stocks
	 */
	public static IStockEntry findSourceStockEntry(IArticle article, int amount, IStockService stockService,
			IContextService contextService) {
		IStockEntry fallback = null;
		for (IStock stock : getSourceStocks(stockService, contextService)) {
			IStockEntry stockEntry = stockService.findStockEntryForArticleInStock(stock, article);
			if (stockEntry != null) {
				if (stockEntry.getCurrentStock() >= amount) {
					return stockEntry;
				}
				if (fallback == null) {
					fallback = stockEntry;
				}
			}
		}
		return fallback;
	}

	public static int getAvailableStockAmount(IStockEntry entry, IStockService stockService,
			IContextService contextService) {
		IStockEntry sourceStockEntry = findSourceStockEntry(entry.getArticle(), getMissingAmount(entry), stockService,
				contextService);
		return sourceStockEntry != null ? sourceStockEntry.getCurrentStock() : 0;
	}

	/**
	 * In a patient stock the minimum stock is the requested amount.
	 */
	public static int getMissingAmount(IStockEntry entry) {
		return Math.max(0, entry.getMinimumStock() - entry.getCurrentStock());
	}

	/**
	 * Taking only a part of the missing amount is not supported. An active
	 * mandator is required, as the taken amount is billed. Entries with an open
	 * order are excluded, as the article is already ordered from the supplier.
	 */
	public static boolean canTakeFromStock(List<IStockEntry> entries, IStockService stockService,
			IContextService contextService, IOrderService orderService) {
		if (entries == null || entries.isEmpty() || contextService.getActiveMandator().isEmpty()) {
			return false;
		}
		Map<IArticle, Integer> missingByArticle = new HashMap<>();
		for (IStockEntry entry : entries) {
			int missing = getMissingAmount(entry);
			if (entry.getArticle() == null || missing == 0 || entry.getMinimumStock() > entry.getMaximumStock()
					|| orderService.findOpenOrderEntryForStockEntry(entry) != null) {
				return false;
			}
			missingByArticle.merge(entry.getArticle(), missing, Integer::sum);
		}
		return missingByArticle.entrySet().stream().allMatch(e -> {
			IStockEntry sourceStockEntry = findSourceStockEntry(e.getKey(), e.getValue(), stockService,
					contextService);
			return sourceStockEntry != null && sourceStockEntry.getCurrentStock() >= e.getValue();
		});
	}

	public static int takeFromStock(IStockEntry entry, IStockService stockService, IModelService coreModelService,
			IContextService contextService, IOrderService orderService) {
		if (!canTakeFromStock(List.of(entry), stockService, contextService, orderService)) {
			return 0;
		}
		IStockEntry sourceStockEntry = findSourceStockEntry(entry.getArticle(), getMissingAmount(entry),
				stockService, contextService);
		LockResponse lockResponse = LocalLockServiceHolder.get().acquireLockBlocking(sourceStockEntry, 1,
				new NullProgressMonitor());
		if (!lockResponse.isOk()) {
			LoggerFactory.getLogger(MediorderPartUtil.class).warn("Could not acquire lock for stock entry [{}]", //$NON-NLS-1$
					sourceStockEntry.getId());
			return 0;
		}
		int missing = getMissingAmount(entry);
		try {
			// another user could have changed the source stock in the meantime
			coreModelService.refresh(sourceStockEntry, true);
			if (sourceStockEntry.getCurrentStock() < missing) {
				return 0;
			}
			sourceStockEntry.setCurrentStock(sourceStockEntry.getCurrentStock() - missing);
			entry.setCurrentStock(entry.getCurrentStock() + missing);
			coreModelService.save(List.of(sourceStockEntry, entry));
		} finally {
			LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
		}
		contextService.postEvent(ElexisEventTopics.EVENT_UPDATE, sourceStockEntry);
		return missing;
	}

	/**
	 * @param items of a FHIR QuestionnaireResponse
	 * @return a map with the question text and answer
	 */
	public static Map<String, String> extractItemValues(JsonArray items) {
		Map<String, String> values = new HashMap<>();
		for (JsonElement itemElement : items) {
			JsonObject item = itemElement.getAsJsonObject();
			String text = item.get("text").getAsString();
			JsonArray answers = item.getAsJsonArray("answer");
			if (answers == null || answers.isEmpty()) {
				continue;
			}
			JsonObject answer = answers.get(0).getAsJsonObject();
			if (answer.has("valueString")) {
				values.put(text, answer.get("valueString").getAsString());
			} else if (answer.has("valueDate")) {
				values.put(text, answer.get("valueDate").getAsString());
			}
		}
		return values;
	}

	/**
	 * The medication group is the third item of the FHIR QuestionnaireResponse.
	 *
	 * @param items of a FHIR QuestionnaireResponse
	 * @return a map with GTIN and order amount
	 */
	public static Map<String, Integer> extractMedications(JsonArray items) {
		Map<String, Integer> articleGtinsWithAmount = new HashMap<>();
		JsonObject medicationGroup = items.get(2).getAsJsonObject();
		JsonArray medicationItems = medicationGroup.getAsJsonArray("item");
		if (medicationItems == null) {
			return articleGtinsWithAmount;
		}

		for (JsonElement medElement : medicationItems) {
			JsonObject medItem = medElement.getAsJsonObject();

			String gtin = null;
			JsonArray extensions = medItem.getAsJsonArray("extension");
			if (extensions != null) {
				for (JsonElement extElement : extensions) {
					JsonObject ext = extElement.getAsJsonObject();
					if ("http://gs1.org/gtin".equals(ext.get("url").getAsString())) {
						gtin = ext.get("valueString").getAsString();
						break;
					}
				}
			}

			if (StringUtils.isBlank(gtin))
				continue;

			int amount = 1;
			JsonArray subItems = medItem.getAsJsonArray("item");
			if (subItems != null) {
				for (JsonElement subElement : subItems) {
					JsonObject subItem = subElement.getAsJsonObject();
					String subText = subItem.get("text").getAsString();
					if ("Anzahl".equals(subText)) {
						JsonArray answers = subItem.getAsJsonArray("answer");
						if (answers != null && !answers.isEmpty()) {
							JsonObject answer = answers.get(0).getAsJsonObject();
							if (answer.has("valueInteger")) {
								amount = answer.get("valueInteger").getAsInt();
							}
						}
						break;
					}
				}
			}

			articleGtinsWithAmount.put(gtin, amount);
		}

		return articleGtinsWithAmount;
	}
}