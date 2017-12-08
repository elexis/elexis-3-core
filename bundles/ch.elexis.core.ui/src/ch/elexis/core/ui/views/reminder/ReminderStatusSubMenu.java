package ch.elexis.core.ui.views.reminder;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.data.Reminder;

public class ReminderStatusSubMenu extends MenuManager {
	
	private CommonViewer cv;
	
	public ReminderStatusSubMenu(CommonViewer cv){
		super("Status...");
		this.cv = cv;
		
		setRemoveAllWhenShown(true);
		addMenuListener(new ReminderStatusSubMenuListener());
	}
	
	private class ReminderStatusSubMenuListener implements IMenuListener {
		
		@Override
		public void menuAboutToShow(IMenuManager manager){
			Object[] selection = cv.getSelection();
			if (selection != null && selection.length == 1) {
				if(selection[0] instanceof Reminder) {
					Reminder reminder = (Reminder) selection[0];
					manager.add(new StatusAction(ProcessStatus.OPEN, reminder));
					manager.add(new StatusAction(ProcessStatus.IN_PROGRESS, reminder));
					manager.add(new StatusAction(ProcessStatus.CLOSED, reminder));
					manager.add(new StatusAction(ProcessStatus.ON_HOLD, reminder));
				}
			} else {
				manager.add(new Action("Multiple selection") {
					@Override
					public boolean isEnabled(){
						return false;
					}
				});
			}
		}
		
		private class StatusAction extends LockRequestingAction<Reminder> {
			
			private final ProcessStatus representedStatus;
			private Reminder reminder;
			
			public StatusAction(ProcessStatus representedStatus, Reminder reminder){
				super(representedStatus.getLocaleText(), SWT.RADIO);
				this.representedStatus = representedStatus;
				this.reminder = reminder;
				
				ProcessStatus status = reminder.getProcessStatus();
				if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
					setChecked(representedStatus == ProcessStatus.OPEN);
				} else {
					setChecked(representedStatus == status);
				}
			}
			
			@Override
			public boolean isChecked(){
				ProcessStatus status = reminder.getProcessStatus();
				if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
					return (representedStatus == ProcessStatus.OPEN);
				} else {
					return (representedStatus == reminder.getProcessStatus());
				}
			}
			
			@Override
			public String getText(){
				String text = super.getText();
				ProcessStatus status = reminder.getProcessStatus();
				if ((ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status)
					&& (ProcessStatus.OPEN == representedStatus)) {
					return text + " (" + status.getLocaleText() + ")";
				}
				return text;
			}
			
			@Override
			public Reminder getTargetedObject(){
				return reminder;
			}
			
			@Override
			public void doRun(Reminder element){
				element.setProcessStatus(representedStatus);
				ElexisEventDispatcher.getInstance()
					.fire(new ElexisEvent(element, Reminder.class, ElexisEvent.EVENT_UPDATE));
			}
		}
	}
}
