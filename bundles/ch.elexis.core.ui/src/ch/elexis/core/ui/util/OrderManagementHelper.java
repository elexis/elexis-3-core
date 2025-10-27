package ch.elexis.core.ui.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.ui.constants.OrderConstants;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.OrderManagementView;

/**
 * OrderManagementHelper – contains business logic that should not be in the
 * view. Central class for selection, update, and table functions.
 */
public class OrderManagementHelper {

	/**
	 * Checks which column is editable (ORDERED or DELIVERED).
	 */
	public static int determineEditableColumn(IOrderEntry entry) {
		if (entry == null) {
			return -1;
		}
		if (entry.getState() == OrderEntryState.OPEN) {
			return OrderConstants.OrderTable.ORDERED;
		}
		if (entry.getState() == OrderEntryState.ORDERED || entry.getState() == OrderEntryState.PARTIAL_DELIVER) {
			return OrderConstants.OrderTable.DELIVERED;
		}
		return -1;
	}

	/**
	 * Updates an order entry (e.g., new quantity or delivery).
	 */
	public static void updateOrderEntry(OrderManagementView view, IOrderEntry entry, int newValue) {
		if (entry == null) {
			return;
		}

		IOrder order = view.getOrder();
		if (entry.getState() == OrderEntryState.OPEN) {
			int oldValue = entry.getAmount();
			entry.setAmount(newValue);
			view.getOrderService().getHistoryService().logEdit(order, entry, oldValue, newValue);
		} else {
			OrderManagementUtil.saveSingleDelivery(entry, newValue, view.getOrderService());
		}

		CoreModelServiceHolder.get().save(entry);
		view.getTableViewer().refresh(entry);
	}

	/**
	 * Returns all entries of an order that may be booked.
	 */
	public static List<IOrderEntry> getEligibleEntries(IOrder order) {
		if (order == null) {
			return List.of();
		}
		return order.getEntries().stream()
				.filter(e -> e.getState() != OrderEntryState.OPEN && e.getDelivered() < e.getAmount())
				.collect(Collectors.toList());
	}

	/**
	 * Sets or removes all checkboxes (“Everything delivered”).
	 */
	public static void applySelectAll(OrderManagementView view, boolean checked,
			Map<IOrderEntry, Integer> pendingDeliveredValues) {

		TableViewer viewer = view.getTableViewer();
		if (!(viewer instanceof CheckboxTableViewer checkboxViewer))
			return;

		if (!checked) {
			pendingDeliveredValues.clear();
			checkboxViewer.setAllChecked(false);
		} else {
			List<IOrderEntry> eligible = getEligibleEntries(view.getOrder());
			for (IOrderEntry e : eligible) {
				int rest = Math.max(0, e.getAmount() - e.getDelivered());
				if (rest > 0)
					pendingDeliveredValues.put(e, rest);
			}
			checkboxViewer.setCheckedElements(eligible.toArray(new IOrderEntry[0]));
		}
		viewer.refresh();
		updateSelectAllCheckbox(view, pendingDeliveredValues);
		if (pendingDeliveredValues.isEmpty()) {
			if (!Messages.MedicationComposite_btnConfirm.equals(view.orderButton.getText())) {
				view.updateOrderStatus(view.getOrder());
			}

		} else {
			view.orderButton.setText(Messages.MedicationComposite_btnConfirm);
			view.orderButton.setImage(Images.IMG_TICK.getImage());
		}

	}

	/**
	 * Updates the status of the “Everything delivered” checkbox.
	 */
	public static void updateSelectAllCheckbox(OrderManagementView view,
			Map<IOrderEntry, Integer> pendingDeliveredValues) {

		if (view.selectAllChk == null)
			return;

		boolean usingCheckboxViewer = (view.tableViewer == view.checkboxViewer);
		view.selectAllChk.setEnabled(false);
		view.selectAllChk.setSelection(false);

		if (!usingCheckboxViewer) {
			return;
		}

		List<IOrderEntry> eligible = getEligibleEntries(view.getOrder());
		if (eligible.isEmpty()) {
			view.selectAllChk.setSelection(false);
			view.selectAllChk.setEnabled(false);
			return;
		}

		view.selectAllChk.setEnabled(true);
		long selected = eligible.stream().filter(e -> pendingDeliveredValues.getOrDefault(e, 0) > 0).count();
		view.selectAllChk.setSelection(selected == eligible.size());
	}
}
