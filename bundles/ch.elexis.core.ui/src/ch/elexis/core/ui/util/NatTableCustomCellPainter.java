package ch.elexis.core.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class NatTableCustomCellPainter extends TextPainter {
	
	BackgroundPainter bgPainter = new BackgroundPainter();
	
	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle,
		IConfigRegistry configRegistry){
		if (paintBg) {
			bgPainter.paintCell(cell, gc, rectangle, configRegistry);
		}
		
		if (paintFg) {
			Rectangle originalClipping = gc.getClipping();
			gc.setClipping(rectangle.intersection(originalClipping));
			
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			setupGCFromConfig(gc, cellStyle);
			
			int fontHeight = gc.getFontMetrics().getHeight();
			String text = convertDataType(cell, configRegistry);
			
			// Draw Text
			text = getTextToDisplay(cell, gc, rectangle.width, text);
			
			int numberOfNewLines = getNumberOfNewLines(text);
			
			//if the content height is bigger than the available row height
			//we're extending the row height (only if word wrapping is enabled)
			int contentHeight = (fontHeight * numberOfNewLines) + (spacing * 2);
			int contentToCellDiff = (cell.getBounds().height - rectangle.height);
			
			if (performRowResize(contentHeight, rectangle)) {
				ILayer layer = cell.getLayer();
				layer.doCommand(new RowResizeCommand(layer, cell.getRowPosition(),
					contentHeight + contentToCellDiff));
			}
			
			//draw every line by itself
			int yStartPos = rectangle.y
				+ CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight);
			String[] lines = text.split("\n"); //$NON-NLS-1$
			for (String line : lines) {
				int lineContentWidth = Math.min(getLengthFromCache(gc, line), rectangle.width);
				
				drawLine(gc, yStartPos,
					rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle,
						lineContentWidth) + spacing,
					line);
				
				//after every line calculate the y start pos new
				yStartPos += fontHeight;
			}
			
			gc.setClipping(originalClipping);
		}
	}
	
	private void drawLine(GC gc, int yStartPos, int xStartPos, String line){
		List<TextPart> textParts = getTextParts(line);
		for (TextPart textPart : textParts) {
			xStartPos = drawTextPart(gc, yStartPos, xStartPos, textPart);
		}
	}
	
	private List<TextPart> getTextParts(String line){
		List<TextPart> ret = new ArrayList<>();
		String[] parts = null;
		if (line.contains("</strong>")) {
			parts = line.split("</strong>");
		}
		if (line.contains("</ strong>")) {
			parts = line.split("</ strong>");
		}
		if (parts != null && parts.length > 0) {
			for (String string : parts) {
				if (string.startsWith("<strong>")) {
					string = string.replaceAll("<strong>", "");
					ret.add(new TextPart(string, TextPart.PartStyle.BOLD));
				} else {
					ret.add(new TextPart(string, TextPart.PartStyle.NORMAL));
				}
			}
		} else {
			ret.add(new TextPart(line, TextPart.PartStyle.NORMAL));
		}
		
		return ret;
	}
	
	private int drawTextPart(GC gc, int yStartPos, int xStartPos, TextPart text){
		Point textExtent = new Point(0, 0);
		if (text.getStyle() == TextPart.PartStyle.NORMAL) {
			textExtent = gc.stringExtent(text.getText());
			gc.drawText(text.getText(), xStartPos, yStartPos + spacing,
				SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
		} else if (text.getStyle() == TextPart.PartStyle.BOLD) {
			Font origFont = gc.getFont();
			FontDescriptor boldDescriptor =
				FontDescriptor.createFrom(gc.getFont()).setStyle(SWT.BOLD);
			Font boldFont = boldDescriptor.createFont(Display.getDefault());
			gc.setFont(boldFont);
			textExtent = gc.stringExtent(text.getText());
			gc.drawText(text.getText(), xStartPos, yStartPos + spacing,
				SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
			gc.setFont(origFont);
			boldFont.dispose();
		}
		return xStartPos + textExtent.x;
	}
	
	private static class TextPart {
		enum PartStyle
		{
				NORMAL, BOLD
		}
		
		private PartStyle style;
		private String text;
		
		public TextPart(String text, PartStyle style){
			this.text = text;
			this.style = style;
		}
		
		public PartStyle getStyle(){
			return style;
		}
		
		public String getText(){
			return text;
		}
	}
}
