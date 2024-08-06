package ch.elexis.core.ui.views.provider;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.FaelleView;
import ch.rgw.tools.TimeTool;

/**
 * @since 3.0.0 extracted from {@link FaelleView}
 */
public class FaelleLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof ICoverage) {
			ICoverage fall = (ICoverage) element;
			cell.setText(fall.getLabel());
			if (CoverageServiceHolder.get().isValid(fall)) {
				cell.setImage(Images.IMG_OK.getImage());
			} else {
				cell.setImage(Images.IMG_FEHLER.getImage());
			}
		}
		super.update(cell);
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof ICoverage) {
			ICoverage fall = (ICoverage) element;
			return new TimeTool(fall.getLastupdate()).toString(TimeTool.FULL_GER);
		}
		return null;
	}
}
