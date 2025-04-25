package ch.elexis.core.ui.reminder.part;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.reminder.part.nattable.ReminderAdditionCellPainter;
import ch.elexis.core.ui.reminder.part.nattable.ReminderCellPainterDecorator;
import ch.elexis.core.ui.reminder.part.nattable.ReminderRichTextCellPainter;

public class ReminderTablesStyleConfiguration extends DefaultNatTableStyleConfiguration {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		hAlign = HorizontalAlignmentEnum.LEFT;
		super.configureRegistry(configRegistry);

		Style selectionStyle = new Style();
		selectionStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		selectionStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		selectionStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT,
				SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT);

		this.cellPainter = new BackgroundPainter(new RichTextCellPainter(false, false, true));
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
				cellPainter);

		ICellPainter decoratedCellPainter = new ReminderCellPainterDecorator(new ReminderRichTextCellPainter(),
				CellEdgeEnum.RIGHT, new ReminderAdditionCellPainter());
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, decoratedCellPainter,
				DisplayMode.NORMAL, "REMINDER");

		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.TOP);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL,
				"REMINDER");
	}
}
