package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;

public class AddMedicationHandler extends AbstractHandler {
	private static MedicationView medicationView;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		medicationView = (MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().findView(MedicationView.PART_ID);
		
		// open the LeistungenView
		try {
			medicationView.getViewSite().getPage().showView(LeistungenView.ID);
			LeistungenView leistungenView = (LeistungenView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().showView(LeistungenView.ID);
			CodeSelectorHandler csHandler = CodeSelectorHandler.getInstance();
			csHandler
				.setCodeSelectorTarget(medicationView.getMedicationComposite().getDropTarget());
			csHandler.getCodeSelectorTarget().registered(false);
			medicationView.getMedicationComposite().setDropChangePrescription(null);
			
			for (CTabItem cti : leistungenView.ctab.getItems()) {
				if (cti.getText().equalsIgnoreCase("Artikelstamm")) {
					leistungenView.setSelected(cti);
					leistungenView.setFocus();
					leistungenView.ctab.setSelection(cti);
				}
			}
			
		} catch (Exception e) {
			getLogger().error("Error showing LeistungenView", e);
		}
		return null;
	}
	
	private Logger getLogger(){
		return LoggerFactory.getLogger(AddMedicationHandler.class);
	}
}
