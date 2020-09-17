package ch.elexis.core.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.l10n.Messages;

public class StatusDialog {
	
	public static void show(IStatus status){
		if (!status.getMessage().isEmpty()) {
			if (status.isOK()) {
				showInfo(status);
			} else {
				if (isWarnining(status)) {
					showWarninig(status);
				} else {
					showError(status);
				}
			}
		}
	}
	
	private static boolean isWarnining(IStatus status){
		return Status.WARNING == status.getSeverity();
	}
	
	private static void showError(IStatus status){
		MessageDialog.openError(getShell(), Messages.ResultDialog_Error, getStatusMessage(status));
	}
	
	private static void showWarninig(IStatus status){
		MessageDialog.openWarning(getShell(), Messages.ResultDialog_Warning,
			getStatusMessage(status));
	}
	
	private static void showInfo(IStatus status){
		MessageDialog.openInformation(getShell(), Messages.ResultDialog_Info,
			getStatusMessage(status));
	}
	
	private static String getStatusMessage(IStatus status){
		return status.getMessage();
	}
	
	private static Shell getShell(){
		return Display.getDefault().getActiveShell();
	}
	
}
