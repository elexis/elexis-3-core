package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IStock;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;
import ch.elexis.core.ui.mediorder.MediorderStockState;

public class FilterByOrderStatusHandler {

	@Execute
	public Object execute(MPart part, MHandledToolItem item) throws org.eclipse.core.commands.ExecutionException {
		MediorderPart mediorderPart = (MediorderPart) part.getObject();
		ToolItem widget = (ToolItem) item.getWidget();

		Map<Integer, String> stockStateMap = createValues();

		if (!widget.getSelection()) {
			mediorderPart.setFilterActive(false);
			mediorderPart.refresh();
		} else {
			ListDialog selectFilterDialog = new ListDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			selectFilterDialog.setContentProvider(ArrayContentProvider.getInstance());
			selectFilterDialog.setInput(stockStateMap.keySet().toArray());
			selectFilterDialog.setTitle(Messages.Core_Filter);
			selectFilterDialog.setMessage(Messages.Mediorder_filter_by_status);
			selectFilterDialog.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					Integer key = (Integer) element;
					return stockStateMap.getOrDefault(key, null);
				}
			});

			if (selectFilterDialog.open() == ListDialog.OK) {
				Object[] selection = selectFilterDialog.getResult();
				Integer value = (Integer) selection[0];

				mediorderPart.setCurrentFilterValue(value);
				mediorderPart.setFilterActive(true);
				mediorderPart.setFilteredStocks(filterPatientStock(value));
				mediorderPart.refresh();
			} else {
				widget.setSelection(false);
			}
		}
		return null;

	}

	private Map<Integer, String> createValues() {
		Map<Integer, String> map = new HashMap<>();
		for (MediorderStockState state : MediorderStockState.values()) {
			map.put(state.ordinal(), state.getLocaleText());
		}
		return map;
	}

	private List<IStock> filterPatientStock(Integer value) {
		List<IStock> stocks = StockServiceHolder.get().getAllPatientStock();
		return stocks.stream().filter(stock -> {
			Integer state = MediorderPartUtil.calculateStockState(stock);
			return state != null && state.equals(value);
		}).toList();

	}
}