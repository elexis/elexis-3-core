package ch.elexis.core.ui.views.provider;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.views.FaelleView;

/**
 * @since 3.0.0 extracted from {@link FaelleView}
 */
public class FaelleLabelProvider extends DefaultLabelProvider {
	
	@Override
	public Image getColumnImage(final Object element, final int columnIndex){
		if (element instanceof ICoverage) {
			ICoverage fall = (ICoverage) element;
			if (CoverageServiceHolder.get().isValid(fall)) {
				return Images.IMG_OK.getImage();
			} else {
				return Images.IMG_FEHLER.getImage();
			}
		}
		return super.getColumnImage(element, columnIndex);
	}
	
}
