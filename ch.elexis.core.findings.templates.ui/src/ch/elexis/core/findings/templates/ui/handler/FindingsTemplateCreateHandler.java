package ch.elexis.core.findings.templates.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.ui.dlg.FindingsDialog;

public class FindingsTemplateCreateHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			Object item = ((StructuredSelection) selection).getFirstElement();
			if (item instanceof FindingsTemplates)
			{
				FindingsDialog findingsDialog = new FindingsDialog(
					Display.getDefault().getActiveShell(), (FindingsTemplates) item);
				findingsDialog.open();
			}
			
		}
		return null;
	}
	
}
