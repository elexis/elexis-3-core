package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.rgw.tools.TimeTool;

public class DispenseHandler extends AbstractHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null)
			return null;
		
		List<Prescription> prescRecipes = new ArrayList<Prescription>();
		
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection.isEmpty()) {
			prescRecipes = Arrays.asList(patient.getFixmedikation());
		} else {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			List<MedicationTableViewerItem> mtvItems = strucSelection.toList();
			for (MedicationTableViewerItem mtvItem : mtvItems) {
				Prescription p = mtvItem.getPrescription();
				if (p != null) {
					prescRecipes.add(p);
				}
			}
		}
		
		Konsultation kons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		
		if (kons != null) {
			boolean isToday = new TimeTool(kons.getDatum()).isSameDay(new TimeTool());
			if (isToday) {
				// create verrechenbar
				for (Prescription prescription : prescRecipes) {
					// update letzte abgabe
					kons.addLeistung(prescription.getArtikel());
				}
				return null;
			}
		}
		
		MessageDialog.openInformation(UiDesk.getTopShell(), "Konsultation ungültig",
			"Die gefundene Konsultation ist nicht von heute.");
		
		return null;
	}
	
}
