 
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
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;

import ch.elexis.core.ui.reminder.part.ReminderTablesPart;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn.Type;

public class ColumnsMenuContribution {

	@AboutToShow
	public void aboutToShow(MPart mpart, List<MMenuElement> items) {
		List<ReminderColumn> activeColumns = Collections.emptyList();
		if (mpart != null && mpart.getObject() instanceof ReminderTablesPart) {
			activeColumns = ((ReminderTablesPart) mpart.getObject()).getColumns();
		}

		List<ReminderColumn> allColumns = ReminderColumn.getAllAvailable();
		List<ReminderColumn> customColumns = allColumns.stream()
				.filter(c -> c.getType() != Type.USER && c.getType() != Type.GROUP).toList();
		List<ReminderColumn> groupColumns = allColumns.stream().filter(c -> c.getType() == Type.GROUP).toList();
		List<ReminderColumn> userColumns = allColumns.stream().filter(c -> c.getType() == Type.USER).toList();

		addColumns(customColumns, activeColumns, items);
		addSeparator("groups", items);
		addColumns(groupColumns, activeColumns, items);
		addSeparator("users", items);
		addColumns(userColumns, activeColumns, items);
	}

	private void addSeparator(String idPostfix, List<MMenuElement> items) {
		MMenuSeparator separator = MMenuFactory.INSTANCE.createMenuSeparator();
		separator.setElementId("ch.elexis.core.ui.reminder.separator." + idPostfix);
		items.add(separator);
	}

	private void addColumns(List<ReminderColumn> columns, List<ReminderColumn> activeColumns,
			List<MMenuElement> items) {
		for (ReminderColumn column : columns) {
			MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
			dynamicItem.setType(ItemType.CHECK);
			dynamicItem.setLabel(column.getFullName());
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