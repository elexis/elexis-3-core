package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;

import ch.elexis.core.ui.dialogs.DateTimeSelectorDialog;
import ch.elexis.data.LabOrder;
import ch.rgw.tools.TimeTool;

public class LabOrderSetObservationDateAction extends Action {
	
	private List<LabOrder> orders;
	private TreeViewer viewer;
	
	public LabOrderSetObservationDateAction(List<LabOrder> orders, TreeViewer viewer){
		super("Beobachtungszeitpunkt ändern (inkl. Resultate)"); //$NON-NLS-1$
		this.orders = orders;
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		TimeTool date = null;
		if (orders.size() == 1) {
			date = orders.get(0).getObservationTime();
		} else {
			date = new TimeTool();
		}
		
		DateTimeSelectorDialog dsd = new DateTimeSelectorDialog(viewer.getTree().getShell(), date);
		if (dsd.open() == Dialog.OK) {
			date = dsd.getSelectedDate();
			for (LabOrder labOrder : orders) {
				labOrder.setObservationTimeWithResults(date);
			}
		}
	}
}
