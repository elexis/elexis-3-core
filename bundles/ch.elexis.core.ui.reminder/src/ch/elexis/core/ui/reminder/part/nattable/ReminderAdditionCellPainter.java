package ch.elexis.core.ui.reminder.part.nattable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.AbstractTextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class ReminderAdditionCellPainter extends AbstractTextPainter {

	private Image tick;
	private Image link;

	public ReminderAdditionCellPainter() {
		tick = Images.IMG_TICK.getImage(ImageSize._16x16_DefaultIconSize);
		link = Images.IMG_LINK.getImage(ImageSize._16x16_DefaultIconSize);
	}

	@Override
    public int getPreferredWidth(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		String text = getAdditionText(cell, configRegistry);
		int textWidth = getLengthFromCache(gc, text) + (this.spacing * 2) + 1;
		return textWidth + 2 + tick.getBounds().width;
    }

	private String getAdditionText(ILayerCell cell, IConfigRegistry configRegistry) {
		String text = convertDataType(cell, configRegistry);
		if (StringUtils.isNotBlank(text) && text.indexOf("<addition>") > -1) {
			String additionText = text.substring(text.indexOf("<addition>") + "<addition>".length(),
					text.indexOf("</addition>"));
			return additionText;
		}
		return StringUtils.EMPTY;
	}

	@Override
	public int getPreferredHeight(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		if (hasLink(cell, configRegistry)) {
			return tick.getBounds().height + 4 + link.getBounds().height;
		} else {
			return tick.getBounds().height;
		}
	}

	private boolean hasLink(ILayerCell cell, IConfigRegistry configRegistry) {
		return getAdditionText(cell, configRegistry).contains("<link>");
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Rectangle originalClipping = gc.getClipping();
		gc.setClipping(bounds.intersection(originalClipping));

		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);

		int fontHeight = gc.getFontMetrics().getHeight();
		String text = getAdditionText(cell, configRegistry);
		int textWidth = getLengthFromCache(gc, text) + (this.spacing * 2) + 1;

		int numberOfNewLines = getNumberOfNewLines(text);

		// if the content height is bigger than the available row height
		// we're extending the row height (only if word wrapping is enabled)
		int contentHeight = (fontHeight * numberOfNewLines) + (this.lineSpacing * (numberOfNewLines - 1))
				+ (this.spacing * 2);

		if (numberOfNewLines == 1) {
			int contentWidth = Math.min(getLengthFromCache(gc, text), bounds.width);

			gc.drawText(text,
					bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, contentWidth)
							+ this.spacing,
					bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, contentHeight)
							+ this.spacing,
					SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB);

			// start x of line = start x of text
			int x = bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, contentWidth)
					+ this.spacing;
			// y = start y of text
			int y = bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, contentHeight)
					+ this.spacing;
			int length = gc.textExtent(text).x;
			paintDecoration(cellStyle, gc, x, y, length, fontHeight);
		} else {
			// draw every line by itself because of the alignment, otherwise
			// the whole text is always aligned right
			int yStartPos = bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, contentHeight);
			String[] lines = text.split("\n"); //$NON-NLS-1$
			for (String line : lines) {
				int lineContentWidth = Math.min(getLengthFromCache(gc, line), bounds.width);

				gc.drawText(line,
						bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, lineContentWidth)
								+ this.spacing,
						yStartPos + this.spacing, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER | SWT.DRAW_TAB);

				// start x of line = start x of text
				int x = bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, lineContentWidth)
						+ this.spacing;
				// y = start y of text
				int y = yStartPos + this.spacing;
				int length = gc.textExtent(line).x;
				paintDecoration(cellStyle, gc, x, y, length, fontHeight);

				// after every line calculate the y start pos new
				yStartPos += fontHeight;
				yStartPos += this.lineSpacing;
			}
		}

		Rectangle imageBounds = tick.getBounds();
		gc.drawImage(tick,
				textWidth + bounds.x
						+ CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width),
				bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height));

		if (hasLink(cell, configRegistry)) {
			imageBounds = link.getBounds();
			gc.drawImage(link,
					textWidth + bounds.x
							+ CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width),
					bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height)
							+ imageBounds.height + 4);
		}
	}

	@Override
	protected void setNewMinLength(ILayerCell cell, int contentWidth) {
		int cellLength = cell.getBounds().width;
		if (cellLength < contentWidth) {
			ILayer layer = cell.getLayer();
			int columnPosition = cell.getColumnPosition();
			if (cell.isSpannedCell()) {
				// if spanned only resize rightmost column and reduce width by
				// left column widths to resize to only the necessary width
				columnPosition = cell.getOriginColumnPosition() + cell.getColumnSpan() - 1;
				for (int i = cell.getOriginColumnPosition(); i < columnPosition; i++) {
					contentWidth -= layer.getColumnWidthByPosition(i);
				}
			}
			layer.doCommand(new ColumnResizeCommand(layer, columnPosition, contentWidth, true));
		}
	}

	@Override
	protected int calculatePadding(ILayerCell cell, int availableLength) {
		return cell.getBounds().width - availableLength;
	}
}
