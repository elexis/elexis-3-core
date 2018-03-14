package ch.elexis.core.ui.views.provider;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Fall;

public class FaelleLabelProvider extends DefaultLabelProvider {
	
	@Override
	public Image getColumnImage(final Object element, final int columnIndex){
		if (element instanceof Fall) {
			Fall fall = (Fall) element;
			if (fall.isValid()) {
				return Images.IMG_OK.getImage();
			} else {
				return Images.IMG_FEHLER.getImage();
			}
		}
		return super.getColumnImage(element, columnIndex);
	}
	
}
