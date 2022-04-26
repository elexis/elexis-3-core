package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.medication.PreferenceConstants;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.ViewerSortOrder;

public class ApplyAtcSortingHandler extends AbstractHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.medication.ApplyAtcSorting";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean useAtc = !HandlerUtil.toggleCommandState(event.getCommand());

		MedicationView medicationView = (MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(MedicationView.PART_ID);
		ViewerSortOrder curSortOrder = medicationView.getMedicationTableViewerComparator();
		if (curSortOrder == ViewerSortOrder.DEFAULT) {
			curSortOrder.setAtcSort(useAtc);
			ConfigServiceHolder.setUser(PreferenceConstants.PREF_MEDICATIONLIST_SORT_ATC, useAtc);
			medicationView.getMedicationComposite().getActiveTableViewer().refresh();
		}
		return null;
	}

}
