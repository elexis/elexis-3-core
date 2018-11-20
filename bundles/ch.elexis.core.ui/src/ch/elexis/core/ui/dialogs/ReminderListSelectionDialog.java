package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
	
	private Label detailLabel;
	private Composite detail;
	
	public ReminderListSelectionDialog(List<Reminder> input, String message){
		super(UiDesk.getTopShell(), input, ArrayContentProvider.getInstance(),
			PersistentObjectLabelProvider.getInstance(),
			Messages.ReminderListSelectionDialog_SelectToClose);
		setTitle(message);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Control control = super.createDialogArea(parent);
		detail = new Composite(parent, SWT.None);
		detail.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		detailLabel = new Label(detail, SWT.None);
		
		getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0){
				final StringBuilder sb = new StringBuilder();
				
				if (!arg0.getSelection().isEmpty()) {
					Reminder reminder =
						(Reminder) ((IStructuredSelection) arg0.getSelection()).getFirstElement();
					sb.append("    "+reminder.getSubject() + "\n");
					sb.append("    "+reminder.getMessage());
				}
				detailLabel.setText(sb.toString());
				detail.getParent().layout();
			}
		});
		
		@SuppressWarnings("unchecked")
		List<Reminder> reminders = (List<Reminder>) getViewer().getInput();
		if (reminders.size() > 0) {
			getViewer().setSelection(new StructuredSelection(reminders.get(0)));
		}
		
		return control;
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
		return new Point(600, 300);
	}
}
