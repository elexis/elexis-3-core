package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IStock;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.mediorder.MediorderStockState;

public class FilterByOrderStatusHandler {

	@Execute
	public Object execute(MPart part, MHandledToolItem item, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
			throws org.eclipse.core.commands.ExecutionException {
		MediorderPart mediorderPart = (MediorderPart) part.getObject();
		ToolItem widget = (ToolItem) item.getWidget();

		Map<Integer, String> stockStateMap = createValues();

		if (!widget.getSelection()) {
			mediorderPart.setFilterActive(false);
			mediorderPart.refresh();
		} else {

			List<Integer> reversedKeys = new ArrayList<>(stockStateMap.keySet());
			Collections.reverse(reversedKeys);

			ListSelectionDialog dialog = ListSelectionDialog.of(reversedKeys.toArray())
					.contentProvider(ArrayContentProvider.getInstance()).labelProvider(new LabelProvider() {
						@Override
						public String getText(Object element) {
							Integer key = (Integer) element;
							return stockStateMap.getOrDefault(key, null);
						}
					})
					.message(Messages.Mediorder_filter_by_status).create(shell);
			dialog.setTitle(Messages.Core_Filter);

			if (dialog.open() == ListSelectionDialog.OK) {
				List<Integer> selections = Arrays.stream(dialog.getResult()).filter(obj -> obj instanceof Integer)
						.map(obj -> (Integer) obj).toList();

				mediorderPart.setCurrentFilterValue(selections);
				mediorderPart.setFilterActive(true);
				mediorderPart.setFilteredStocks(filterPatientStock(selections));
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

	private List<IStock> filterPatientStock(List<Integer> values) {
		List<IStock> stocks = StockServiceHolder.get().getAllPatientStock();
		return stocks.stream().filter(stock -> {
			Integer state = MediorderUtil.calculateStockState(stock);
			return state != null && values.contains(state);
		}).toList();
		
	}
}