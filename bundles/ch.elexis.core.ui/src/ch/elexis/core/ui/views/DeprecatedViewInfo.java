package ch.elexis.core.ui.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class DeprecatedViewInfo {

	private boolean isOldShown = false;

	private String message;

	public DeprecatedViewInfo() {
		this.message = getDefaultMesage();
	}

	public DeprecatedViewInfo(String viewReplacement) {
		this.message = getDefaultMesage(viewReplacement);
	}

	private String getDefaultMesage() {
		return "Die Ansicht %s ist veraltet, und wird nicht mehr unterstützt.";
	}

	private String getDefaultMesage(String viewReplacement) {
		return "Die Ansicht %s ist veraltet, und wird nicht mehr unterstützt.  Bitte verwenden Sie die "
				+ viewReplacement + " Ansicht.";
	}

	/**
	 * Show the message if not already shown- State is kept in this
	 * DeprecatedViewInfo.
	 */
	public void showInfo(String viewTitle) {
		if (!isOldShown) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Ansicht veraltet",
					String.format(message, viewTitle));
			isOldShown = true;
		}
	}
}
