package ch.elexis.core.ui.mediorder;

import java.util.List;

import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IStockEntry;

public class MediorderCanExecuteUtil {

	public static boolean canExecute(List<IStockEntry> stockEntries) {
		return stockEntries.stream()
				.allMatch(entry -> MediorderUtil.determineState(entry).equals(MediorderEntryState.IN_STOCK)
						&& entry.getCurrentStock() == 0);
	}
}
