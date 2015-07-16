package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;

public class DeleteTemplateCommand extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				Brief template = textTemplate.getTemplate();
				textTemplate.removeTemplateReference();
				
				if (template != null) {
					template.delete();
					
					IWorkbenchPage activePage =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					TextTemplateView textTemplateView =
						(TextTemplateView) activePage.findView(TextTemplateView.ID);
					textTemplateView.refresh();
				}
			}
		}
		
		return null;
	}
	
}
