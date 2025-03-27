package ch.elexis.core.ui.reminder.part;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.ui.reminder.part.nattable.ReminderBodyDataProvider;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn;

public class ReminderTablesConfigLabelsAccumulator implements IConfigLabelAccumulator {

	private ReminderBodyDataProvider dataProvider;
	private ViewportLayer viewportLayer;

	public ReminderTablesConfigLabelsAccumulator(ReminderBodyDataProvider dataProvider, ViewportLayer viewportLayer) {
		this.dataProvider = dataProvider;
		this.viewportLayer = viewportLayer;
	}

	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		rowPosition = viewportLayer.getRowIndexByPosition(rowPosition);
		columnPosition = viewportLayer.getColumnPositionByIndex(columnPosition);

		if (dataProvider.getColumns() != null && !dataProvider.getColumns().isEmpty()) {
			ReminderColumn column = dataProvider.getColumns().get(columnPosition);

			if (dataProvider.getData(columnPosition, rowPosition) == null) {
				configLabels.addLabel("BG_" + column.getName());
			}

			if (dataProvider.getData(columnPosition, rowPosition) instanceof IReminder || rowPosition == 0) {
				configLabels.addLabel("BG_" + column.getName());
			}

			if (dataProvider.getData(columnPosition, rowPosition) instanceof IReminder) {
				configLabels.addLabel("REMINDER");
			}
		}
	}
}
