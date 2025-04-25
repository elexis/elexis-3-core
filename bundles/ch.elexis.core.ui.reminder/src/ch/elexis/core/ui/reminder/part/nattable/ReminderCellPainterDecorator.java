package ch.elexis.core.ui.reminder.part.nattable;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class ReminderCellPainterDecorator extends CellPainterDecorator {

	public ReminderCellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge,
			ICellPainter decoratorCellPainter) {
		super(baseCellPainter, cellEdge, decoratorCellPainter);
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle adjustedCellBounds, IConfigRegistry configRegistry) {
		Rectangle painterBounds = new Rectangle(adjustedCellBounds.x + 2 , adjustedCellBounds.y + 2,
				adjustedCellBounds.width - 4, adjustedCellBounds.height - 4);

		Rectangle baseCellPainterBounds = getBaseCellPainterBounds(cell, gc, painterBounds, configRegistry);
		Rectangle decoratorCellPainterBounds = getDecoratorCellPainterBounds(cell, gc, painterBounds,
				configRegistry);

		Color originalBg = gc.getBackground();
		gc.setBackground(brighten(CellStyleUtil.getCellStyle(cell, configRegistry)
				.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR)));
		gc.fillRectangle(adjustedCellBounds);
		gc.setBackground(originalBg);

		if (getBaseCellPainter() != null) {
			getBaseCellPainter().paintCell(cell, gc, baseCellPainterBounds, configRegistry);
		}

		if (getDecoratorCellPainter() != null) {
			getDecoratorCellPainter().paintCell(cell, gc, decoratorCellPainterBounds, configRegistry);
		}

		gc.setClipping(adjustedCellBounds);
		// Save GC settings
		Color originalForeground = gc.getForeground();
		int originalLineWidth = gc.getLineWidth();
		int originalLineStyle = gc.getLineStyle();

		Integer gridLineWidth = configRegistry.getConfigAttribute(CellConfigAttributes.GRID_LINE_WIDTH,
				DisplayMode.NORMAL, cell.getConfigLabels());
		int adjustment = (gridLineWidth == null || gridLineWidth == 1) ? 0 : Math.round(gridLineWidth.floatValue() / 2);

		int borderThickness = 2;
		gc.setLineWidth(2);

		Rectangle borderArea = new Rectangle(adjustedCellBounds.x, adjustedCellBounds.y, adjustedCellBounds.width,
				adjustedCellBounds.height);
		if (borderThickness >= 1) {
			int shift = 0;
			int areaShift = 0;
			if ((borderThickness % 2) == 0) {
				shift = borderThickness / 2;
				areaShift = (shift * 2);
			} else {
				shift = borderThickness / 2;
				areaShift = (shift * 2) + 1;
			}
			borderArea.x += (shift + adjustment);
			borderArea.y += (shift + adjustment);
			borderArea.width -= (areaShift + adjustment);
			borderArea.height -= (areaShift + adjustment);
		}

		gc.setLineStyle(LineStyleEnum.toSWT(LineStyleEnum.SOLID));
		gc.setForeground(GUIHelper.COLOR_BLACK);
		gc.drawRectangle(borderArea);

		// Restore GC settings
		gc.setForeground(originalForeground);
		gc.setLineWidth(originalLineWidth);
		gc.setLineStyle(originalLineStyle);
	}

	private Color brighten(Color attributeValue) {
		RGB rgb = attributeValue.getRGB();
		if (rgb.red < 220) {
			rgb.red = rgb.red + 25;
		}
		if (rgb.green < 220) {
			rgb.green = rgb.green + 25;
		}
		if (rgb.blue < 220) {
			rgb.blue = rgb.blue + 25;
		}
		String colorString = rgb.toString();

		JFaceResources.getColorRegistry().put(colorString, rgb);
		return JFaceResources.getColorRegistry().get(colorString);
	}

	@Override
	public Rectangle getDecoratorCellPainterBounds(ILayerCell cell, GC gc, Rectangle adjustedCellBounds,
			IConfigRegistry configRegistry) {
		Rectangle ret = super.getDecoratorCellPainterBounds(cell, gc, adjustedCellBounds, configRegistry);

		ret.y = adjustedCellBounds.y;
		ret.height = adjustedCellBounds.height;

		return ret;
	}
}
