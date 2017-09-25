package ch.elexis.core.findings.templates.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.templates.ui.views.FindingsView;
import ch.elexis.core.findings.util.commands.FindingDeleteCommand;
import ch.elexis.core.findings.util.commands.ObservationDeleteCommand;

public class FindingDeleteHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			Object item = ((StructuredSelection) selection).getFirstElement();
			if (item instanceof IFinding) {
				IFinding iFinding = (IFinding) item;
				
				if (iFinding instanceof IObservation) {
					new ObservationDeleteCommand((IObservation) iFinding).execute();
				} else {
					new FindingDeleteCommand(iFinding).execute();
				}
				
				IWorkbenchPart part = HandlerUtil.getActivePart(event);
				if (part instanceof FindingsView) {
					FindingsView findingsView = (FindingsView) part;
					findingsView.removeFromTable(iFinding);
				}
			}
			
		}
		return null;
	}
	
}
