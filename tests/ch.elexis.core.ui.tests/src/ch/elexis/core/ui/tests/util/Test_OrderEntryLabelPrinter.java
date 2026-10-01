package ch.elexis.core.ui.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import ch.elexis.core.constants.OrderLabelPrintConstants;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.ui.util.OrderEntryLabelPrinter;

public class Test_OrderEntryLabelPrinter {

	@Test
	public void isPatientStockEntry() {
		assertTrue(OrderEntryLabelPrinter.isPatientStockEntry(entry("e1", true)));
		assertFalse(OrderEntryLabelPrinter.isPatientStockEntry(entry("e2", false)));
		assertFalse(OrderEntryLabelPrinter.isPatientStockEntry(entryWithoutOwner("e3")));
		assertFalse(OrderEntryLabelPrinter.isPatientStockEntry(null));
	}

	@Test
	public void buildEntriesParameter() {
		Map<IOrderEntry, Integer> delivered = new LinkedHashMap<>();
		delivered.put(entry("patient1", true), 2);
		delivered.put(entry("mandatorStock", false), 3);
		delivered.put(entry("patient2", true), 1);
		delivered.put(entry("patientZero", true), 0);
		delivered.put(entry("patientNegative", true), -1);
		delivered.put(entryWithoutOwner("noOwner"), 1);

		assertEquals("patient1:2;patient2:1", OrderEntryLabelPrinter.buildEntriesParameter(delivered));
	}

	@Test
	public void buildEntriesParameterEmpty() {
		Map<IOrderEntry, Integer> delivered = new LinkedHashMap<>();
		delivered.put(entry("mandatorStock", false), 3);
		assertEquals("", OrderEntryLabelPrinter.buildEntriesParameter(delivered));
	}

	@Test
	public void buildStatusMessage() {
		Map<String, Integer> result = new HashMap<>();
		assertNull(OrderEntryLabelPrinter.buildStatusMessage(result));

		result.put(OrderLabelPrintConstants.RESULT_PRINTED, 3);
		String printed = OrderEntryLabelPrinter.buildStatusMessage(result);
		assertTrue(printed, printed.contains("3"));

		result.put(OrderLabelPrintConstants.RESULT_SKIPPED_NO_PRINTER, 2);
		String skipped = OrderEntryLabelPrinter.buildStatusMessage(result);
		assertTrue(skipped, skipped.contains("3") && skipped.contains("2"));
		assertFalse(printed.equals(skipped));

		result.put(OrderLabelPrintConstants.RESULT_PRINTED, 0);
		String onlySkipped = OrderEntryLabelPrinter.buildStatusMessage(result);
		assertTrue(onlySkipped, onlySkipped.contains("0") && onlySkipped.contains("2"));
	}

	private static IOrderEntry entry(String id, boolean patientOwner) {
		IPerson owner = proxy(IPerson.class, Map.of("isPatient", patientOwner));
		IStock stock = proxy(IStock.class, Map.of("getOwner", owner));
		return proxy(IOrderEntry.class, Map.of("getId", id, "getStock", stock));
	}

	private static IOrderEntry entryWithoutOwner(String id) {
		IStock stock = proxy(IStock.class, Map.of());
		return proxy(IOrderEntry.class, Map.of("getId", id, "getStock", stock));
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> clazz, Map<String, Object> returnValues) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, (p, method, args) -> {
			switch (method.getName()) {
			case "hashCode":
				return System.identityHashCode(p);
			case "equals":
				return p == args[0];
			case "toString":
				return clazz.getSimpleName() + returnValues;
			default:
				if (returnValues.containsKey(method.getName())) {
					return returnValues.get(method.getName());
				}
				return method.getReturnType() == boolean.class ? false : null;
			}
		});
	}
}
