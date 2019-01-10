package ch.elexis.core.ui.medication.handlers;

import java.time.LocalDateTime;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;

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
				IPrescription presc = mtvItem.getPrescription();
				
				if (presc != null && !(presc.getEntryType() == EntryType.RESERVE_MEDICATION)) {
					AcquireLockUi.aquireAndRun(presc, new ILockHandler() {
						
						@Override
						public void lockFailed(){
							// do nothing
						}
						
						@Override
						public void lockAcquired(){
							IPrescription reserveMedi =
								MedicationServiceHolder.get().createPrescriptionCopy(presc);
							reserveMedi.setEntryType(EntryType.RESERVE_MEDICATION);
							CoreModelServiceHolder.get().save(reserveMedi);
							
							MedicationServiceHolder.get().stopPrescription(presc,
								LocalDateTime.now());
							presc.setStopReason("Umgestellt auf Reserve Medikation");
							CoreModelServiceHolder.get().save(presc);
						}
					});
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, presc);
				}
			}
		}
		return null;
	}
	
}
