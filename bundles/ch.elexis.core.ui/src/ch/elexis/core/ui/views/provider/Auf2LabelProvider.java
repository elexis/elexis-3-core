package ch.elexis.core.ui.views.provider;

import java.time.LocalDate;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;

public class Auf2LabelProvider extends DefaultLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		if (element instanceof ISickCertificate) {
			ISickCertificate certificate = (ISickCertificate) element;
			LocalDate endDate = certificate.getEnd();
			if (endDate != null && !endDate.isBefore(LocalDate.now())) {
				return Images.IMG_OK.getImage();
			}
		}
		return super.getColumnImage(element, columnIndex);
	}
}
