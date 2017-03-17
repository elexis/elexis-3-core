package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.ElexisConfigurationConstants;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;
import ch.elexis.core.ui.views.RezeptBlatt;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;

public class PrintTakingsListHandler extends AbstractHandler {
	
	public static final String COMMAND_ID = "ch.elexis.core.ui.medication.PrintTakingsList";
	
	private static Logger log = LoggerFactory.getLogger(PrintTakingsListHandler.class);
	
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
			try {
				RezeptBlatt rpb = (RezeptBlatt) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(RezeptBlatt.ID);
				rpb.createEinnahmeliste(patient,
					prescRecipes.toArray(new Prescription[prescRecipes.size()]));
			} catch (PartInitException e) {
				log.error("Error outputting recipe", e);
			}
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
	
	/**
	 * Adpater class to use {@link ViewerSortOrder} sorter implementations to sort a list of
	 * {@link Prescription}. Sorting is done using the current UI state of the
	 * {@link ViewerSortOrder} implementation.
	 * 
	 * @author thomas
	 *
	 */
	public static class SorterAdapter {
		private ViewerSortOrder.ManualViewerComparator manualComparator =
			new ViewerSortOrder.ManualViewerComparator();
		
		private ViewerSortOrder.DefaultViewerComparator defaultComparator =
			new ViewerSortOrder.DefaultViewerComparator();
		
		private enum CompareMode {
				MANUAL, DEFAULT
		}
		
		private CompareMode mode = CompareMode.DEFAULT;
		
		public SorterAdapter(ExecutionEvent event){
			ICommandService commandService = (ICommandService) HandlerUtil.getActiveSite(event)
				.getService(ICommandService.class);
			if (commandService != null) {
				Command command = commandService.getCommand(ApplyCustomSortingHandler.CMD_ID);
				State state = command.getState(ApplyCustomSortingHandler.STATE_ID);
				if (state.getValue() instanceof Boolean) {
					if ((Boolean) state.getValue()) {
						mode = SorterAdapter.CompareMode.MANUAL;
					}
				}
			}
		}
		
		public List<Prescription> getSorted(List<Prescription> list){
			MedicationTableViewerItem[] toSort =
				MedicationTableViewerItem.createFromPrescriptionList(list, null)
					.toArray(new MedicationTableViewerItem[list.size()]);
			// make sure properties are resolved for sorting
			for (MedicationTableViewerItem medicationTableViewerItem : toSort) {
				medicationTableViewerItem.resolve();
			}
			if (mode == CompareMode.DEFAULT) {
				defaultComparator.sort(null, toSort);
			} else if (mode == CompareMode.MANUAL) {
				manualComparator.sort(null, toSort);
			}
			ArrayList<Prescription> ret = new ArrayList<>();
			for (int i = 0; i < toSort.length; i++) {
				ret.add(toSort[i].getPrescription());
			}
			return ret;
		}
	}
}
