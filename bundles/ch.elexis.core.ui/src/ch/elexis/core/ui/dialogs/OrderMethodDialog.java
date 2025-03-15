package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class OrderMethodDialog {

	public enum OrderMethod {
		SEND, CANCEL
	}

	public static OrderMethod open() {
		Shell shell = Display.getDefault().getActiveShell();
		String[] options = { Messages.Core_DoSend, Messages.Core_Abort };
		int choice = MessageDialog.open(MessageDialog.QUESTION, shell, Messages.OrderMethodDialog_Title,
				Messages.OrderMethodDialog_Message, SWT.NONE, options);

		switch (choice) {
		case 0:
			return OrderMethod.SEND;
		default:
			return OrderMethod.CANCEL;
		}
	}
}
