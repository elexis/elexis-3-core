package ch.elexis.core.ui.util.dnd;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ReminderDndManager {

	public enum TableType {
		CURRENT_PATIENT, GENERAL_PATIENT, GENERALREMINDERS, MYREMINDERS, GROUP
	}

	public static void addDragSupport(TableViewer viewer) {
		DragSource dragSource = new DragSource(viewer.getTable(), DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });

		dragSource.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				Table table = viewer.getTable();
				if (table.isDisposed()) {
					event.doit = false;
					return;
				}

				Point pt = table.toControl(table.getDisplay().getCursorLocation());
				int clientHeight = table.getClientArea().height;

				if (pt.y >= (clientHeight - 40)) {
					event.doit = false;
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(viewer.getStructuredSelection());
			}
		});
	}

	public static void addDropSupport(TableViewer viewer, TableType tableType, Shell shell, IUserGroup targetGroup,
			Runnable onRefresh) {
		DropTarget dropTarget = new DropTarget(viewer.getTable(), DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });

		dropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object element : ((IStructuredSelection) selection).toList()) {
						if (element instanceof IReminder) {
							IReminder reminder = (IReminder) element;
							updateReminderForTarget(reminder, tableType, targetGroup, shell);
							CoreModelServiceHolder.get().save(reminder);
							if (onRefresh != null) {
								onRefresh.run();
							}
						}
					}
				}
			}
		});
	}

	private static void updateReminderForTarget(IReminder reminder, TableType type, IUserGroup targetGroup,
			Shell shell) {
		switch (type) {
		case CURRENT_PATIENT:
			// do nothing, it should not be possible to change the patient of a reminder.
			break;
		case GENERAL_PATIENT:
			if (reminder.isResponsibleAll()) {
				break;
			}
			String responsibleText = reminder.getResponsible().stream()
					.map(r -> r.getDescription1() + StringUtils.SPACE + r.getDescription2())
					.collect(Collectors.joining(", ")); //$NON-NLS-1$

			StringBuilder sb = new StringBuilder("Pendenz "); //$NON-NLS-1$
			if (!responsibleText.isEmpty()) {
				sb.append("von: ").append(responsibleText).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			sb.append("an Alle zuweisen?"); //$NON-NLS-1$

			if (MessageDialog.openConfirm(shell, "Pendenz: " + reminder.getSubject(), sb.toString())) { //$NON-NLS-1$
				if (!reminder.getResponsible().isEmpty()) {
					for (IContact contact : reminder.getResponsible()) {
						reminder.removeResponsible(contact);
					}
				}
				reminder.setResponsibleAll(true);
			}
			break;
		case GENERALREMINDERS:
			// do nothing
			break;
		case GROUP:
			if (targetGroup != null) {
				List<IContact> currentResponsibles = reminder.getResponsible();
				if (reminder.isResponsibleAll()) {
					reminder.setResponsibleAll(false);
				}
				if (!currentResponsibles.isEmpty()) {
					for (IContact contact : currentResponsibles) {
						reminder.removeResponsible(contact);
					}
				}
				for (IUser user : targetGroup.getUsers()) {
					reminder.addResponsible(user.getAssignedContact());
				}
			}
			break;
		case MYREMINDERS:
			if (reminder.isResponsibleAll()) {
				reminder.setResponsibleAll(false);
			}
			if (!reminder.getResponsible().isEmpty()) {
				for (IContact contact : reminder.getResponsible()) {
					reminder.removeResponsible(contact);
				}
			}
			reminder.addResponsible(ContextServiceHolder.getActiveMandatorOrNull());
			break;
		default:
			break;
		}
	}
}