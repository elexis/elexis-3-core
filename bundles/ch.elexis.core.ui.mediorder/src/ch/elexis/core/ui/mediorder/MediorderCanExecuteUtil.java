package ch.elexis.core.ui.mediorder;

import java.util.List;

import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IStockEntry;

public class MediorderCanExecuteUtil {

	/**
	 * The articles are billed when they are ordered, so the entries can be closed
	 * as soon as all of them are in stock.
	 *
	 * @param stockEntries
	 * @return
	 */
	public static boolean canExecute(List<IStockEntry> stockEntries) {
		return !stockEntries.isEmpty() && stockEntries.stream()
				.allMatch(e -> MediorderEntryState.IN_STOCK.equals(MediorderUtil.determineState(e)));
	}
}
