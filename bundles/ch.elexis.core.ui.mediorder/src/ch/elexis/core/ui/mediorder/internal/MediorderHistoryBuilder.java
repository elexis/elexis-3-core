package ch.elexis.core.ui.mediorder.internal;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ch.elexis.core.mediorder.MediorderBlobId;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;

public class MediorderHistoryBuilder {

	private static final String HISTORY_JSON_LINK = "http://mediorder.elexis/json/";
	private static final String HISTORY_JSON_COPY = HISTORY_JSON_LINK + "copy/";
	private static final String HISTORY_JSON_DOWNLOAD = HISTORY_JSON_LINK + "download/";

	private final IModelService coreModelService;
	private final IOrderService orderService;
	private final ICodeElementService codeElementService;
	private final DateTimeFormatter dateFormatter;
	private final DateTimeFormatter timeFormatter;
	private final Map<String, String> historyJsonRefs = new HashMap<>();

	public MediorderHistoryBuilder(IModelService coreModelService, IOrderService orderService,
			ICodeElementService codeElementService, DateTimeFormatter dateFormatter,
			DateTimeFormatter timeFormatter) {
		this.coreModelService = coreModelService;
		this.orderService = orderService;
		this.codeElementService = codeElementService;
		this.dateFormatter = dateFormatter;
		this.timeFormatter = timeFormatter;
	}

	public static record MedicationOrderEntry(String name, int count) {
	}

	public static record JsonExport(String blobId, String fileName, String json, boolean download) {
	}

	public static boolean isJsonLink(String location) {
		return location != null && location.startsWith(HISTORY_JSON_LINK);
	}

	public Optional<JsonExport> resolveJsonExport(String location) {
		if (!isJsonLink(location)) {
			return Optional.empty();
		}
		boolean download = location.startsWith(HISTORY_JSON_DOWNLOAD);
		if (!download && !location.startsWith(HISTORY_JSON_COPY)) {
			return Optional.empty();
		}
		String prefix = download ? HISTORY_JSON_DOWNLOAD : HISTORY_JSON_COPY;
		String blobId = historyJsonRefs.get(StringUtils.strip(location.substring(prefix.length()), "/"));
		if (blobId == null) {
			return Optional.empty();
		}
		IBlob blob = coreModelService.load(blobId, IBlob.class).orElse(null);
		String json = blob != null ? prettyJson(blob) : null;
		if (StringUtils.isBlank(json)) {
			LoggerFactory.getLogger(getClass()).warn("JSON of blob {} is not available.", blobId);
			return Optional.empty();
		}
		return Optional.of(new JsonExport(blobId, suggestJsonFileName(blob), json, download));
	}

	private record TimelineEntry(LocalDateTime sortKey, Map<String, Object> model) {
	}

	public List<Map<String, Object>> buildSections(IStock stock) {
		historyJsonRefs.clear();
		if (stock == null || stock.getOwner() == null) {
			return List.of();
		}
		List<OrderRound> rounds = loadRounds(stock);
		if (rounds.isEmpty()) {
			rounds = List.of(new OrderRound(null, null, List.of()));
		}
		int newest = rounds.size() - 1;
		List<Map<String, Object>> sections = new ArrayList<>();
		for (int i = newest; i >= 0; i--) {
			OrderRound round = rounds.get(i);
			boolean isOpenRound = i == newest && !isClosed(round.events());
			List<TimelineEntry> collected = collectRoundEntries(stock, round, isOpenRound);
			if (collected.isEmpty()) {
				continue;
			}
			sections.add(createSectionModel(i + 1, round, isOpenRound, sections.isEmpty(), collected));
		}
		return sections;
	}

	private List<TimelineEntry> collectRoundEntries(IStock stock, OrderRound round, boolean isOpenRound) {
		List<TimelineEntry> collected = new ArrayList<>();
		if (round.blob() != null) {
			addOnlineOrderEntry(collected, stock, round.blob(), round.receivedAt());
		}
		if (isOpenRound) {
			if (round.blob() != null) {
				addArticleDiffEntries(collected, stock, round.blob(), round.events());
			}
			addOrderHistoryEntries(collected, stock);
		}
		addLoggedEntries(collected, round.events());

		collected.sort((a, b) -> {
			if (a.sortKey() == null && b.sortKey() == null) {
				return 0;
			}
			if (a.sortKey() == null) {
				return 1;
			}
			if (b.sortKey() == null) {
				return -1;
			}
			return b.sortKey().compareTo(a.sortKey());
		});
		return collected;
	}

