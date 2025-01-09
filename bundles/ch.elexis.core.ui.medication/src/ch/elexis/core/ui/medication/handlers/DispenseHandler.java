package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;

public class DispenseHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
		if (!patient.isPresent()) {
			return null;
		}

		List<IPrescription> prescRecipes = new ArrayList<>();

		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			List<MedicationTableViewerItem> mtvItems = strucSelection.toList();
			for (MedicationTableViewerItem mtvItem : mtvItems) {
				IPrescription p = mtvItem.getPrescription();
				if (p != null) {
					prescRecipes.add(p);
				}
			}
		}
		for (IPrescription prescription : prescRecipes) {
			CreatePrescriptionHelper prescriptionHelper = new CreatePrescriptionHelper(null,
					HandlerUtil.getActiveShell(event));
			prescriptionHelper.selfDispense(prescription, true);
		}
		ContextServiceHolder.get().getTyped(IEncounter.class)
				.ifPresent(enc -> ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, enc));
		return null;
	}

}
