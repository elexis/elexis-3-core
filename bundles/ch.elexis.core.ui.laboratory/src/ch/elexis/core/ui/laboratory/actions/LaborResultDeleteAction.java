package ch.elexis.core.ui.laboratory.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;

public class LaborResultDeleteAction extends Action implements IAction {
	
	private StructuredViewer viewer;
	private List<LabResult> results;
	
	public LaborResultDeleteAction(List<LabResult> currentSelection, StructuredViewer viewer){
		super(Messages.LabResultDeleteAction_title);
		if (!currentSelection.isEmpty()) {
			Object firstObject = currentSelection.get(0);
			if (firstObject instanceof LabResult) {
				this.results = (List<LabResult>) currentSelection;
			} else {
				throw new IllegalArgumentException("Unknown list type of class " //$NON-NLS-1$
					+ firstObject.getClass());
			}
		}
		this.viewer = viewer;
	}
	
	@Override
	public void run(){
		if (results != null) {
			for (LabResult result : results) {
				boolean delete =
					MessageDialog.openConfirm(viewer.getControl().getShell(), "Resultat entfernen",
						"Soll das Resultat " + result + " entfernt werden?");
				if (delete) {
					List<LabOrder> orders =
						LabOrder.getLabOrders(result.getPatient(), null, result.getItem(), result,
							null, null, null);
					
					if (orders != null) {
						for (LabOrder labOrder : orders) {
							labOrder.setState(State.ORDERED);
						}
					}
					final LabResult lockResult = result;
					AcquireLockBlockingUi.aquireAndRun(result, new ILockHandler() {
						
						@Override
						public void lockFailed(){
							// do nothing
							
						}
						
						@Override
						public void lockAcquired(){
							lockResult.delete();
						}
					});
					ElexisEventDispatcher.reload(LabResult.class);
				}
			}
			viewer.refresh();
		}
	}
	
	@Override
	public boolean isEnabled(){
		return results != null && !results.isEmpty();
	}
	
}
