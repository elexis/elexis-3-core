package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.ui.laboratory.controls.LaborOrderViewerItem;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;

public class LaborResultOrderDeleteAction extends Action implements IAction {
	
	private List<?> selectedOrdersOrResults;
	private final StructuredViewer viewer;
	
	public LaborResultOrderDeleteAction(List<?> list, StructuredViewer viewer){
		super(ch.elexis.core.l10n.Messages.LabResultOrOrderDeleteAction_title);
		this.selectedOrdersOrResults = list;
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		for (Object object : selectedOrdersOrResults) {
			
			LabResult result;
			LabOrder order;
			
			if (object instanceof LabOrder) {
				order = (LabOrder) object;
				result = (LabResult) order.getLabResult();
			} else if (object instanceof LabResult) {
				result = (LabResult) object;
				order = result.getLabOrder();
			} else if (object instanceof LaborOrderViewerItem) {
				// drop-in-replacement support for LaborOrdersComposite
				result = (LabResult) ((LaborOrderViewerItem) object).getLabResult();
				order = result.getLabOrder();
			} else {
				throw new IllegalArgumentException("Unknown list entry type of class " //$NON-NLS-1$
					+ object.getClass());
			}
			
			boolean delete = MessageDialog.openConfirm(viewer.getControl().getShell(),
				"Resultat/Verordnung entfernen", "Sollen Resultat [" + result + "] und Verordnung ["
					+ order.get(LabOrder.FLD_ORDERID) + "] wirklich entfernt werden?");
			
			if (delete) {
				AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
					@Override
					public void lockFailed(){
						// do nothing
					}
					
					@Override
					public void lockAcquired(){
						if (result != null) {
							result.delete();
						}
						order.delete();
						if (viewer != null) {
							viewer.refresh();
						}
					}
				});
			}
		}
	}
	
	@Override
	public boolean isEnabled(){
		return selectedOrdersOrResults != null && !selectedOrdersOrResults.isEmpty();
	}
	
}
