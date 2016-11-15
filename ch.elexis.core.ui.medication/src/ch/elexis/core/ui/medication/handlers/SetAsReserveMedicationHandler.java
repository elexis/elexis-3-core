package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.data.Prescription;

public class SetAsReserveMedicationHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			
			if (firstElement instanceof MedicationTableViewerItem) {
				MedicationTableViewerItem mtvItem = (MedicationTableViewerItem) firstElement;
				Prescription presc = mtvItem.getPrescription();
				
				if (presc != null && !(presc.getEntryType() == EntryType.RESERVE_MEDICATION)) {
					AcquireLockUi.aquireAndRun(presc, new ILockHandler() {
						
						@Override
						public void lockFailed(){
							// do nothing
						}
						
						@Override
						public void lockAcquired(){
							Prescription reserveMedi = new Prescription(presc);
							AcquireLockUi.aquireAndRun(reserveMedi, new ILockHandler() {
								@Override
								public void lockFailed(){
									reserveMedi.remove();
								}
								
								@Override
								public void lockAcquired(){
									reserveMedi.setEntryType(EntryType.RESERVE_MEDICATION);
								}
							});
							presc.stop(null);
							presc.setStopReason("Umgestellt auf Reserve Medikation");
						}
					});
					ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(presc, Prescription.class, ElexisEvent.EVENT_UPDATE));
				}
			}
		}
		return null;
	}
	
}
