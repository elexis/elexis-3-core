package ch.elexis.core.spotlight.ui.controls;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpotlightResultLabelProvider extends LabelProvider
		implements
			ITableLabelProvider,
			ITableColorProvider,
			ITableFontProvider {

	private Font defaultFont;
	private Font categoryFont;

	public SpotlightResultLabelProvider(Font defaultFont, Font categoryFont) {
		this.defaultFont = defaultFont;
		this.categoryFont = categoryFont;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		if (element instanceof Category) {
			return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		}
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		if (element instanceof Category) {
			return categoryFont;
		}
		return defaultFont;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {

		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 1) {
			if (element instanceof ISpotlightResultEntry) {
				return ((ISpotlightResultEntry) element).getLabel();
			}
			Category category = (Category) element;
			switch (category) {
				case ENCOUNTER :
					return "KONSULTATION";
				default :
					return category.name();
			}
		}
		return "";
	}

}
