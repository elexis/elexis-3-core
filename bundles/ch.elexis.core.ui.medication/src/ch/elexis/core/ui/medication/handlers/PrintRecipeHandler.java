package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.Arrays;
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

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.ElexisConfigurationConstants;
import ch.elexis.core.ui.medication.handlers.PrintTakingsListHandler.SorterAdapter;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.views.RezeptBlatt;
import ch.elexis.data.Rezept;

public class PrintRecipeHandler extends AbstractHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.medication.PrintRecipe"; //$NON-NLS-1$

	private static Logger log = LoggerFactory.getLogger(PrintRecipeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);
		if (patient == null)
			return null;

		String medicationType = event.getParameter("ch.elexis.core.ui.medication.commandParameter.medication"); //$NON-NLS-1$
		// if not set use selection
		if (medicationType == null || medicationType.isEmpty()) {
			medicationType = "selection"; //$NON-NLS-1$
		}

		String address = event.getParameter("ch.elexis.core.ui.medication.commandParameter.address"); //$NON-NLS-1$

		List<IPrescription> prescRecipes = getPrescriptions(patient, medicationType, event);
		if (!prescRecipes.isEmpty()) {
			prescRecipes = sortPrescriptions(prescRecipes, event);

			IRecipe recipe = MedicationServiceHolder.get().createRecipe(patient, prescRecipes);

			RezeptBlatt rpb;
			try {
				rpb = (RezeptBlatt) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(ElexisConfigurationConstants.rezeptausgabe);
				boolean previousAddressSelection = rpb.isAddressSelection();
				rpb.setAddressSelection("select".equals(address)); //$NON-NLS-1$
				rpb.createRezept(Rezept.load(recipe.getId()));
				rpb.setAddressSelection(previousAddressSelection);

				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, recipe);
			} catch (PartInitException e) {
				log.error("Error outputting recipe", e); //$NON-NLS-1$
			}
		}
		return null;
	}

	private List<IPrescription> sortPrescriptions(List<IPrescription> prescRecipes, ExecutionEvent event) {
		SorterAdapter sorter = new SorterAdapter(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof MedicationView) {
			return sorter.getSorted(prescRecipes);
		}
		return prescRecipes;
	}

	@SuppressWarnings("unchecked")
	private List<IPrescription> getPrescriptions(IPatient patient, String medicationType, ExecutionEvent event) {
		if ("selection".equals(medicationType)) { //$NON-NLS-1$
			ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
			if (selection != null && !selection.isEmpty()) {
				List<IPrescription> ret = new ArrayList<>();
				IStructuredSelection strucSelection = (IStructuredSelection) selection;
				if (strucSelection.getFirstElement() instanceof MedicationTableViewerItem) {
					List<MedicationTableViewerItem> mtvItems = (List<MedicationTableViewerItem>) strucSelection
							.toList();
					for (MedicationTableViewerItem mtvItem : mtvItems) {
						IPrescription p = mtvItem.getPrescription();
						if (p != null) {
							ret.add(p);
						}
					}
				} else if (strucSelection.getFirstElement() instanceof IPrescription) {
					ret.addAll(strucSelection.toList());
				}
				return ret;
			}
		} else if ("all".equals(medicationType)) { //$NON-NLS-1$
			return patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION, EntryType.RESERVE_MEDICATION,
					EntryType.SYMPTOMATIC_MEDICATION));
		} else if ("fix".equals(medicationType)) { //$NON-NLS-1$
			return patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
		} else if ("reserve".equals(medicationType)) { //$NON-NLS-1$
			return patient.getMedication(Arrays.asList(EntryType.RESERVE_MEDICATION));
		} else if ("symptomatic".equals(medicationType)) { //$NON-NLS-1$
			return patient.getMedication(Arrays.asList(EntryType.SYMPTOMATIC_MEDICATION));
		}
		return Collections.emptyList();
	}
}
