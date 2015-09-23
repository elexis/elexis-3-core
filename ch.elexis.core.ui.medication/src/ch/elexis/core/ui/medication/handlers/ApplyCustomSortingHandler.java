package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;

public class ApplyCustomSortingHandler extends AbstractHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.medication.ApplyCustomSorting";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		boolean useDefault = HandlerUtil.toggleCommandState(event.getCommand());
		
		ViewerSortOrder sortOrder =
			ViewerSortOrder.getSortOrderPerValue(ViewerSortOrder.DEFAULT.ordinal());
		if (!useDefault) {
			sortOrder = ViewerSortOrder.getSortOrderPerValue(ViewerSortOrder.MANUAL.ordinal());
		}
		
		MedicationView medicationView = (MedicationView) PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getActivePage().findView(MedicationView.PART_ID);
		medicationView.setMedicationTableViewerComparator(sortOrder);
		return null;
	}
	
}
