package ch.elexis.core.findings.templates.ui.util;

import org.eclipse.swt.graphics.Image;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.ui.icons.Images;

public class FindingsTemplateUtil {
	
	public static Image getImage(Object object){
		if (object instanceof FindingsTemplate) {
			FindingsTemplate findingsTemplate = (FindingsTemplate) object;
			if (findingsTemplate.getInputData() instanceof InputDataGroup) {
				return Images.IMG_DOCUMENT_STACK.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataGroupComponent) {
				return Images.IMG_DOCUMENT_STAND_UP.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataNumeric) {
				return Images.IMG_DOCUMENT.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataText) {
				return Images.IMG_DOCUMENT.getImage();
			}
		} else if (object instanceof FindingsTemplates) {
			return Images.IMG_FOLDER.getImage();
		}
		return null;
	}
}
