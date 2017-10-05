package ch.elexis.core.findings.templates.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.ui.dlg.FindingsEditDialog;
import ch.elexis.core.findings.templates.ui.views.FindingsView;

public class FindingEditHandler extends AbstractHandler implements IHandler {
	public static final String COMMAND_ID = "ch.elexis.core.findings.templates.ui.commandEdit";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			Object item = ((StructuredSelection) selection).getFirstElement();
			if (item instanceof IFinding) {
				IFinding iFinding = (IFinding) item;
				
				FindingsEditDialog findingsEditDialog =
					new FindingsEditDialog(Display.getDefault().getActiveShell(), iFinding);
				if (findingsEditDialog.open() == MessageDialog.OK) {
					IWorkbenchPart part = HandlerUtil.getActivePart(event);
					if (part instanceof FindingsView) {
						FindingsView findingsView = (FindingsView) part;
						findingsView.refresh();
					}
				}
			}
			
		}
		return null;
	}
	
}
