package ch.elexis.core.ui.util;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.OrderLabelPrintConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.holder.ConfigServiceHolder;

public class OrderEntryLabelPrinter {

	private static final Logger logger = LoggerFactory.getLogger(OrderEntryLabelPrinter.class);

	private OrderEntryLabelPrinter() {
	}

	public static boolean isAutoPrintEnabled() {
		return ConfigServiceHolder.getGlobal(Preferences.MEDIORDER_AUTO_PRINT_LABELS,
				Preferences.MEDIORDER_AUTO_PRINT_LABELS_DEFAULT);
	}

	public static boolean isPatientStockEntry(IOrderEntry entry) {
		return entry != null && isPatientStock(entry.getStock());
	}

	public static boolean isPatientStock(IStock stock) {
		IPerson owner = stock != null ? stock.getOwner() : null;
		return owner != null && owner.isPatient();
	}

	public static String buildEntriesParameter(Map<IOrderEntry, Integer> deliveredEntries) {
		StringJoiner joiner = new StringJoiner(OrderLabelPrintConstants.ENTRY_SEPARATOR);
		deliveredEntries.forEach((entry, amount) -> {
			if (amount != null && amount > 0 && isPatientStockEntry(entry)) {
				joiner.add(entry.getId() + OrderLabelPrintConstants.AMOUNT_SEPARATOR + amount);
			}
		});
		return joiner.toString();
	}

	public static String buildStatusMessage(Map<String, Integer> result) {
		int printed = result.getOrDefault(OrderLabelPrintConstants.RESULT_PRINTED, 0);
		int skipped = result.getOrDefault(OrderLabelPrintConstants.RESULT_SKIPPED_NO_PRINTER, 0);
		if (skipped > 0) {
			return MessageFormat.format(Messages.Mediorder_labels_printed_skipped, printed, skipped);
		} else if (printed > 0) {
			return MessageFormat.format(Messages.Mediorder_labels_printed, printed);
		}
		return null;
	}

	public static void showStatus(IStatusLineManager statusLine, Map<String, Integer> result) {
		String message = buildStatusMessage(result);
		if (statusLine == null || message == null) {
			return;
		}
		if (result.getOrDefault(OrderLabelPrintConstants.RESULT_SKIPPED_NO_PRINTER, 0) > 0) {
			statusLine.setMessage(null);
			statusLine.setErrorMessage(message);
		} else {
			statusLine.setErrorMessage(null);
			statusLine.setMessage(message);
		}
	}

	public static String buildStockEntriesParameter(Map<IStockEntry, Integer> takenEntries) {
		StringJoiner joiner = new StringJoiner(OrderLabelPrintConstants.ENTRY_SEPARATOR);
		takenEntries.forEach((entry, amount) -> {
			if (amount != null && amount > 0 && entry != null && isPatientStock(entry.getStock())) {
				joiner.add(entry.getId() + OrderLabelPrintConstants.AMOUNT_SEPARATOR + amount);
			}
		});
		return joiner.toString();
	}

	public static Map<String, Integer> print(Map<IOrderEntry, Integer> deliveredEntries) {
		return execute(OrderLabelPrintConstants.PARAM_ENTRIES, buildEntriesParameter(deliveredEntries));
	}

	/**
	 * Print the labels of articles taken from stock into patient stock entries.
	 *
	 * @param takenEntries the patient stock entries with the taken amount
	 * @return the result of the label print command, see
	 *         {@link OrderLabelPrintConstants#RESULT_PRINTED}
	 */
	public static Map<String, Integer> printTakenFromStock(Map<IStockEntry, Integer> takenEntries) {
		return execute(OrderLabelPrintConstants.PARAM_STOCK_ENTRIES, buildStockEntriesParameter(takenEntries));
	}

	private static Map<String, Integer> execute(String parameterId, String entriesParameter) {
		if (entriesParameter.isEmpty()) {
			return Collections.emptyMap();
		}
		try {
			ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
			IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
			Command command = commandService.getCommand(OrderLabelPrintConstants.COMMAND_ID);
			if (command == null || !command.isDefined()) {
				logger.debug("No label print command [{}] available", OrderLabelPrintConstants.COMMAND_ID); //$NON-NLS-1$
				return Collections.emptyMap();
			}
			Map<String, String> params = new HashMap<>();
			params.put(parameterId, entriesParameter);
			Object result = handlerService
					.executeCommand(ParameterizedCommand.generateCommand(command, params), null);
			return toResultMap(result);
		} catch (Exception e) {
			logger.error("Error printing labels [{}={}]", parameterId, entriesParameter, e); //$NON-NLS-1$
			return Collections.emptyMap();
		}
	}

	private static Map<String, Integer> toResultMap(Object result) {
		Map<String, Integer> ret = new HashMap<>();
		if (result instanceof Map<?, ?> map) {
			map.forEach((key, value) -> {
				if (key instanceof String && value instanceof Integer) {
					ret.put((String) key, (Integer) value);
				}
			});
		}
		return ret;
	}
}
