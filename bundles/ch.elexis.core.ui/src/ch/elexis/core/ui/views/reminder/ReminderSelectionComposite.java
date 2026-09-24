package ch.elexis.core.ui.views.reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;

public class ReminderSelectionComposite extends Composite implements ISelectionProvider {

	public static final String SELECTIONCOMP_CURRENTPATIENT_ID = "reminderlistsview.selection.currentpatient"; //$NON-NLS-1$
	public static final String SELECTIONCOMP_GENERALPATIENT_ID = "reminderlistsview.selection.generalpatient"; //$NON-NLS-1$
	public static final String SELECTIONCOMP_GENERALREMINDERS_ID = "reminderlistsview.selection.generalreminders"; //$NON-NLS-1$
	public static final String SELECTIONCOMP_MYREMINDERS_ID = "reminderlistsview.selection.myreminders"; //$NON-NLS-1$
	public static final String SELECTIONCOMP_GROUPREMINDERS_PREFIX = "reminderlistsview.selection.groupreminders."; //$NON-NLS-1$

	private List<Action> currentSelection;
	private ListenerList<ISelectionChangedListener> selectionChangedListeners;
	private ToolBarManager manager;
	private List<IUserGroup> userGroups;

	public ReminderSelectionComposite(Composite parent, int style, List<IUserGroup> userGroups) {
		super(parent, style);
		this.userGroups = userGroups;
		this.currentSelection = new ArrayList<>();
		this.selectionChangedListeners = new ListenerList<>();

		createContent();
	}

	public void setCount(String id, int itemCount) {
		for (IContributionItem item : manager.getItems()) {
			if (item.getId().equals(id)) {
				IAction action = ((ActionContributionItem) item).getAction();
				String text = action.getText();
				if (text.indexOf(" (") != -1) { //$NON-NLS-1$
					text = text.substring(0, text.indexOf(" (")); //$NON-NLS-1$
				}
				action.setText(text + " (" + itemCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				item.update();
			}
		}
		manager.update(true);
		layout();
	}

	private void createContent() {
		setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		setLayout(new FillLayout());

		manager = new ToolBarManager(SWT.WRAP);
		manager.add(new Action("aktueller Patient", Action.AS_CHECK_BOX) {
			@Override
			public String getId() {
				return SELECTIONCOMP_CURRENTPATIENT_ID;
			}

			@Override
			public void run() {
				toggleSelection(this);
			}
		});
		manager.add(new Action("alle Patienten", Action.AS_CHECK_BOX) {
			@Override
			public String getId() {
				return SELECTIONCOMP_GENERALPATIENT_ID;
			}

			@Override
			public void run() {
				toggleSelection(this);
			}
		});
		manager.add(new Action("Pendenzen ohne Patientenbezug", Action.AS_CHECK_BOX) {
			@Override
			public String getId() {
				return SELECTIONCOMP_GENERALREMINDERS_ID;
			}

			@Override
			public void run() {
				toggleSelection(this);
			}
		});
		manager.add(new Action("meine Pendenzen", Action.AS_CHECK_BOX) {
			@Override
			public String getId() {
				return SELECTIONCOMP_MYREMINDERS_ID;
			}

			@Override
			public void run() {
				toggleSelection(this);
			}
		});
		for (IUserGroup group : userGroups) {
			manager.add(new Action(group.getId(), Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return SELECTIONCOMP_GROUPREMINDERS_PREFIX + group.getId();
				}

				@Override
				public void run() {
					toggleSelection(this);
				}
			});
		}
		manager.createControl(this);
	}

	private void toggleSelection(Action action) {
		if (currentSelection.contains(action)) {
			currentSelection.remove(action);
		} else {
			currentSelection.add(action);
		}
		fireSelectionChanged();
		manager.update(true);
		saveSelection();
	}

	private void saveSelection() {
		List<String> selectedIds = currentSelection.stream().map(action -> action.getId()).collect(Collectors.toList());
		StringJoiner sj = new StringJoiner(","); //$NON-NLS-1$
		selectedIds.forEach(id -> sj.add(id));
		ConfigServiceHolder.setUser(Preferences.USR_REMINDER_VIEWER_SELECTION, sj.toString());
	}

	public void loadSelection() {
		currentSelection.clear();
		String[] loadedIds = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_VIEWER_SELECTION, StringUtils.EMPTY)
				.split(","); //$NON-NLS-1$
		for (String id : loadedIds) {
			for (IContributionItem item : manager.getItems()) {
				if (item.getId().equals(id)) {
					IAction action = ((ActionContributionItem) item).getAction();
					action.setChecked(true);
					currentSelection.add((Action) action);
				}
			}
			fireSelectionChanged();
			manager.update(true);
			layout();
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(
				currentSelection.stream().map(action -> action.getId()).collect(Collectors.toList()));
	}

	@Override
	public void setSelection(ISelection selection) {
		// ignore until needed
	}

	private void fireSelectionChanged() {
		ISelection selection = getSelection();
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			listener.selectionChanged(event);
		}
	}
}