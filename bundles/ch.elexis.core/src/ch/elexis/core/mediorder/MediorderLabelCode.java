package ch.elexis.core.mediorder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;

/**
 * Barcode on the label of a delivered mediorder package, formatted as
 * <code>$MEDO$&lt;stockEntryId&gt;$&lt;orderEntryId&gt;$&lt;labelNumber&gt;</code>.
 * The orderEntryId is empty if the article was taken from stock instead of
 * being ordered. The stock entry is removed when the article is handed out, so
 * the label of a previous order can not hand out the current one.
 */
public record MediorderLabelCode(String stockEntryId, String orderEntryId, Integer labelNumber) {

	public static final String PREFIX = "$MEDO$"; //$NON-NLS-1$
	private static final String SEPARATOR = "$"; //$NON-NLS-1$

	/**
	 * The labels are numbered by the delivered amount of the order entry, so a
	 * partial delivery continues the numbers of the previous one.
	 */
	public static List<String> encodeDelivery(IOrderEntry orderEntry, int amount) {
		Optional<IStockEntry> stockEntry = findStockEntry(orderEntry);
		if (stockEntry.isEmpty() || amount <= 0) {
			return List.of();
		}
		int first = Math.max(1, orderEntry.getDelivered() - amount + 1);
		return encodeLabels(stockEntry.get().getId(), orderEntry.getId(), first, amount);
	}

	/**
	 * Codes for the labels of the packages taken from stock into the patient stock
	 * entry. The labels continue the numbers of the packages already in the
	 * patient stock, so the current stock of the entry has to include the taken
	 * amount.
	 *
	 * @param stockEntry   the patient stock entry the packages were taken into
	 * @param amount       number of taken packages
	 * @param orderService
	 * @return the codes, empty if the stock entry is not a patient stock entry
	 */
	public static List<String> encodeTakenFromStock(IStockEntry stockEntry, int amount,
			IOrderService orderService) {
		if (stockEntry == null || amount <= 0 || !isPatientStock(stockEntry.getStock())) {
			return List.of();
		}
		int first = Math.max(1, stockEntry.getCurrentStock() - amount + 1);
		return encodeStockEntry(stockEntry, first, amount, orderService);
	}

	/**
	 * Codes for reprinting the labels of a billed mediorder article, e.g. manually
	 * from the encounter. The labels are numbered 1..amount like the labels of the
	 * delivery, so a reprinted label is recognized as already scanned instead of
	 * being counted twice.
	 *
	 * @param billed       the billed article, linked to the patient stock entry by
	 *                     {@link Constants#FLD_EXT_MEDIORDER_STOCKENTRY_ID}
	 * @param amount       number of labels
	 * @param modelService
	 * @param orderService
	 * @return the codes, empty if the billed article does not belong to a patient
	 *         stock entry or the stock entry was already handed out, the standard
	 *         label is printed then
	 */
	public static List<String> encodeReprint(IBilled billed, int amount, IModelService modelService,
			IOrderService orderService) {
		if (billed == null || amount <= 0) {
			return List.of();
		}
		Object stockEntryId = billed.getExtInfo(Constants.FLD_EXT_MEDIORDER_STOCKENTRY_ID);
		if (!(stockEntryId instanceof String id) || StringUtils.isBlank(id)) {
			return List.of();
		}
		return modelService.load(id, IStockEntry.class).filter(stockEntry -> isPatientStock(stockEntry.getStock()))
				.map(stockEntry -> encodeStockEntry(stockEntry, 1, amount, orderService)).orElse(List.of());
	}

	public static String encode(String stockEntryId, String orderEntryId, int labelNumber) {
		return new MediorderLabelCode(stockEntryId, orderEntryId, labelNumber).toCode();
	}

	public static boolean isLabelCode(String code) {
		return code != null && code.trim().startsWith(PREFIX);
	}

