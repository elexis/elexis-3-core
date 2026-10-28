package ch.elexis.core.mediorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.services.IModelService;

/**
 * Barcode on the label of a delivered mediorder package, formatted as
 * <code>$MEDO$&lt;stockEntryId&gt;$&lt;orderEntryId&gt;$&lt;labelNumber&gt;</code>.
 * The stock entry is removed when the article is handed out, so the label of a
 * previous order can not hand out the current one.
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
		List<String> codes = new ArrayList<>();
		for (int labelNumber = first; labelNumber < first + amount; labelNumber++) {
			codes.add(encode(stockEntry.get().getId(), orderEntry.getId(), labelNumber));
		}
		return codes;
	}

	public static String encode(String stockEntryId, String orderEntryId, int labelNumber) {
		return new MediorderLabelCode(stockEntryId, orderEntryId, labelNumber).toCode();
	}

	public static boolean isLabelCode(String code) {
		return code != null && code.trim().startsWith(PREFIX);
	}

	public static Optional<MediorderLabelCode> decode(String code) {
		if (!isLabelCode(code)) {
			return Optional.empty();
		}
		String[] parts = code.trim().substring(PREFIX.length()).split("\\$", -1); //$NON-NLS-1$
		if (parts.length < 2 || parts.length > 3 || StringUtils.isAnyBlank(parts)) {
			return Optional.empty();
		}
		Integer labelNumber = null;
		if (parts.length == 3) {
			if (!StringUtils.isNumeric(parts[2]) || parts[2].length() > 6 || Integer.parseInt(parts[2]) < 1) {
				return Optional.empty();
			}
			labelNumber = Integer.valueOf(parts[2]);
		}
		return Optional.of(new MediorderLabelCode(parts[0], parts[1], labelNumber));
	}

	public static Set<String> getScannedLabels(List<OrderHistoryEntry> mediorderHistory, String stockEntryId) {
		String stockEntryPrefix = PREFIX + stockEntryId + SEPARATOR;
		return mediorderHistory.stream().filter(entry -> OrderHistoryAction.LABELSCANNED == entry.getAction())
				.map(OrderHistoryEntry::getExtraInfo).filter(code -> code != null && code.startsWith(stockEntryPrefix))
				.collect(Collectors.toSet());
	}

	public String toCode() {
		String code = PREFIX + stockEntryId + SEPARATOR + orderEntryId;
		return labelNumber != null ? code + SEPARATOR + labelNumber : code;
	}

	public Optional<Identifiable> resolve(IModelService modelService) {
		return modelService.load(stockEntryId, IStockEntry.class).map(Identifiable.class::cast)
				.or(() -> modelService.load(orderEntryId, IOrderEntry.class).map(Identifiable.class::cast));
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
