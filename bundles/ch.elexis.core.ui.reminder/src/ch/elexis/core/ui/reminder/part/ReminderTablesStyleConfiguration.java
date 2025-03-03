package ch.elexis.core.ui.reminder.part;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class ReminderTablesStyleConfiguration extends DefaultNatTableStyleConfiguration {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		hAlign = HorizontalAlignmentEnum.LEFT;
		super.configureRegistry(configRegistry);

		Style selectionStyle = new Style();
		selectionStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		selectionStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
				Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
		selectionStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT,
				SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, selectionStyle, DisplayMode.SELECT);

		cellPainter = new RichTextCellPainter(true, false, true);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new BackgroundPainter(cellPainter));

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
				new CellPainterDecorator(cellPainter, CellEdgeEnum.LEFT,
						new ImagePainter(Images.IMG_TICK.getImage(ImageSize._16x16_DefaultIconSize))),
				DisplayMode.NORMAL, "REMINDER");
	}
}
