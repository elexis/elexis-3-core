package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;
import ch.elexis.data.Mandant;

public class CreateMandantTemplateCommand extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Mandant mandant = ElexisEventDispatcher.getSelectedMandator();
		if (mandant == null) {
			return null;
		}
		
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			
			// get brief via text template and clone it
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				Brief template = textTemplate.getTemplate();
				
				Brief specTemplate =
					new Brief(template.getBetreff(), null, CoreHub.actUser, mandant, null,
						Brief.TEMPLATE);
				specTemplate.save(template.loadBinary(), template.getMimeType());
				
				TextTemplate specTextTemplate =
					new TextTemplate(specTemplate.getBetreff(), "", specTemplate.getMimeType());
				specTextTemplate.addFormTemplateReference(specTemplate);
				
				refreshTextTemplateView(specTextTemplate);
			}
		}
		return null;
	}
	
	private void refreshTextTemplateView(TextTemplate template){
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TextTemplateView textTemplateView =
			(TextTemplateView) activePage.findView(TextTemplateView.ID);
		textTemplateView.update(template);
	}
}