	private Map<String, Object> createSectionModel(int number, OrderRound round, boolean isOpenRound, boolean expanded,
			List<TimelineEntry> collected) {
		Map<String, Object> section = new HashMap<>();
		section.put("title", describeTitle(number, round, isOpenRound));
		section.put("statusType", isOpenRound ? "open" : "closed");
		section.put("statusLabel", isOpenRound ? "Offen" : "Abgeschlossen");
		section.put("rangeLabel", describeRange(collected));
		section.put("summary", describeSummary(round.events(), collected));
		section.put("expanded", expanded);
		section.put("entries", collected.stream().map(TimelineEntry::model).collect(Collectors.toList()));
		addJsonLinks(section, round.blob());
		return section;
	}

	private void addJsonLinks(Map<String, Object> model, IBlob blob) {
		if (blob == null || StringUtils.isBlank(blob.getId())) {
			return;
		}
		String token = "j" + historyJsonRefs.size();
		historyJsonRefs.put(token, blob.getId());
		model.put("jsonCopyHref", HISTORY_JSON_COPY + token);
		model.put("jsonDownloadHref", HISTORY_JSON_DOWNLOAD + token);
	}
	
	private String describeTitle(int number, OrderRound round, boolean isOpenRound) {
		LocalDate received = round.receivedAt() != null ? round.receivedAt().toLocalDate()
				: round.blob() != null ? round.blob().getDate() : null;
		if (received != null) {
			return "Bestellung vom " + received.format(dateFormatter);
		}
		return isOpenRound ? "Aktueller Vorgang" : "Vorgang " + number;
	}

	private String describeRange(List<TimelineEntry> collected) {
		List<LocalDateTime> stamps = collected.stream().map(TimelineEntry::sortKey).filter(Objects::nonNull).toList();
		if (stamps.isEmpty()) {
			return StringUtils.EMPTY;
		}
		String newest = stamps.get(0).format(dateFormatter);
		String oldest = stamps.get(stamps.size() - 1).format(dateFormatter);
		return oldest.equals(newest) ? newest : oldest + " – " + newest;
	}

	private String describeSummary(List<OrderHistoryEntry> events, List<TimelineEntry> collected) {
		String count = collected.size() == 1 ? "1 Ereignis" : collected.size() + " Ereignisse";
		return events.stream().filter(e -> OrderHistoryAction.PICKEDUP.equals(e.getAction()))
				.map(OrderHistoryEntry::getDetails).filter(StringUtils::isNotBlank).findFirst()
				.map(articles -> count + " · " + articles).orElse(count);
	}

	private record OrderRound(IBlob blob, LocalDateTime receivedAt, List<OrderHistoryEntry> events) {
	}

	private List<IBlob> loadOrderBlobs(IPatient patient) {
		Comparator<IBlob> byReceived = Comparator.comparing(
				blob -> MediorderBlobId.resolveTimestamp(blob).orElse(null),
				Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()));

