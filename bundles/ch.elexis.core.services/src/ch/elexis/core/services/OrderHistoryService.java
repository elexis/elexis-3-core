package ch.elexis.core.services;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class OrderHistoryService implements IOrderHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(OrderHistoryService.class);

	private static final Gson GSON = new Gson();

	private static final Type ENTRY_LIST_TYPE = new TypeToken<List<OrderHistoryEntry>>() {
	}.getType();

	public static final String MEDIORDER_OBJECT_TYPE = "ch.elexis.core.mediorder"; //$NON-NLS-1$

	/**
	 * Object type of the {@link IOutputLog} entries written for an {@link IOrder}.
	 * Deliberately not derived from the runtime class of the order, as
	 * {@link IOrder} has more than one implementation
	 * ({@link ch.elexis.core.model.Order} and the legacy ch.elexis.data.Bestellung)
	 * and the same order would otherwise be logged under two different types. The
	 * value must stay stable, as it is used to look up already persisted logs.
	 */
	public static final String ORDER_OBJECT_TYPE = "ch.elexis.core.model.Order"; //$NON-NLS-1$

	@Override
	public void logCreateOrder(IOrder order) {
		logOrderStatus(order, OrderHistoryAction.CREATED, null); // $NON-NLS-1$
	}

	@Override
	public void logEdit(IOrder order, IOrderEntry entry, int oldValue, int newValue) {
		if (oldValue == newValue)
			return;

		String details = entry.getArticle().getLabel() + " changed from " + oldValue + " to " + newValue; //$NON-NLS-1$ //$NON-NLS-2$
		logOrderStatus(order, OrderHistoryAction.EDITED, details); // $NON-NLS-1$
	}

	@Override
	public void logDelivery(IOrder order, IOrderEntry entry, int deliveredAmount, int orderAmaunt) {
		String details = deliveredAmount + "x von " + orderAmaunt + " " + entry.getArticle().getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
		logOrderStatus(order, OrderHistoryAction.DELIVERED, details); // $NON-NLS-1$
	}

	@Override
	public void logCreateEntry(IOrder order, IOrderEntry entry, int quantity) {
		String details = entry.getArticle().getLabel() + "/" + quantity; //$NON-NLS-1$
		logOrderStatus(order, OrderHistoryAction.ADDMEDI, details); // $NON-NLS-1$
	}

	@Override
	public void logOrder(IOrder order) {
		logOrderStatus(order, OrderHistoryAction.ORDERED, null); // $NON-NLS-1$
	}

	@Override
	public void logDelete(IOrder order) {
		logOrderStatus(order, OrderHistoryAction.DELETED, null); // $NON-NLS-1$
	}

	@Override
	public void logChangedAmount(IOrder order, IOrderEntry entry, int oldAmount, int newAmount) {
		if (oldAmount == newAmount) {
			return;
		}

		OrderHistoryAction action;
		String details;
		String articleLabel = entry.getArticle().getLabel();

		if (oldAmount == 0) {
			action = OrderHistoryAction.ADDED;
			details = articleLabel + " (Neu: " + newAmount + ")";
		} else if (newAmount > oldAmount) {
			int diff = newAmount - oldAmount;
			action = OrderHistoryAction.INCREASED;
			details = articleLabel + " (" + oldAmount + " \u2192 " + newAmount + ", +" + diff + ")";
		} else {
			int diff = oldAmount - newAmount;
			action = OrderHistoryAction.DECREASED;
			details = articleLabel + " (" + oldAmount + " \u2192 " + newAmount + ", -" + diff + ")";
		}

		logOrderStatus(order, action, details);
	}

	@Override
	public void logCompleteDelivery(IOrder order) {
		if (order == null) {
			return;
		}

		String details = ch.elexis.core.l10n.Messages.BestellView_OrderIsClosed;
		logOrderStatus(order, OrderHistoryAction.COMPLETEDELIVERY, details); // $NON-NLS-1$
	}

	@Override
	public void logRemove(IOrder order, IOrderEntry entry) {
		if (order == null || entry == null)
			return;

		String details = entry.getArticle().getLabel() + "/" + entry.getAmount(); //$NON-NLS-1$
		logOrderStatus(order, OrderHistoryAction.REMOVEDMEDI, details); // $NON-NLS-1$
	}

	@Override
	public void logOrderSent(IOrder order, boolean sent) {
		if (order == null)
			return;

		String method = sent ? ch.elexis.core.l10n.Messages.Outputter_Sent : "Printed"; //$NON-NLS-1$
		logOrderStatus(order, OrderHistoryAction.ORDERED, method); // $NON-NLS-1$
	}

	@Override
	public void logSupplierAdded(IOrder order, IOrderEntry entry, String supplier) {
		if (order == null || entry == null || supplier == null || supplier.isEmpty())
			return;

		String details = entry.getArticle().getLabel();
		logOrderStatus(order, OrderHistoryAction.SUPPLIERADDED, details, supplier); // $NON-NLS-1$
	}

	@Override
	public void logMediorderBilled(IPatient patient, List<String> articles) {
		logMediorderStatus(patient, OrderHistoryAction.BILLED, joinArticles(articles), null);
	}

	@Override
	public void logMediorderPickedUp(IPatient patient, List<String> articles) {
		logMediorderStatus(patient, OrderHistoryAction.PICKEDUP, joinArticles(articles), null);
	}

	@Override
	public void logMediorderArticleAdded(IPatient patient, IArticle article) {
		logMediorderArticle(patient, OrderHistoryAction.ADDMEDI, article);
	}

	@Override
	public void logMediorderArticleRemoved(IPatient patient, IArticle article) {
		logMediorderArticle(patient, OrderHistoryAction.REMOVEDMEDI, article);
	}

	@Override
	public void logMediorderAmountChanged(IPatient patient, IArticle article, String amountLabel, int oldValue,
			int newValue) {
		if (article == null || oldValue == newValue) {
			return;
		}
		String details = article.getLabel() + ": " + amountLabel + " " + oldValue + " \u2192 " + newValue;
		logMediorderStatus(patient, OrderHistoryAction.AMOUNTADJUSTED, details, article.getId());
	}
	
	private void logMediorderArticle(IPatient patient, OrderHistoryAction action, IArticle article) {
		if (article == null) {
			return;
		}
		logMediorderStatus(patient, action, article.getLabel(), article.getId());
	}

	@Override
	public List<OrderHistoryEntry> getMediorderHistory(IPatient patient) {
		if (patient == null) {
			return new ArrayList<>();
		}
		return findLog(patient.getId(), MEDIORDER_OBJECT_TYPE).map(this::readEntries).orElseGet(ArrayList::new);
	}

	private String joinArticles(List<String> articles) {
		return articles == null || articles.isEmpty() ? null : String.join(", ", articles); //$NON-NLS-1$
	}

	private void logMediorderStatus(IPatient patient, OrderHistoryAction action, String details, String extraInfo) {
		if (patient == null) {
			return;
		}
		OrderHistoryEntry entry = new OrderHistoryEntry(action, getActiveUserId(), details, extraInfo);
		appendEntry(patient.getId(), MEDIORDER_OBJECT_TYPE, entry);
	}

	private void logOrderStatus(IOrder order, OrderHistoryAction action, String details) {
		logOrderStatus(order, action, details, null);
	}

	private void logOrderStatus(IOrder order, OrderHistoryAction action, String details, String extraInfo) {
		if (order == null)
			return;
		OrderHistoryEntry entry = new OrderHistoryEntry(action, getActiveUserId(), details, extraInfo);
		saveLogEntry(order, entry);
	}

	private void saveLogEntry(IOrder order, OrderHistoryEntry entry) {
		if (order == null) {
			return;
		}
		Optional<IOutputLog> existingLog = findLog(order.getId(), ORDER_OBJECT_TYPE);
		if (existingLog.flatMap(log -> findEntry(log, entry)).isPresent()) {
			return;
		}
		appendEntry(existingLog, order.getId(), ORDER_OBJECT_TYPE, entry);
	}

	/**
	 * Look for an entry already present in the log, matching action, user, details
	 * and extra info of the provided entry. Used to avoid logging the very same
	 * information twice, as order events may be triggered repeatedly.
	 *
	 * @param log
	 * @param entry
	 * @return
	 */
	private Optional<OrderHistoryEntry> findEntry(IOutputLog log, OrderHistoryEntry entry) {
		return readEntries(log).stream()
				.filter(e -> Objects.equals(e.getAction(), entry.getAction())
						&& Objects.equals(e.getUserId(), entry.getUserId())
						&& Objects.equals(e.getDetails(), entry.getDetails())
						&& Objects.equals(e.getExtraInfo(), entry.getExtraInfo()))
				.findFirst();
	}

	private String getActiveUserId() {
		return PortableServiceLoader.get(IContextService.class).getActiveUser().map(user -> user.getId())
				.orElse("Unknown"); //$NON-NLS-1$
	}

	private Optional<IOutputLog> findLog(String objectId, String objectType) {
		IQuery<IOutputLog> query = PortableServiceLoader.getCoreModelService().getQuery(IOutputLog.class);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.EQUALS, objectId);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_TYPE, COMPARATOR.EQUALS, objectType);
		return query.execute().stream().findFirst();
	}

	private List<OrderHistoryEntry> readEntries(IOutputLog log) {
		List<OrderHistoryEntry> logList = new ArrayList<>();
		try {
			JsonElement jsonElement = JsonParser.parseString(log.getOutputterStatus());
			if (jsonElement.isJsonArray()) {
				logList = GSON.fromJson(jsonElement, ENTRY_LIST_TYPE);
			}
		} catch (Exception e) {
			logger.error("Error when parsing the existing logs: " + e.getMessage()); //$NON-NLS-1$
		}
		return logList;
	}

	private void appendEntry(String objectId, String objectType, OrderHistoryEntry entry) {
		appendEntry(findLog(objectId, objectType), objectId, objectType, entry);
	}

	private void appendEntry(Optional<IOutputLog> existingLog, String objectId, String objectType,
			OrderHistoryEntry entry) {
		List<OrderHistoryEntry> logList = existingLog.map(this::readEntries).orElseGet(ArrayList::new);
		logList.add(entry);

		String updatedJson = GSON.toJson(logList);
		if (existingLog.isPresent()) {
			IOutputLog log = existingLog.get();
			log.setOutputterStatus(updatedJson);
			PortableServiceLoader.getCoreModelService().save(log);
		} else {
			IOutputLog outputLog = PortableServiceLoader.getCoreModelService().create(IOutputLog.class);
			outputLog.setObjectId(objectId);
			outputLog.setObjectType(objectType);
			outputLog.setCreatorId(getActiveUserId());
			outputLog.setOutputter(OrderHistoryService.class.getName());
			outputLog.setDate(LocalDate.now());
			outputLog.setOutputterStatus(updatedJson);
			PortableServiceLoader.getCoreModelService().save(outputLog);
		}
	}

}
