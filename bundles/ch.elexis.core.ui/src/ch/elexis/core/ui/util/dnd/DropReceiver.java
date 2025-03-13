package ch.elexis.core.ui.util.dnd;

import java.util.List;

import org.eclipse.swt.dnd.DropTargetEvent;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.views.OrderManagementView;

public final class DropReceiver implements GenericObjectDropTarget.IReceiver {

	private final OrderManagementView view;

	public DropReceiver(OrderManagementView view) {
		this.view = view;
	}

	@Override
	public void dropped(List<Object> list, DropTargetEvent e) {
		IOrder actOrder = view.getOrder();

		if (actOrder == null || (actOrder.isDone() && !actOrder.getEntries().isEmpty())) {
			return;
		}

		boolean changesMade = false;

		for (Object o : list) {
			if (o instanceof IArticle) {
				IArticle article = (IArticle) o;

				if (actOrder.getEntries().isEmpty()) {
					actOrder = OrderManagementUtil.addItemsToOrder(actOrder, List.of(article),
							view.getSite().getShell());
					changesMade = true;
					continue;
				}

				// Falls ein Eintrag nicht mehr "OPEN" ist, wird nichts hinzugefügt
				if (actOrder.getEntries().stream().anyMatch(entry -> entry.getState() != OrderEntryState.OPEN)) {
					return;
				}

				actOrder = OrderManagementUtil.addItemsToOrder(actOrder, List.of(article), view.getSite().getShell());
				changesMade = true;
			}
		}

		if (changesMade) {
			view.table.getDisplay().asyncExec(() -> {
				view.refresh();
				view.loadOpenOrders();
				view.loadCompletedOrders();
			});
		}
	}

	@Override
	public boolean accept(List<Object> list) {
		IOrder actOrder = view.getOrder();
		return actOrder != null && (!actOrder.isDone() || actOrder.getEntries().isEmpty())
				&& list.stream().allMatch(o -> o instanceof IArticle);
	}
}
