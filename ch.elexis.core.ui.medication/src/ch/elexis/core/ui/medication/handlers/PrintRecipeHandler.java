package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.ElexisConfigurationConstants;
import ch.elexis.core.ui.medication.handlers.PrintTakingsListHandler.SorterAdapter;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.views.RezeptBlatt;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.elexis.data.Rezept;

public class PrintRecipeHandler extends AbstractHandler {
	
	public static final String COMMAND_ID = "ch.elexis.core.ui.medication.PrintRecipe";
	
	private static Logger log = LoggerFactory.getLogger(PrintRecipeHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null)
			return null;
		
		String medicationType =
			event.getParameter("ch.elexis.core.ui.medication.commandParameter.medication"); //$NON-NLS-1$
		// if not set use selection
		if (medicationType == null || medicationType.isEmpty()) {
			medicationType = "selection";
		}
		
		List<Prescription> prescRecipes = getPrescriptions(patient, medicationType, event);
		if (!prescRecipes.isEmpty()) {
			prescRecipes = sortPrescriptions(prescRecipes, event);
			
			Rezept rp = new Rezept(patient);
			for (Prescription p : prescRecipes) {
				Prescription prescription = new Prescription(p);
				prescription.setEndDate(null);
				rp.addPrescription(prescription);
			}
			
			// PMDI - Dependency Injection through ElexisConfigurationConstants
			RezeptBlatt rpb;
			try {
				rpb = (RezeptBlatt) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(ElexisConfigurationConstants.rezeptausgabe);
				rpb.createRezept(rp);
			} catch (PartInitException e) {
				log.error("Error outputting recipe", e);
			}
			// PMDI - Dependency Injection through ElexisConfigurationConstants
		}
		return null;
	}
	
	private List<Prescription> sortPrescriptions(List<Prescription> prescRecipes,
		ExecutionEvent event){
		SorterAdapter sorter = new SorterAdapter(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof MedicationView) {
			return sorter.getSorted(prescRecipes);
		}
		return prescRecipes;
	}
	
	@SuppressWarnings("unchecked")
	private List<Prescription> getPrescriptions(Patient patient, String medicationType,
		ExecutionEvent event){
		if ("selection".equals(medicationType)) {
			ISelection selection =
				HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
			if (selection != null && !selection.isEmpty()) {
				List<Prescription> ret = new ArrayList<Prescription>();
				IStructuredSelection strucSelection = (IStructuredSelection) selection;
				if (strucSelection.getFirstElement() instanceof MedicationTableViewerItem) {
					List<MedicationTableViewerItem> mtvItems =
						(List<MedicationTableViewerItem>) strucSelection.toList();
					for (MedicationTableViewerItem mtvItem : mtvItems) {
						Prescription p = mtvItem.getPrescription();
						if (p != null) {
							ret.add(p);
						}
					}
				} else if (strucSelection.getFirstElement() instanceof Prescription) {
					ret.addAll(strucSelection.toList());
				}
				return ret;
			}
		} else if ("all".equals(medicationType)) {
			List<Prescription> ret = new ArrayList<Prescription>();
			ret.addAll(patient.getMedication(EntryType.FIXED_MEDICATION));
			ret.addAll(patient.getMedication(EntryType.RESERVE_MEDICATION));
			return ret;
		} else if ("fix".equals(medicationType)) {
			return patient.getMedication(EntryType.FIXED_MEDICATION);
		} else if ("reserve".equals(medicationType)) {
			return patient.getMedication(EntryType.RESERVE_MEDICATION);
		}
		return Collections.emptyList();
	}
}
