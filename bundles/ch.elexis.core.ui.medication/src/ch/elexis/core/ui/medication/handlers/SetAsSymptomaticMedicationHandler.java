package ch.elexis.core.ui.medication.handlers;

import java.time.LocalDateTime;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;

public class SetAsSymptomaticMedicationHandler extends AbstractHandler {
	
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
				
				if (presc != null && !(presc.getEntryType() == EntryType.SYMPTOMATIC_MEDICATION)) {
					AcquireLockUi.aquireAndRun(presc, new ILockHandler() {
						
						@Override
						public void lockFailed(){
							// do nothing
						}
						
						@Override
						public void lockAcquired(){
							IPrescription symptomaticMedi =
								MedicationServiceHolder.get().createPrescriptionCopy(presc);
							symptomaticMedi.setEntryType(EntryType.SYMPTOMATIC_MEDICATION);
							CoreModelServiceHolder.get().save(symptomaticMedi);
							
							MedicationServiceHolder.get().stopPrescription(presc,
								LocalDateTime.now(), "Umgestellt auf Symtomatische Medikation");
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
