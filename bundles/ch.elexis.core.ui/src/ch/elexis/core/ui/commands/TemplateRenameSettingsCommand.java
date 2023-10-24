package ch.elexis.core.ui.commands;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IDocumentLetter;
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

	            
	            if (textTemplate.isSystemTemplate()) {
	                MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.Template_not_editable,
	                        Messages.Template_not_editable_text);
	                return null;
	            }

	            boolean shouldShowInputDialog = true;

	            while (shouldShowInputDialog) {
	                InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(), Messages.Edit_Template,
	                        Messages.Edit_Template_Description, textTemplate.getName(), null);

					int inputDialogResult = inputDialog.open();

					if (inputDialogResult == InputDialog.OK) {
	                    String newName = inputDialog.getValue();
	                    if (StringUtils.isNotBlank(newName)) {
	                        Brief template = textTemplate.getTemplate();
	                        if (template != null) {
	                            template.set(Brief.FLD_SUBJECT, newName);
	                            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
	                            ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
								shouldShowInputDialog = false;
	                        }
	                    } else {
							MessageDialog messageDialog = new MessageDialog(Display.getCurrent().getActiveShell(),
									Messages.Template_not_editable, null,
									Messages.Template_not_editable_empty, MessageDialog.WARNING,
									new String[] { IDialogConstants.OK_LABEL }, 0);
							int messageDialogResult = messageDialog.open();
							if (messageDialogResult != IDialogConstants.OK_ID) {
								shouldShowInputDialog = false; 
							}
	                    }
	                } else {
						shouldShowInputDialog = false; 
	                }
	            }
	        }
	    }

	    return null;
	}
}