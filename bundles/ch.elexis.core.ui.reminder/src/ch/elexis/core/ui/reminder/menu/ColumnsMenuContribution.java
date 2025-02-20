 
package ch.elexis.core.ui.reminder.menu;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import ch.elexis.core.ui.reminder.part.ReminderTablesPart;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn;

public class ColumnsMenuContribution {

	@AboutToShow
	public void aboutToShow(MPart mpart, List<MMenuElement> items) {
		List<ReminderColumn> activeColumns = Collections.emptyList();
		if (mpart != null && mpart.getObject() instanceof ReminderTablesPart) {
			activeColumns = ((ReminderTablesPart) mpart.getObject()).getColumns();
		}

		for (ReminderColumn column : ReminderColumn.getAllAvailable()) {
			MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
			dynamicItem.setType(ItemType.CHECK);
			dynamicItem.setLabel(column.getName());
			dynamicItem.setContributionURI("bundleclass://ch.elexis.core.ui.reminder/" + getClass().getName()); //$NON-NLS-1$
			dynamicItem.setSelected(activeColumns.contains(column));
			dynamicItem.getTransientData().put("column", column);
			items.add(dynamicItem);
		}
	}

	@Execute
	private void toggleColumn(MPart mpart, MDirectMenuItem menuItem) {
		if (mpart != null && mpart.getObject() instanceof ReminderTablesPart) {
			ReminderColumn selectedColumn = (ReminderColumn) menuItem.getTransientData().get("column");
			List<ReminderColumn> activeColumns = ((ReminderTablesPart) mpart.getObject()).getColumns();
			if (activeColumns.contains(selectedColumn)) {
				activeColumns.remove(selectedColumn);
			} else {
				activeColumns.add(selectedColumn);
			}
			((ReminderTablesPart) mpart.getObject()).setColumns(activeColumns);
		}
	}
}