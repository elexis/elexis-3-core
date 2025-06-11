package ch.elexis.core.ui.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.BillingSystemColorHelper;

public class CoverageColorLabelProvider extends LabelProvider
		implements org.eclipse.jface.viewers.ITableLabelProvider, org.eclipse.jface.viewers.ITableColorProvider {
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ICoverage) {
			return ((ICoverage) element).getLabel();
		}
		return StringUtils.EMPTY;
	}

	@Override
	public org.eclipse.swt.graphics.Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		if (element instanceof ICoverage) {
			String sys = ((ICoverage) element).getBillingSystem().getName();
			String hex = BillingSystemColorHelper.getMixedHexColorForBillingSystem(sys, 80);
			return UiDesk.getColorFromRGB(hex);
		}
		return null;
	}
}
