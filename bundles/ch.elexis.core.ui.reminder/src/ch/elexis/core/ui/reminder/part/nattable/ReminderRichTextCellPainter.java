package ch.elexis.core.ui.reminder.part.nattable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.nebula.richtext.RichTextCellPainter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.jsoup.Jsoup;

import ch.elexis.core.ui.util.FilterNonPrintableModifyListener;

public class ReminderRichTextCellPainter extends RichTextCellPainter {

	public ReminderRichTextCellPainter() {
		super(false, false, true);
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);

		String htmlText = getHtmlText(cell, configRegistry);

		Rectangle initialPainterBounds = new Rectangle(bounds.x, bounds.y - this.richTextPainter.getParagraphSpace(),
				bounds.width, bounds.height);
		Rectangle painterBounds = new Rectangle(bounds.x, bounds.y - this.richTextPainter.getParagraphSpace(),
				bounds.width, bounds.height);

		if (getPreferredSize(htmlText, gc, cell).x > painterBounds.width) {
			htmlText = abbreviateText(htmlText, gc, cell, painterBounds.width);
		}

		// if a vertical alignment is set != TOP we need to update the bounds
		// Note:
		// to make the vertical alignment handling work correctly, you need to
		// use at least Nebula 3.0, as it contains the necessary fix for the
		// content height calculation. In case you can not consume Nebula >= 3.0
		// as it requires Java 8, it is recommended to configure
		// VerticalAlignmentEnum.TOP to get the same result as in previous
		// versions of the RichTextPainter
		VerticalAlignmentEnum verticalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
		if (verticalAlignment != VerticalAlignmentEnum.TOP) {

			this.richTextPainter.preCalculate(htmlText, gc, painterBounds, false);
			int contentHeight = this.richTextPainter.getPreferredSize().y
					- 2 * this.richTextPainter.getParagraphSpace();
			int verticalAlignmentPadding = CellStyleUtil.getVerticalAlignmentPadding(cellStyle, painterBounds,
					contentHeight);
			painterBounds.y = painterBounds.y + verticalAlignmentPadding;
			painterBounds.height = contentHeight;
		}

//		Color originalBackground = gc.getBackground();
//		gc.setBackground(GUIHelper.COLOR_WHITE);
//		gc.fillRectangle(bounds);
//		gc.setBackground(originalBackground);

		this.richTextPainter.paintHTML(htmlText, gc, painterBounds);

		int height = this.richTextPainter.getPreferredSize().y - 2 * this.richTextPainter.getParagraphSpace();
		if (performRowResize(height, initialPainterBounds)) {
			cell.getLayer()
					.doCommand(new RowResizeCommand(cell.getLayer(), cell.getRowPosition(),
							GUIHelper.convertVerticalDpiToPixel(height, configRegistry)
									+ (cell.getBounds().height - bounds.height)));
		}

		if (performColumnResize(this.richTextPainter.getPreferredSize().x, initialPainterBounds)) {
			cell.getLayer().doCommand(new ColumnResizeCommand(cell.getLayer(), cell.getColumnPosition(),
					GUIHelper.convertHorizontalDpiToPixel(this.richTextPainter.getPreferredSize().x, configRegistry)
							+ (cell.getBounds().width - bounds.width)));
		}
	}

	private Point getPreferredSize(String htmlText, GC gc, ILayerCell cell) {
		this.richTextPainter.preCalculate(htmlText, gc, new Rectangle(0, 0, 0, cell.getBounds().height), false);
		return this.richTextPainter.getPreferredSize();
	}

	private String abbreviateText(String htmlText, GC gc, ILayerCell cell, int maxWidth) {
		String ret = FilterNonPrintableModifyListener.filterNonPrintable(htmlText);
		ret = ret.trim().replaceAll("\r\n", " ");
		ret = ret.trim().replaceAll(" +", " ");
		while (getPreferredSize(ret, gc, cell).x > maxWidth) {
			String maxText = getMaxText(ret).replaceAll("§", "'&sect;'").replaceAll("&", "&amp;");
			if (maxText.length() < 5) {
				break;
			}
			ret = ret.replace(maxText, StringUtils.abbreviate(maxText, maxText.length() - 1));
		}
		return ret;
	}

	private String getMaxText(String ret) {
		if (ret.indexOf("!! </span></strong>") > 0) {
			ret = ret.substring(ret.indexOf("!! </span></strong>") + "!! </span></strong>".length());
		}
		String line1 = ret;
		String line2 = null;
		if (line1.indexOf("<br />") > 0) {
			line1 = ret.substring(0, ret.indexOf("<br />"));
			line2 = ret.substring(ret.indexOf("<br />") + "<br />".length());
			line1 = Jsoup.parse(line1).text();
			line2 = Jsoup.parse(line2).text();
		} else {
			line1 = Jsoup.parse(line1).text();
		}

		if (line2 != null) {
			return line1.length() >= line2.length() ? line1 : line2;
		}
		return line1;
	}

	@Override
	protected String getHtmlText(ILayerCell cell, IConfigRegistry configRegistry) {
		String text = super.getHtmlText(cell, configRegistry);
		if (StringUtils.isNotBlank(text) && text.indexOf("<addition>") > -1) {
			text = text.substring(0, text.indexOf("<addition>"));
		}
		return text.replaceAll("§", "'&sect;'").replaceAll("&", "&amp;");
	}
}
