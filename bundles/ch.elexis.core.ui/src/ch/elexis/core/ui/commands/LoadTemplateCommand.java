package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;
import ch.rgw.tools.ExHandler;

public class LoadTemplateCommand extends AbstractHandler {
	public static String ID = "ch.elexis.core.ui.command.loadTemplate";
	private static Logger logger = LoggerFactory.getLogger(LoadTemplateCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		// get the selection
		Brief template = null;
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				template = textTemplate.getTemplate();
			}
		}
		
		// show template in textview
		try {
			if (template == null) {
				SWTHelper.alert(ch.elexis.core.ui.commands.Messages.LoadTemplateCommand_Error,
					ch.elexis.core.ui.commands.Messages.LoadTemplateCommand_NoTextTemplate);
				return null;
			}
			IWorkbenchPage activePage =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			TextView textView = (TextView) activePage.showView(TextView.ID);
			if (!textView.openDocument(template)) {
				SWTHelper.alert(Messages.BriefAuswahlErrorHeading, //$NON-NLS-1$
					Messages.BriefAuswahlCouldNotLoadText); //$NON-NLS-1$
			}
		} catch (PartInitException e) {
			logger.error("Could not open TextView", e);
			ExHandler.handle(e);
		}
		return null;
	}
	
}
