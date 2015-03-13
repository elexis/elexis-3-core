package ch.elexis.core.ui.medication.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.data.Prescription;

public class DeleteHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		if (selection != null && !selection.isEmpty()) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Prescription prescription = (Prescription) strucSelection.getFirstElement();
			prescription.remove();
		}
		return null;
	}
}
