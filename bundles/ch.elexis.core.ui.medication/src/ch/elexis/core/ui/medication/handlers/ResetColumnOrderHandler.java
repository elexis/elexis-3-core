package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ui.medication.views.MedicationComposite;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.MedicationViewerHelper;

public class ResetColumnOrderHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		MedicationView medicationView = (MedicationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(MedicationView.PART_ID);
		if (medicationView != null) {
			MedicationComposite composite = medicationView.getMedicationComposite();
			if (composite != null && !composite.isDisposed()) {
				TableViewer viewer = composite.getActiveTableViewer();
				MedicationViewerHelper.resetColumnOrder(viewer);
			}
		}
		return null;
	}
}
