package ch.elexis.core.ui.laboratory.actions;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;

public class LaborResultOrderDeleteAction extends Action implements IAction {

	private List<?> selectedOrdersOrResults;
	private final Shell shell;
	private final StructuredViewer viewer;

	public LaborResultOrderDeleteAction(List<?> list, StructuredViewer viewer) {
		super(ch.elexis.core.l10n.Messages.LabResultOrOrderDeleteAction_title);
		this.selectedOrdersOrResults = list;
		this.viewer = viewer;
		this.shell = viewer.getControl().getShell();
	}

	public LaborResultOrderDeleteAction(List<?> list, Shell shell) {
		super(ch.elexis.core.l10n.Messages.LabResultOrOrderDeleteAction_title);
		this.selectedOrdersOrResults = list;
		this.viewer = null;
		this.shell = shell;
	}

	@Override
	public void run() {

		String[] dialogButtonLabels;
		if (selectedOrdersOrResults.size() > 1) {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL,
					IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL };
		} else {
			dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
		}

		Boolean persistedDelete = null;

		for (Object object : selectedOrdersOrResults) {

			LabResult result;
			LabOrder order;
			Boolean delete = null;

			if (object instanceof LabOrder) {
				order = (LabOrder) object;
				result = (LabResult) order.getLabResult();
			} else if (object instanceof LabResult) {
				result = (LabResult) object;
				order = result.getLabOrder();
			} else if (object instanceof LaborOrderViewerItem) {
				// drop-in-replacement support for LaborOrdersComposite
				result = ((LaborOrderViewerItem) object).getLabResult();
				order = ((LaborOrderViewerItem) object).getLabOrder();
			} else {
				throw new IllegalArgumentException("Unknown list entry type of class " //$NON-NLS-1$
						+ object.getClass());
			}

			if (persistedDelete == null) {
				final MessageDialog dialog = new MessageDialog(shell, "Resultat/Verordnung entfernen", null,
						"Soll das Resultat [" + result + "] sowie die zugeh. Verordnung wirklich entfernt werden?",
						MessageDialog.CONFIRM, dialogButtonLabels, dialogButtonLabels.length);
				int open = dialog.open();
				if (dialogButtonLabels.length == 2) {
					delete = (open == 0);
				} else {
					if (open == 0) {
						delete = true;
					} else if (open == 1) {
						persistedDelete = true;
					} else if (open == 2) {
						delete = false;
					} else if (open == 3) {
						persistedDelete = false;
					}
				}
			}

			if (Objects.equals(Boolean.TRUE, delete) || Objects.equals(Boolean.TRUE, persistedDelete)) {
				AcquireLockBlockingUi.aquireAndRun(result != null ? result : order, new ILockHandler() {
					@Override
					public void lockFailed() {
						// do nothing
					}

					@Override
					public void lockAcquired() {
						if (result != null) {
							result.delete();
						}
						if (order != null) {
							order.delete();
						}
						if (viewer != null) {
							viewer.refresh();
						}

					}
				});
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, ILabResult.class);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return selectedOrdersOrResults != null && !selectedOrdersOrResults.isEmpty();
	}

}
