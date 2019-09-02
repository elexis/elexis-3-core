package ch.elexis.core.ui.propertypage;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.types.Country;
import ch.elexis.core.ui.icons.Images;

public class CountryComboLabelProvider extends LabelProvider {
	
	@Override
	public Image getImage(Object element){
		
		switch ((Country) element) {
		case AT:
			return Images.IMG_FLAG_AT.getImage();
		case CH:
			return Images.IMG_FLAG_CH.getImage();
		case DE:
			return Images.IMG_FLAG_DE.getImage();
		case FR:
			return Images.IMG_FLAG_FR.getImage();
		case IT:
			return Images.IMG_FLAG_IT.getImage();
		case LI:
			return Images.IMG_FLAG_FL.getImage();
		default:
			break;
		}
		
		return null;
	}
	
	@Override
	public String getText(Object element){
		return ((Country) element).value();
	}
	
}
