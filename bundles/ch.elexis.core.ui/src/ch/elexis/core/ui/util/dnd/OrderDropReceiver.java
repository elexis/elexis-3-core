package ch.elexis.core.ui.util.dnd;

import java.util.List;

import org.eclipse.swt.dnd.DropTargetEvent;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.views.OrderManagementView;

public final class OrderDropReceiver implements GenericObjectDropTarget.IReceiver {

	private final OrderManagementView view;
	private final IOrderService orderService;

	public OrderDropReceiver(OrderManagementView view, IOrderService orderService) {
		this.view = view;
		this.orderService = orderService;
	}

	@Override
	public void dropped(List<Object> list, DropTargetEvent e) {
		IOrder actOrder = view.getOrder();

		if (actOrder == null || (actOrder.isDone() && !actOrder.getEntries().isEmpty())) {
			return;
		}

		boolean changesMade = false;

		for (Object o : list) {
			IArticle article = null;
			if (o instanceof IArticle) {
				article = (IArticle) o;
			} else if (o instanceof IStockEntry) {
				IStockEntry stockEntry = (IStockEntry) o;
				article = stockEntry.getArticle();
			}
			if (article == null) {
				continue;
			}
			if (actOrder.getEntries().isEmpty()) {
				actOrder = OrderManagementUtil.addItemsToOrder(actOrder, List.of(article), view.getSite().getShell(),
						orderService);
				changesMade = true;
				continue;
			}
			if (actOrder.getEntries().stream().anyMatch(entry -> entry.getState() != OrderEntryState.OPEN)) {
				return;
			}
			actOrder = OrderManagementUtil.addItemsToOrder(actOrder, List.of(article), view.getSite().getShell(),
					orderService);
			changesMade = true;
		}

		if (changesMade) {
			view.tableViewer.getTable().getDisplay().asyncExec(() -> view.reload());
		}
	}

	@Override
	public boolean accept(List<Object> list) {
		IOrder actOrder = view.getOrder();
		if (actOrder == null) {
			return false;
		}
		if (actOrder.isDone() && !actOrder.getEntries().isEmpty()) {
			return false;
		}
		return list.stream().allMatch(o -> (o instanceof IArticle) || (o instanceof IStockEntry));
	}
}
