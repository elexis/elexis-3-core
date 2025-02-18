
package ch.elexis.core.pdfbox.ui.parts.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;

import ch.elexis.core.pdfbox.ui.parts.Constants;
import ch.elexis.core.pdfbox.ui.parts.PdfPreviewPart;
import ch.elexis.core.services.IConfigService;
import jakarta.inject.Inject;

public class PdfPreviewPartZoomHandler {

	@Inject
	private IConfigService configService;

	@Execute
	public void execute(MPart pdfPreviewPart, MDirectToolItem toolItem) {

		String zoomLevel = configService.getActiveUserContact(Constants.PREFERENCE_USER_ZOOMLEVEL,
				Constants.PREFERENCE_USER_ZOOMLEVEL_DEFAULT);
		Float _zoomLevel = Float.valueOf(zoomLevel);

		String zoomDirection = toolItem.getTags().get(0);
		if ("ZoomIn".equals(zoomDirection)) {
			if (_zoomLevel < 4) {
				_zoomLevel += 0.2f;
			}
		} else {
			if (_zoomLevel > 0.4f) {
				_zoomLevel -= 0.2f;
			}
		}

		configService.setActiveUserContact(Constants.PREFERENCE_USER_ZOOMLEVEL, _zoomLevel.toString());

		PdfPreviewPart _pdfPreviewPart = (PdfPreviewPart) pdfPreviewPart.getObject();
		_pdfPreviewPart.changeScalingFactor(_zoomLevel);

	}

}