	/**
	 * The orderEntryId may be empty if a label number is present (article taken
	 * from stock). <code>$MEDO$&lt;stockEntryId&gt;$</code> is not a valid code,
	 * it is the fallback key of the label scan.
	 */
	public static Optional<MediorderLabelCode> decode(String code) {
		if (!isLabelCode(code)) {
			return Optional.empty();
		}
		String[] parts = code.trim().substring(PREFIX.length()).split("\\$", -1); //$NON-NLS-1$
		if (parts.length < 2 || parts.length > 3 || StringUtils.isBlank(parts[0])) {
			return Optional.empty();
		}
		String orderEntryId = StringUtils.trimToNull(parts[1]);
		Integer labelNumber = null;
		if (parts.length == 3) {
			if (!StringUtils.isNumeric(parts[2]) || parts[2].length() > 6 || Integer.parseInt(parts[2]) < 1) {
				return Optional.empty();
			}
			labelNumber = Integer.valueOf(parts[2]);
		} else if (orderEntryId == null) {
			return Optional.empty();
		}
		return Optional.of(new MediorderLabelCode(parts[0], orderEntryId, labelNumber));
	}

	public static Set<String> getScannedLabels(List<OrderHistoryEntry> mediorderHistory, String stockEntryId) {
		String stockEntryPrefix = PREFIX + stockEntryId + SEPARATOR;
		return mediorderHistory.stream().filter(entry -> OrderHistoryAction.LABELSCANNED == entry.getAction())
				.map(OrderHistoryEntry::getExtraInfo).filter(code -> code != null && code.startsWith(stockEntryPrefix))
				.collect(Collectors.toSet());
	}

	public String toCode() {
		String code = PREFIX + stockEntryId + SEPARATOR + StringUtils.defaultString(orderEntryId);
		return labelNumber != null ? code + SEPARATOR + labelNumber : code;
	}

	public Optional<Identifiable> resolve(IModelService modelService) {
		Optional<Identifiable> stockEntry = modelService.load(stockEntryId, IStockEntry.class)
				.map(Identifiable.class::cast);
		if (stockEntry.isPresent() || StringUtils.isBlank(orderEntryId)) {
			return stockEntry;
		}
		return modelService.load(orderEntryId, IOrderEntry.class).map(Identifiable.class::cast);
	}

	private static List<String> encodeStockEntry(IStockEntry stockEntry, int first, int amount,
			IOrderService orderService) {
		String orderEntryId = findOrderEntry(stockEntry, orderService).map(IOrderEntry::getId).orElse(null);
		return encodeLabels(stockEntry.getId(), orderEntryId, first, amount);
	}

	private static List<String> encodeLabels(String stockEntryId, String orderEntryId, int first, int amount) {
		List<String> codes = new ArrayList<>();
		for (int labelNumber = first; labelNumber < first + amount; labelNumber++) {
			codes.add(encode(stockEntryId, orderEntryId, labelNumber));
		}
		return codes;
	}

	/**
	 * The open order entry of the stock entry, or the latest order entry of the
	 * article in the stock if it is already delivered. Empty if the article was
	 * taken from stock.
	 */
	private static Optional<IOrderEntry> findOrderEntry(IStockEntry stockEntry, IOrderService orderService) {
		if (orderService == null) {
			return Optional.empty();
		}
		IOrderEntry open = orderService.findOpenOrderEntryForStockEntry(stockEntry);
		if (open != null) {
			return Optional.of(open);
		}
		IArticle article = stockEntry.getArticle();
		if (article == null) {
			return Optional.empty();
		}
		return orderService.findOrderEntryForStock(stockEntry.getStock()).stream()
				.filter(entry -> !entry.isDeleted() && article.equals(entry.getArticle()))
				.max(Comparator.comparing(MediorderLabelCode::getOrderTimestamp));
	}

	private static LocalDateTime getOrderTimestamp(IOrderEntry orderEntry) {
		IOrder order = orderEntry.getOrder();
		return order != null && order.getTimestamp() != null ? order.getTimestamp() : LocalDateTime.MIN;
	}

	private static Optional<IStockEntry> findStockEntry(IOrderEntry orderEntry) {
		if (orderEntry == null || orderEntry.getArticle() == null || !isPatientStock(orderEntry.getStock())) {
			return Optional.empty();
		}
		IArticle article = orderEntry.getArticle();
		return orderEntry.getStock().getStockEntries().stream()
				.filter(stockEntry -> article.equals(stockEntry.getArticle())).findFirst();
	}

	private static boolean isPatientStock(IStock stock) {
		IPerson owner = stock != null ? stock.getOwner() : null;
		return owner != null && owner.isPatient();
	}
}
