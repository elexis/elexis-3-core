package ch.elexis.core.ui.views;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.l10n.Messages;

public class DeprecatedViewInfo {

	private boolean isOldShown = false;

	private String successor;

	public DeprecatedViewInfo() {
		this(null);
	}

	public DeprecatedViewInfo(String viewReplacement) {
		this.successor = viewReplacement;
	}

	/**
	 * Show the message if not already shown- State is kept in this
	 * DeprecatedViewInfo.
	 */
	public void showInfo(String viewTitle) {
		if (!isOldShown) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.DeprecatedView_Title,
					getMessage(viewTitle));
			isOldShown = true;
		}
	}

	public String getMessage(String viewTitle) {
		if (StringUtils.isBlank(successor)) {
			return MessageFormat.format(Messages.DeprecatedView_Message, viewTitle);
		}
		return MessageFormat.format(Messages.DeprecatedView_MessageWithSuccessor, viewTitle, successor);
	}

	public boolean isShown() {
		return isOldShown;
	}

	public void markShown() {
		isOldShown = true;
	}
}
