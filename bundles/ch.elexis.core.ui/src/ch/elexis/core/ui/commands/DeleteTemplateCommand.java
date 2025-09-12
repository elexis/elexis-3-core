package ch.elexis.core.ui.commands;

import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;

public class DeleteTemplateCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
						Messages.GenericReallyDeleteCaption,
						MessageFormat.format(Messages.GenericReallyDeleteContents, textTemplate.getName()))) {

					Brief template = textTemplate.getTemplate();
					textTemplate.removeTemplateReference();

					if (template != null) {
						template.delete();

						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
					}
				}
			}
		}

		return null;
	}

}
