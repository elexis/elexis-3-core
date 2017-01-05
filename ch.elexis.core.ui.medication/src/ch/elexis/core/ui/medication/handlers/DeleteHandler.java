package ch.elexis.core.ui.medication.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.medication.views.MedicationView;
import ch.elexis.core.ui.medication.views.Messages;

public class DeleteHandler extends AbstractHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ISelection selection = activePage.getSelection();
		
		// remove selection first - otherwise selection will try to select removed item...
		IWorkbenchPart activePart = activePage.getActivePart();
		if (activePart != null && activePart instanceof MedicationView) {
			MedicationView mediView = (MedicationView) activePart;
			mediView.resetSelection();
		}
		
		if (selection != null && !selection.isEmpty()) {
			if (MessageDialog.openQuestion(HandlerUtil.getActiveShell(event),
				Messages.FixMediDisplay_DeleteUnrecoverable,
				Messages.FixMediDisplay_DeleteUnrecoverable)) {
				IStructuredSelection strucSelection = (IStructuredSelection) selection;
				Iterator<MedicationTableViewerItem> selectionList = strucSelection.iterator();
				while (selectionList.hasNext()) {
					MedicationTableViewerItem item = selectionList.next();
					item.getPrescription().remove();
				}
			}
		}
		return null;
	}
}
