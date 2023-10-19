package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;

public class TemplateRenameSettingsCommand extends AbstractHandler {

	private boolean systemTemplate;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;

				// Check if it is a SystemTemplate
				if (textTemplate.isSystemTemplate()) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.Template_not_editable,
							Messages.Template_not_editable_text);
					return null;
				}

				InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(), Messages.Edit_Template,
						Messages.Edit_Template_Description, textTemplate.getName(), null);

				if (inputDialog.open() == InputDialog.OK) {
					String newName = inputDialog.getValue();
					Brief template = textTemplate.getTemplate();
					if (template != null) {
						template.set(Brief.FLD_SUBJECT, newName);
						IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
						TextTemplateView view = (TextTemplateView) page.findView(TextTemplateView.ID);
						if (view != null) {
							view.refresh();
						}
					}
				}
			}
		}

		return null;
	}
}
