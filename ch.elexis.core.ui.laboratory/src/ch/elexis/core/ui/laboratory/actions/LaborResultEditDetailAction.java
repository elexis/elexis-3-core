package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.laboratory.dialogs.EditLabResultDialog;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class LaborResultEditDetailAction extends Action {
	private List<LabResult> results;
	private List<LabOrder> orders;
	private StructuredViewer viewer;
	
	@SuppressWarnings("unchecked")
	public LaborResultEditDetailAction(List<?> list, StructuredViewer viewer){
		super(Messages.LaborResultEditDetailAction_title); //$NON-NLS-1$
		Object firstObject = list.get(0);
		if (firstObject instanceof LabResult) {
			this.results = (List<LabResult>) list;
		} else if (firstObject instanceof LabOrder) {
			this.orders = (List<LabOrder>) list;
		} else {
			throw new IllegalArgumentException("Unknown list type of class " //$NON-NLS-1$
				+ firstObject.getClass());
		}
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		if (results != null) {
			for (LabResult result : results) {
				EditLabResultDialog dialog =
					new EditLabResultDialog(viewer.getControl().getShell(), result);
				if (dialog.open() == Window.OK) {
					ElexisEventDispatcher.reload(LabResult.class);
				}
			}
		} else if (orders != null) {
			for (LabOrder order : orders) {
				LabResult result = order.getLabResult();
				if (result == null) {
					result = order.createResult();
					result.setTransmissionTime(new TimeTool());
				}
				EditLabResultDialog dialog =
					new EditLabResultDialog(viewer.getControl().getShell(), result);
				if (dialog.open() == Window.OK) {
					order.setState(State.DONE);
					ElexisEventDispatcher.reload(LabResult.class);
				}
			}
		}
	}
	
	@Override
	public boolean isEnabled(){
		if (results != null) {
			for (LabResult result : results) {
				if (result.getItem() != null) {
					if (result.getItem().getTyp() == typ.DOCUMENT
						|| result.getItem().getTyp() == typ.FORMULA) {
						return false;
					}
				}
			}
		} else if (orders != null) {
			for (LabOrder order : orders) {
				if (order.getLabItem() != null) {
					if (order.getLabItem().getTyp() == typ.DOCUMENT
						|| order.getLabItem().getTyp() == typ.FORMULA) {
						return false;
					}
				}
			}
		}
		return super.isEnabled();
	}
}