package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.viewers.PersistentObjectLabelProvider;
import ch.elexis.data.Reminder;

/**
 * Show a list of reminders, and set them as closed on selection and ok
 * 
 * @since 3.7
 */
public class ReminderListSelectionDialog extends ListSelectionDialog {
	
	public ReminderListSelectionDialog(List<Reminder> input, String message){
		super(UiDesk.getTopShell(), input, ArrayContentProvider.getInstance(),
			PersistentObjectLabelProvider.getInstance(),
			Messages.ReminderListSelectionDialog_SelectToClose);
		setTitle(message);
	}
	
	@Override
	protected void okPressed(){
		super.okPressed();
		
		Object[] result = getResult();
		for (Object object : result) {
			Reminder reminder = (Reminder) object;
			reminder.setProcessStatus(ProcessStatus.CLOSED);
		}
	}
	
	@Override
	protected Point getInitialSize(){
		return new Point(600, 250);
	}
	
}
