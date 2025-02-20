package ch.elexis.core.ui.reminder.part;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.ui.reminder.part.nattable.ReminderSpanningBodyDataProvider;

public class ReminderNatTableToolTip extends DefaultToolTip {

	private NatTable natTable;
	private ReminderSpanningBodyDataProvider dataProvider;

	public ReminderNatTableToolTip(NatTable natTable, ReminderSpanningBodyDataProvider dataProvider) {
		super(natTable, ToolTip.RECREATE, false);
		this.natTable = natTable;
		this.dataProvider = dataProvider;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.ToolTip#getToolTipArea(org.eclipse.swt. widgets
	 * .Event)
	 *
	 * Implementation here means the tooltip is not redrawn unless mouse hover moves
	 * outside of the current cell (the combination of ToolTip.NO_RECREATE style and
	 * override of this method).
	 */
	@Override
	protected Object getToolTipArea(Event event) {
		int col = this.natTable.getColumnPositionByX(event.x);
		int row = this.natTable.getRowPositionByY(event.y);

		return new Point(col, row);
	}

	@Override
	protected String getText(Event event) {
		ILayerCell cell = SelectionUtil.getCell(natTable, dataProvider, event.x, event.y);
		if (SelectionUtil.isHoverCheck(natTable, dataProvider, cell, event.x, event.y)) {
			return "Erledigt mit Doppelklick.";
		} else {
			Object data = SelectionUtil.getData(natTable, dataProvider, event.x, event.y);
			if(data instanceof IReminder) {
				IReminder reminder = (IReminder) data;
				StringBuilder sb = new StringBuilder();
				sb.append("Titel: ").append(reminder.getSubject()).append("\n");
				sb.append("Status: ").append(reminder.getStatus().getLocaleText()).append("\n");
				sb.append("Beschreibung: \n").append(reminder.getMessage());
				return sb.toString();
			}
		}
		return null;
	}
}
