package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dialogs.DateTimeSelectorDialog;
import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.rgw.tools.TimeTool;

public class LabOrderSetObservationDateAction extends Action {

	private List<LaborOrderViewerItem> orders;
	private StructuredViewer viewer;

	public LabOrderSetObservationDateAction(List<LaborOrderViewerItem> list, StructuredViewer viewer) {
		super("Beobachtungszeitpunkt für Auftrag ändern (inkl. Resultate)"); //$NON-NLS-1$
		this.orders = list;
		this.viewer = viewer;
	}

	@Override
	public void run() {
		TimeTool date = null;
		if (orders.size() == 1) {
			date = orders.get(0).getObservationTime();
		} else {
			date = new TimeTool();
		}

		DateTimeSelectorDialog dsd = new DateTimeSelectorDialog(viewer.getControl().getShell(), date);
		if (dsd.open() == Dialog.OK) {
			date = dsd.getSelectedDate();
			for (LaborOrderViewerItem labOrderViewerItem : orders) {
				labOrderViewerItem.setObservationTimeWithResults(date);
			}
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, ILabResult.class);
		}
	}
}
