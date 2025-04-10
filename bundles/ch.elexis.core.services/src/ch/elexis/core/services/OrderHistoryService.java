package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.internal.model.OrderHistoryEntry;


public class OrderHistoryService implements IOrderHistoryService {

		private static final Logger logger = LoggerFactory.getLogger(OrderHistoryService.class);

		public void logCreateOrder(IOrder order) {
			logOrderStatus(order, "Created", null); //$NON-NLS-1$
		}

		public void logEdit(IOrder order, IOrderEntry entry, int oldValue, int newValue) {
			if (oldValue == newValue)
				return;

			String details = entry.getArticle().getLabel() + " changed from " + oldValue + " to " + newValue; //$NON-NLS-1$ //$NON-NLS-2$
			logOrderStatus(order, "Edited", details); //$NON-NLS-1$
		}

		public void logDelivery(IOrder order, IOrderEntry entry, int deliveredAmount, int orderAmaunt) {
			String details = deliveredAmount + "x von " + orderAmaunt + " " + entry.getArticle().getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
			logOrderStatus(order, "Delivered", details); //$NON-NLS-1$
		}

		public void logCreateEntry(IOrder order, IOrderEntry entry, int quantity) {
			String details = entry.getArticle().getLabel() + "/" + quantity; //$NON-NLS-1$
			logOrderStatus(order, "ADDMedi", details); //$NON-NLS-1$
		}

		public void logOrder(IOrder order) {
			logOrderStatus(order, "Ordered", null); //$NON-NLS-1$
		}

		public void logDelete(IOrder order) {
			logOrderStatus(order, "Deleted", null); //$NON-NLS-1$
		}

		public void logChangedAmount(IOrder order, IOrderEntry entry, int oldAmount, int newAmount) {
			if (oldAmount == newAmount) {
				return;
			}

			String action;
			String details;
			String articleLabel = entry.getArticle().getLabel();

			if (oldAmount == 0) {
				action = "Added";
				details = articleLabel + " (Neu: " + newAmount + ")";
			} else if (newAmount > oldAmount) {
				int diff = newAmount - oldAmount;
				action = "Increased";
				details = articleLabel + " (" + oldAmount + " \u2192 " + newAmount + ", +" + diff + ")";
			} else {
				int diff = oldAmount - newAmount;
				action = "Decreased";
				details = articleLabel + " (" + oldAmount + " \u2192 " + newAmount + ", -" + diff + ")";
			}

			logOrderStatus(order, action, details);
		}


		public void logCompleteDelivery(IOrder order) {
			if (order == null) {
				return;
			}

			String details = ch.elexis.core.l10n.Messages.BestellView_OrderIsClosed;
			logOrderStatus(order, "CompleteDelivery", details); //$NON-NLS-1$
		}

		public void logRemove(IOrder order, IOrderEntry entry) {
			if (order == null || entry == null)
				return;

			String details = entry.getArticle().getLabel() + "/" + entry.getAmount(); //$NON-NLS-1$
			logOrderStatus(order, "RemovedMedi", details); //$NON-NLS-1$
		}

		public void logOrderSent(IOrder order, boolean sent) {
			if (order == null)
				return;

			String method = sent ? ch.elexis.core.l10n.Messages.Outputter_Sent : "Printed"; //$NON-NLS-1$
			logOrderStatus(order, "Ordered", method); //$NON-NLS-1$
		}

		public void logSupplierAdded(IOrder order, IOrderEntry entry, String supplier) {
			if (order == null || entry == null || supplier == null || supplier.isEmpty())
				return;

			String details = entry.getArticle().getLabel();
			logOrderStatus(order, "SupplierAdded", details, supplier); //$NON-NLS-1$
		}

		private void logOrderStatus(IOrder order, String action, String details) {
			logOrderStatus(order, action, details, null);
		}

		private void logOrderStatus(IOrder order, String action, String details, String extraInfo) {
			if (order == null)
				return;
			String userId = ContextServiceHolder.get().getActiveUser().map(user -> user.getId()).orElse("Unknown"); //$NON-NLS-1$
			OrderHistoryEntry entry = new OrderHistoryEntry(action, userId, details, extraInfo);
			saveLogEntry(order, entry);
		}

		private void saveLogEntry(IOrder order, OrderHistoryEntry entry) {
			if (order == null)
				return;

		    IQuery<IOutputLog> query = CoreModelServiceHolder.get().getQuery(IOutputLog.class);
		    query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.EQUALS, order.getId());

		    IOutputLog existingLog = query.execute().isEmpty() ? null : query.execute().get(0);
		    Gson gson = new Gson();
		    List<OrderHistoryEntry> logList = new ArrayList<>();

		    if (existingLog != null) {
		        String existingJson = existingLog.getOutputterStatus();
		        try {
		            JsonElement jsonElement = JsonParser.parseString(existingJson);
		            if (jsonElement.isJsonArray()) {
		                logList = gson.fromJson(jsonElement, new TypeToken<List<OrderHistoryEntry>>() {}.getType());
		            }
		        } catch (Exception e) {
					logger.error("Error when parsing the existing logs: " + e.getMessage()); //$NON-NLS-1$
		        }
		    }

			boolean exists = logList.stream()
					.anyMatch(e -> e.getAction().equals(entry.getAction()) && e.getUserId().equals(entry.getUserId())
							&& Objects.equals(e.getDetails(), entry.getDetails())
							&& ((e.getExtraInfo() == null && entry.getExtraInfo() == null)
									|| (e.getExtraInfo() != null && e.getExtraInfo().equals(entry.getExtraInfo()))));

			if (!exists) {
				logList.add(entry);

			}

		    String updatedJson = gson.toJson(logList);
		    if (existingLog != null) {
		        existingLog.setOutputterStatus(updatedJson);
		        CoreModelServiceHolder.get().save(existingLog);
		    } else {
		        IOutputLog outputLog = CoreModelServiceHolder.get().create(IOutputLog.class);
		        outputLog.setObjectId(order.getId());
		        outputLog.setObjectType(order.getClass().getName());
				outputLog.setCreatorId(
						ContextServiceHolder.get().getActiveUser().map(user -> user.getId()).orElse("Unknown")); //$NON-NLS-1$
				outputLog.setOutputter(OrderHistoryService.class.getName());
		        outputLog.setDate(LocalDate.now());
		        outputLog.setOutputterStatus(updatedJson);
		        CoreModelServiceHolder.get().save(outputLog);
		    }
		}

	}


