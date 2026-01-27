package ch.elexis.core.ui.e4.fhir.providers;

import java.util.Set;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.fhir.model.interfaces.IFhirBased;
import ch.elexis.core.ui.icons.Images;

public class FhirBasedLabelProvider {

	public static String getText(IFhirBased fb) {
		return fb.getNarrativeLabel();
	}

	public static Image getImage(IFhirBased fb) {
		Set<String> narrativeTags = fb.getNarrativeTags();
		if (narrativeTags.contains("closed")) {
			return Images.IMG_LOCK_CLOSED.getImage();
		}
		if (narrativeTags.contains("valid")) {
			return Images.IMG_OK.getImage();
		}
		if (narrativeTags.contains("invalid")) {
			return Images.IMG_FEHLER.getImage();
		}
		return null;
	}

}
