package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.elexis.core.ui.laboratory.dialogs.EditLabResultDialog;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class LaborResultEditDetailAction extends Action {
	private List<LabResult> results;
	private List<LaborOrderViewerItem> orders;
	private StructuredViewer viewer;

	@SuppressWarnings("unchecked")
	public LaborResultEditDetailAction(List<?> list, StructuredViewer viewer) {
		super(Messages.LaborResultEditDetailAction_title); // $NON-NLS-1$
		Object firstObject = list.get(0);
		if (firstObject instanceof LabResult) {
			this.results = (List<LabResult>) list;
		} else if (firstObject instanceof LaborOrderViewerItem) {
			this.orders = (List<LaborOrderViewerItem>) list;
		} else {
			throw new IllegalArgumentException("Unknown list type of class " //$NON-NLS-1$
					+ firstObject.getClass());
		}
		this.viewer = viewer;
	}

	@Override
	public void run() {
		if (results != null) {
			for (LabResult result : results) {
				final LabResult lockingResult = result;
				AcquireLockBlockingUi.aquireAndRun(lockingResult, new ILockHandler() {
					@Override
					public void lockFailed() {
						// do nothing
					}

					@Override
					public void lockAcquired() {
						EditLabResultDialog dialog = new EditLabResultDialog(viewer.getControl().getShell(),
								lockingResult);
						if (dialog.open() == Window.OK) {
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, ILabResult.class);
						}
					}
				});
			}
		} else if (orders != null) {
			for (LaborOrderViewerItem orderViewerItem : orders) {
				LabResult result = orderViewerItem.getLabResult();
				if (result == null) {
					result = orderViewerItem.createResult();
					result.setTransmissionTime(new TimeTool());
				}
				final LabResult lockingResult = result;
				final LaborOrderViewerItem lockingOrder = orderViewerItem;
				AcquireLockBlockingUi.aquireAndRun(lockingResult, new ILockHandler() {
					@Override
					public void lockFailed() {
						// do nothing
					}

					@Override
					public void lockAcquired() {
						EditLabResultDialog dialog = new EditLabResultDialog(viewer.getControl().getShell(),
								lockingResult);
						if (dialog.open() == Window.OK) {
							lockingOrder.setState(State.DONE);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, ILabResult.class);
						}
					}
				});
			}
		}
	}

	@Override
	public boolean isEnabled() {
		if (results != null) {
			for (LabResult result : results) {
				if (result.getItem() != null) {
					if (result.getItem().getTyp() == LabItemTyp.DOCUMENT
							|| result.getItem().getTyp() == LabItemTyp.FORMULA) {
						return false;
					}
				}
			}
		} else if (orders != null) {
			for (LaborOrderViewerItem order : orders) {
				if (order.hasLabItem()) {
					LabItemTyp itemTyp = order.getLabItemTyp();
					if (itemTyp == LabItemTyp.DOCUMENT || itemTyp == LabItemTyp.FORMULA) {
						return false;
					}
				}
			}
		}
		return super.isEnabled();
	}
}