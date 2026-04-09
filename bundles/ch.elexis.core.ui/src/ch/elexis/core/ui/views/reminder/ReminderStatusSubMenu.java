package ch.elexis.core.ui.views.reminder;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.util.viewers.CommonViewer;

public class ReminderStatusSubMenu extends MenuManager {

	private CommonViewer legacyViewer;
	private Viewer e4Viewer;

	public ReminderStatusSubMenu(CommonViewer cv) {
		super("Status...");
		this.legacyViewer = cv;
		init();
	}

	public ReminderStatusSubMenu(Viewer viewer) {
		super("Status...");
		this.e4Viewer = viewer;
		init();
	}

	private void init() {
		setRemoveAllWhenShown(true);
		addMenuListener(new ReminderStatusSubMenuListener());
	}

	private class ReminderStatusSubMenuListener implements IMenuListener {

		@Override
		public void menuAboutToShow(IMenuManager manager) {
			Object selectedElement = null;

			if (legacyViewer != null) {
				Object[] selection = legacyViewer.getSelection();
				if (selection != null && selection.length == 1) {
					selectedElement = selection[0];
				}
			} else if (e4Viewer != null) {
				ISelection sel = e4Viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection ssel = (IStructuredSelection) sel;
					if (ssel.size() == 1) {
						selectedElement = ssel.getFirstElement();
					}
				}
			}

			String globalCustoms = ConfigServiceHolder.getGlobal(Preferences.USR_REMINDER_CUSTOM_STATUSES_GLOBAL,
					StringUtils.EMPTY);
			String[] customArray = globalCustoms.isEmpty() ? new String[0] : globalCustoms.split(",");

			if (selectedElement instanceof IReminder) {
				IReminder reminder = (IReminder) selectedElement;
				manager.add(new StatusAction(ProcessStatus.OPEN, reminder));
				manager.add(new StatusAction(ProcessStatus.IN_PROGRESS, reminder));
				manager.add(new StatusAction(ProcessStatus.CLOSED, reminder));
				manager.add(new StatusAction(ProcessStatus.ON_HOLD, reminder));

				if (customArray.length > 0) {
					manager.add(new Separator());
					for (String cs : customArray) {
						manager.add(new CustomStatusAction(cs, reminder));
					}
				}
			} else {
				manager.add(new Action("Multiple selection / No selection") {
					@Override
					public boolean isEnabled() {
						return false;
					}
				});
			}
		}

		private class StatusAction extends LockRequestingAction<IReminder> {
			private final ProcessStatus representedStatus;
			private final IReminder reminder;

			public StatusAction(ProcessStatus representedStatus, IReminder reminder) {
				super(representedStatus.getLocaleText(), SWT.RADIO);
				this.representedStatus = representedStatus;
				this.reminder = reminder;

				Object customStatus = reminder.getExtInfo(IReminder.EXTINFO_CUSTOM_STATUS);
				boolean hasCustom = customStatus != null && !customStatus.toString().isEmpty();
				setChecked(representedStatus == reminder.getStatus() && !hasCustom);
			}

			@Override
			public IReminder getTargetedObject() {
				return reminder;
			}

			@Override
			public void doRun(IReminder element) {
				element.setStatus(representedStatus);
				element.setExtInfo(IReminder.EXTINFO_CUSTOM_STATUS, null);
				CoreModelServiceHolder.get().save(element);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, element);
			}
		}

		private class CustomStatusAction extends LockRequestingAction<IReminder> {
			private final String customStatus;
			private final IReminder reminder;

			public CustomStatusAction(String customStatus, IReminder reminder) {
				super(customStatus, SWT.RADIO);
				this.customStatus = customStatus;
				this.reminder = reminder;

				Object currentCustom = reminder.getExtInfo(IReminder.EXTINFO_CUSTOM_STATUS);
				setChecked(customStatus.equals(currentCustom));
			}

			@Override
			public IReminder getTargetedObject() {
				return reminder;
			}

			@Override
			public void doRun(IReminder element) {
				element.setStatus(ProcessStatus.IN_PROGRESS);
				element.setExtInfo(IReminder.EXTINFO_CUSTOM_STATUS, customStatus);
				CoreModelServiceHolder.get().save(element);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, element);
			}
		}
	}
}