		IQuery<IBlob> query = coreModelService.getQuery(IBlob.class);
		query.and("id", COMPARATOR.LIKE, MediorderBlobId.idPrefix(patient.getId()) + "%");
		return query.execute().stream().filter(blob -> MediorderBlobId.belongsTo(blob.getId(), patient.getId()))
				.sorted(byReceived).toList();
	}

	private List<OrderRound> loadRounds(IStock stock) {
		if (stock == null || stock.getOwner() == null) {
			return List.of();
		}
		IPatient patient = stock.getOwner().asIPatient();
		if (patient == null) {
			return List.of();
		}
		List<OrderHistoryEntry> logged = orderService.getHistoryService().getMediorderHistory(patient);
		List<IBlob> blobs = loadOrderBlobs(patient);
		if (blobs.isEmpty()) {
			return splitIntoRounds(logged).stream().map(events -> new OrderRound(null, null, events)).toList();
		}

		List<OrderRound> rounds = new ArrayList<>();
		for (IBlob blob : blobs) {
			rounds.add(new OrderRound(blob, MediorderBlobId.resolveTimestamp(blob).orElse(null), new ArrayList<>()));
		}
		for (OrderHistoryEntry entry : logged) {
			rounds.get(roundIndexFor(rounds, parseTimestamp(entry.getTimestamp()))).events().add(entry);
		}
		return rounds;
	}

	private int roundIndexFor(List<OrderRound> rounds, LocalDateTime timestamp) {
		if (timestamp == null) {
			return 0;
		}
		int index = 0;
		for (int i = 0; i < rounds.size(); i++) {
			LocalDateTime receivedAt = rounds.get(i).receivedAt();
			if (receivedAt != null && !receivedAt.isAfter(timestamp)) {
				index = i;
			}
		}
		return index;
	}

	private List<List<OrderHistoryEntry>> splitIntoRounds(List<OrderHistoryEntry> logged) {
		List<List<OrderHistoryEntry>> rounds = new ArrayList<>();
		List<OrderHistoryEntry> current = new ArrayList<>();
		for (OrderHistoryEntry entry : logged) {
			current.add(entry);
			if (OrderHistoryAction.PICKEDUP.equals(entry.getAction())) {
				rounds.add(current);
				current = new ArrayList<>();
			}
		}
		rounds.add(current);
		return rounds;
	}

	private boolean isClosed(List<OrderHistoryEntry> round) {
		return round.stream().anyMatch(e -> OrderHistoryAction.PICKEDUP.equals(e.getAction()));
	}

	private void addOnlineOrderEntry(List<TimelineEntry> collected, IStock stock, IBlob blob,
			LocalDateTime receivedAt) {
		LocalDateTime timestamp = receivedAt != null ? receivedAt
				: MediorderBlobId.resolveTimestamp(blob).orElse(null);
		boolean timeKnown = MediorderBlobId.hasTimestamp(blob.getId());
		Map<String, Object> entry = createModel("created", timestamp, "Online-Bestellung eingegangen");
		List<MedicationOrderEntry> medications = parseMedicationEntries(blob.getStringContent());
		if (!medications.isEmpty()) {
			entry.put("description", medications.stream().map(m -> m.name() + " (" + m.count() + ")")
					.collect(Collectors.joining(", ")));
		}
		entry.put("actor", stock.getOwner().getLabel());
		addJsonLinks(entry, blob);
		if (!timeKnown) {
			entry.put("timeLabel", "");
		}
		collected.add(new TimelineEntry(timestamp, entry));
	}

	private void addArticleDiffEntries(List<TimelineEntry> collected, IStock stock, IBlob blob,
			List<OrderHistoryEntry> events) {
		Map<String, Integer> ordered = readOrderedArticles(blob);
		if (ordered.isEmpty()) {
			return;
		}
		List<IStockEntry> current = stock.getStockEntries().stream().filter(e -> e.getArticle() != null).toList();
		Set<String> loggedArticles = loggedArticleIds(events, OrderHistoryAction.ADDMEDI,
				OrderHistoryAction.REMOVEDMEDI);
		Set<String> loggedAmounts = loggedArticleIds(events, OrderHistoryAction.AMOUNTADJUSTED);

		Set<String> matched = new HashSet<>();
		List<TimelineEntry> changes = new ArrayList<>();

		for (Map.Entry<String, Integer> orderedArticle : ordered.entrySet()) {
			String gtin = orderedArticle.getKey();
			IArticle article = resolveArticle(gtin);
			IStockEntry stockEntry = findStockEntry(current, article, gtin);
			if (stockEntry == null) {
				if (article == null || !loggedArticles.contains(article.getId())) {
					Map<String, Object> model = createModel("deleted", null, "Artikel abgelehnt");
					model.put("description", article != null ? article.getLabel() : gtin);
					collected.add(new TimelineEntry(null, model));
				}
				continue;
			}
			matched.add(stockEntry.getId());
			if (loggedAmounts.contains(stockEntry.getArticle().getId())) {
				continue;
			}
			int requested = orderedArticle.getValue();
			if (stockEntry.getMinimumStock() != requested) {
				Map<String, Object> model = createModel("updated", null, "Anzahl geändert");
				model.put("description", stockEntry.getArticle().getLabel() + ": " + requested + " → "
						+ stockEntry.getMinimumStock());
				changes.add(new TimelineEntry(null, model));
			}
			if (stockEntry.getMaximumStock() < stockEntry.getMinimumStock()) {
				Map<String, Object> model = createModel("updated", null, "Teilweise freigegeben");
				model.put("description", stockEntry.getArticle().getLabel() + ": " + stockEntry.getMaximumStock()
						+ " von " + stockEntry.getMinimumStock() + " freigegeben");
				changes.add(new TimelineEntry(null, model));
			}
		}
		for (IStockEntry stockEntry : current) {
			if (matched.contains(stockEntry.getId())
					|| loggedArticles.contains(stockEntry.getArticle().getId())) {
				continue;
			}
			Map<String, Object> model = createModel("created", null, "Artikel hinzugefügt");
			model.put("description", stockEntry.getArticle().getLabel());
			collected.add(new TimelineEntry(null, model));
		}
		collected.addAll(changes);
	}

	private Set<String> loggedArticleIds(List<OrderHistoryEntry> events, OrderHistoryAction... actions) {
		Set<OrderHistoryAction> wanted = Set.of(actions);
		return events.stream().filter(e -> e.getAction() != null && wanted.contains(e.getAction()))
				.map(OrderHistoryEntry::getExtraInfo).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
	}

	private IStockEntry findStockEntry(List<IStockEntry> current, IArticle article, String gtin) {
		if (article != null) {
			for (IStockEntry stockEntry : current) {
				if (article.getId().equals(stockEntry.getArticle().getId())) {
					return stockEntry;
				}
			}
		}
		for (IStockEntry stockEntry : current) {
			if (gtinMatches(stockEntry.getArticle().getGtin(), gtin)) {
				return stockEntry;
			}
		}
		return null;
	}

	private boolean gtinMatches(String left, String right) {
		if (StringUtils.isBlank(left) || StringUtils.isBlank(right)) {
			return false;
		}
		return StringUtils.stripStart(left.trim(), "0").equals(StringUtils.stripStart(right.trim(), "0"));
	}

	private Map<String, Integer> readOrderedArticles(IBlob blob) {
		try {
			JsonObject root = JsonParser.parseString(blob.getStringContent()).getAsJsonObject();
			JsonArray items = root.getAsJsonArray("item");
			if (items == null || items.size() <= 2) {
				return Map.of();
			}
			return MediorderPartUtil.extractMedications(items);
		} catch (JsonParseException | IllegalStateException e) {
			LoggerFactory.getLogger(getClass()).warn("Failed to read the order JSON.", e);
			return Map.of();
		}
	}

	private IArticle resolveArticle(String gtin) {
		try {
			return codeElementService.findArticleByGtin(gtin).orElse(null);
		} catch (IllegalStateException e) {
			LoggerFactory.getLogger(getClass()).warn("Unable to resolve the item with GTIN {}", gtin, e);
			return null;
		}
	}

	private void addOrderHistoryEntries(List<TimelineEntry> collected, IStock stock) {
		List<IOrderEntry> orderEntries = orderService.findOrderEntryForStock(stock);
		if (orderEntries.isEmpty()) {
			return;
		}
		Set<String> patientArticles = orderEntries.stream().map(IOrderEntry::getArticle).filter(Objects::nonNull)
				.map(IArticle::getLabel).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

		Set<String> seenOrders = new HashSet<>();
		for (IOrderEntry orderEntry : orderEntries) {
			IOrder order = orderEntry.getOrder();
			if (order == null || !seenOrders.add(order.getId())) {
				continue;
			}
			IOutputLog log = orderService.getOrderLogEntry(order);
			if (log == null) {
				continue;
			}
			for (OrderHistoryEntry logged : parseOrderHistory(log)) {
				OrderHistoryAction action = logged.getAction();
				if (action == null) {
					continue;
				}
				String type = mapOrderActionToType(action);
				if (type == null) {
					continue;
				}
				if (OrderHistoryAction.DELIVERED.equals(action) && !mentionsAny(logged.getDetails(), patientArticles)) {
					continue;
				}
				LocalDateTime timestamp = parseTimestamp(logged.getTimestamp());
				Map<String, Object> model = createModel(type, timestamp, action.getTranslation());
				model.put("description", StringUtils.defaultString(logged.getDetails()));
				model.put("actor", resolveUserLabel(logged.getUserId()));
				collected.add(new TimelineEntry(timestamp, model));
			}
		}
	}

	private String mapOrderActionToType(OrderHistoryAction action) {
		return switch (action) {
		case ORDERED -> "updated";
		case DELIVERED, COMPLETEDELIVERY -> "created";
		default -> null;
		};
	}

	private boolean mentionsAny(String details, Set<String> articleLabels) {
		if (StringUtils.isBlank(details)) {
			return false;
		}
		return articleLabels.stream().anyMatch(details::contains);
	}

	private List<OrderHistoryEntry> parseOrderHistory(IOutputLog log) {
		try {
			OrderHistoryEntry[] entries = new Gson().fromJson(
					StringUtils.defaultIfBlank(log.getOutputterStatus(), "[]"), OrderHistoryEntry[].class);
			return entries != null ? Arrays.asList(entries) : List.of();
		} catch (JsonParseException | IllegalStateException e) {
			LoggerFactory.getLogger(getClass()).warn("Failed to read the order history.", e);
			return List.of();
		}
	}

	private String mapMediorderActionToType(OrderHistoryAction action) {
		return switch (action) {
		case PICKEDUP, ADDMEDI -> "created";
		case REMOVEDMEDI -> "deleted";
		default -> "updated";
		};
	}

	private void addLoggedEntries(List<TimelineEntry> collected, List<OrderHistoryEntry> roundEntries) {
		for (int i = 0; i < roundEntries.size(); i++) {
			OrderHistoryEntry logged = roundEntries.get(i);
			OrderHistoryAction action = logged.getAction();
			if (action == null) {
				continue;
			}
			OrderHistoryEntry next = i + 1 < roundEntries.size() ? roundEntries.get(i + 1) : null;
			if (isReplacement(logged, next)) {
				addReplacementEntry(collected, logged, next);
				i++;
				continue;
			}
			LocalDateTime timestamp = parseTimestamp(logged.getTimestamp());
			Map<String, Object> model = createModel(mapMediorderActionToType(action), timestamp,
					action.getTranslation());
			model.put("description", StringUtils.defaultString(logged.getDetails()));
			model.put("actor", resolveUserLabel(logged.getUserId()));
			collected.add(new TimelineEntry(timestamp, model));
		}
	}

	private static final Duration REPLACEMENT_WINDOW = Duration.ofMinutes(10);

	private boolean isReplacement(OrderHistoryEntry first, OrderHistoryEntry second) {
		if (second == null || second.getAction() == null) {
			return false;
		}
		boolean pair = (OrderHistoryAction.REMOVEDMEDI.equals(first.getAction())
				&& OrderHistoryAction.ADDMEDI.equals(second.getAction()))
				|| (OrderHistoryAction.ADDMEDI.equals(first.getAction())
						&& OrderHistoryAction.REMOVEDMEDI.equals(second.getAction()));
		if (!pair || !Objects.equals(first.getUserId(), second.getUserId())) {
			return false;
		}
		LocalDateTime from = parseTimestamp(first.getTimestamp());
		LocalDateTime to = parseTimestamp(second.getTimestamp());
		if (from == null || to == null || to.isBefore(from)) {
			return false;
		}
		return Duration.between(from, to).compareTo(REPLACEMENT_WINDOW) <= 0;
	}

	private void addReplacementEntry(List<TimelineEntry> collected, OrderHistoryEntry first,
			OrderHistoryEntry second) {
		boolean firstIsRemoval = OrderHistoryAction.REMOVEDMEDI.equals(first.getAction());
		OrderHistoryEntry removed = firstIsRemoval ? first : second;
		OrderHistoryEntry added = firstIsRemoval ? second : first;

		LocalDateTime timestamp = parseTimestamp(second.getTimestamp());
		Map<String, Object> model = createModel("updated", timestamp, "Artikel ersetzt");
		model.put("description",
				StringUtils.defaultString(removed.getDetails()) + " → " + StringUtils.defaultString(added.getDetails()));
		model.put("actor", resolveUserLabel(added.getUserId()));
		collected.add(new TimelineEntry(timestamp, model));
	}

	private Map<String, Object> createModel(String type, LocalDateTime timestamp, String title) {
		Map<String, Object> model = new HashMap<>();
		model.put("type", type);
		model.put("dateLabel", timestamp != null ? timestamp.format(dateFormatter) : "");
		model.put("timeLabel", timestamp != null ? timestamp.format(timeFormatter) : "");
		model.put("title", title);
		return model;
	}

	private LocalDateTime parseTimestamp(String timestamp) {
		if (StringUtils.isBlank(timestamp)) {
			return null;
		}
		try {
			return LocalDateTime.parse(timestamp);
		} catch (DateTimeParseException e) {
			LoggerFactory.getLogger(getClass()).warn("Failed to read timestamp {}.", timestamp, e);
			return null;
		}
	}

	private String resolveUserLabel(String userId) {
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		return coreModelService.load(userId, IUser.class).map(IUser::getAssociatedContactId)
				.filter(StringUtils::isNotBlank).flatMap(contactId -> coreModelService.load(contactId, IContact.class))
				.map(IContact::getLabel).orElse(userId);
	}

	private List<MedicationOrderEntry> parseMedicationEntries(String json) {
		if (StringUtils.isBlank(json)) {
			return List.of();
		}
		try {
			List<MedicationOrderEntry> entries = new ArrayList<>();
			for (JsonElement group : JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("item")) {
				JsonArray children = group.getAsJsonObject().getAsJsonArray("item");
				if (children == null) {
					continue;
				}
				for (JsonElement medication : children) {
					MedicationOrderEntry entry = toEntry(medication.getAsJsonObject());
					if (entry != null) {
						entries.add(entry);
					}
				}
			}
			return entries;
		} catch (JsonParseException | IllegalStateException e) {
			LoggerFactory.getLogger(getClass()).warn("Failed to read the medication list.", e);
			return List.of();
		}
	}

	private static final String LABEL_COUNT = "Anzahl";
	private static final String LABEL_SELECTED = "Auswählen";

	private MedicationOrderEntry toEntry(JsonObject medication) {
		JsonArray details = medication.getAsJsonArray("item");
		if (details == null || !medication.has("text")) {
			return null;
		}
		Integer count = null;
		boolean selected = true;
		for (JsonElement detail : details) {
			JsonObject detailObject = detail.getAsJsonObject();
			JsonArray answer = detailObject.getAsJsonArray("answer");
			if (answer == null || answer.isEmpty() || !detailObject.has("text")) {
				continue;
			}
			JsonObject value = answer.get(0).getAsJsonObject();
			String label = detailObject.get("text").getAsString();
			if (LABEL_COUNT.equalsIgnoreCase(label) && value.has("valueInteger")) {
				count = value.get("valueInteger").getAsInt();
			} else if (LABEL_SELECTED.equalsIgnoreCase(label) && value.has("valueBoolean")) {
				selected = value.get("valueBoolean").getAsBoolean();
			}
		}
		return count != null && selected ? new MedicationOrderEntry(medication.get("text").getAsString(), count) : null;
	}

	private String suggestJsonFileName(IBlob blob) {
		return "mediorder_" + MediorderBlobId.resolveTimestamp(blob)
				.map(timestamp -> timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")))
				.orElse("bestellung") + ".json";
	}

	private String prettyJson(IBlob blob) {
		if (StringUtils.isBlank(blob.getStringContent())) {
			return null;
		}
		try {
			JsonObject root = JsonParser.parseString(blob.getStringContent()).getAsJsonObject();
			return new GsonBuilder().setPrettyPrinting().create().toJson(root);
		} catch (JsonSyntaxException | IllegalStateException e) {
			LoggerFactory.getLogger(getClass()).error("Error parsing JSON of blob {}", blob.getId(), e);
			return null;
		}
	}
}
