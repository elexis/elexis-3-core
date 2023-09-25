package ch.elexis.core.ui.e4.dialog;

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.l10n.Messages;

public class StatusDialog {

	/**
	 * Present a status information dialog to the user
	 *
	 * @param status   to show
	 * @param showIfOk show the status if its ok
	 */
	public static void show(IStatus status, boolean showIfOk) {
		if (!status.getMessage().isEmpty()) {
			if (status.isOK()) {
				if (showIfOk) {
					showInfo(status);
				}
			} else {
				if (isWarning(status)) {
					showWarning(status);
				} else {
					showError(status);
				}
			}
		}
	}

	private static boolean showStatus(IStatus status, boolean showIfOk) {
		if (!status.getMessage().isEmpty()) {
			if (status.isOK()) {
				return showIfOk;
			}
		}
		return true;
	}

	private static boolean isWarning(IStatus status) {
		return Status.WARNING == status.getSeverity();
	}

	private static void showError(IStatus status) {
		MessageDialog.openError(getShell(), Messages.Core_Error, getStatusMessage(status));
	}

	private static void showWarning(IStatus status) {
		MessageDialog.openWarning(getShell(), Messages.Core_Warning, getStatusMessage(status));
	}

	private static void showInfo(IStatus status) {
		MessageDialog.openInformation(getShell(), Messages.Core_Information, getStatusMessage(status));
	}

	private static String getStatusMessage(IStatus status) {
		if (status instanceof MultiStatus) {
			MultiStatus _status = (MultiStatus) status;
			StringBuilder sb = new StringBuilder();
			sb.append(_status.getMessage());
			IStatus[] children = status.getChildren();
			if (children != null && children.length > 0) {
				sb.append(":");
				Arrays.asList(_status.getChildren()).forEach(entry -> sb.append("\n" + entry.getMessage()));
			}
			return sb.toString();
		}
		return status.getMessage();
	}

	private static Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	public static void show(UISynchronize uiSynchronizer, IStatus status, boolean showIfOk) {
		if (showStatus(status, showIfOk)) {
			uiSynchronizer.syncExec(() -> show(status, showIfOk));
		}
	}

}
