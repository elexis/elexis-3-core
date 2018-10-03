package ch.elexis.core.ui.dialogs;

import java.util.StringJoiner;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class ResultDialog {
	
	public static void show(Result<?> result){
		if (!result.getMessages().isEmpty()) {
			if (result.isOK()) {
				showInfo(result);
			} else {
				if (isWarnining(result)) {
					showWarninig(result);
				} else {
					showError(result);
				}
			}
		}
	}
	
	private static boolean isWarnining(Result<?> result){
		for (Result<?>.msg message : result.getMessages()) {
			if (message.getSeverity() != SEVERITY.WARNING) {
				return false;
			}
		}
		return true;
	}
	
	private static void showError(Result<?> result){
		MessageDialog.openError(getShell(), "", getResultMessage(result));
	}
	
	private static void showWarninig(Result<?> result){
		MessageDialog.openWarning(getShell(), "", getResultMessage(result));
	}
	
	private static void showInfo(Result<?> result){
		MessageDialog.openInformation(getShell(), "", getResultMessage(result));
	}
	
	private static String getResultMessage(Result<?> result){
		StringJoiner sj = new StringJoiner("\n\n");
		for (Result<?>.msg message : result.getMessages()) {
			sj.add(message.getText());
		}
		return sj.toString();
	}
	
	private static Shell getShell(){
		return Display.getDefault().getActiveShell();
	}
	
}
