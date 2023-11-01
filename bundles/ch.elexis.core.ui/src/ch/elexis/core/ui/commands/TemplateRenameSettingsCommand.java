package ch.elexis.core.ui.commands;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;

public class TemplateRenameSettingsCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				if (textTemplate.isSystemTemplate()) {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.Template_not_editable,
							Messages.Template_not_editable_text);
				} else {
					List<String> existingNames = getExistingNames();
					InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), Messages.Edit_Template,
							Messages.Edit_Template_Description, textTemplate.getName(), newName -> {
								if (StringUtils.isBlank(newName)) {
									return "Eingabefeld darf nicht leer sein.";
								} else if (existingNames.contains(newName)) {
									return "Dieser Name ist bereits vergeben. Bitte wählen Sie einen anderen.";
								}
								return null;
							});

					if (dialog.open() == InputDialog.OK) {
						String newName = dialog.getValue();
						Brief template = textTemplate.getTemplate();
						if (template != null) {
							template.set(Brief.FLD_SUBJECT, newName);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
						}
					}
				}
			}
		}
		return null;
	}

	private List<String> getExistingNames() {
		IQuery<IDocumentTemplate> query = CoreModelServiceHolder.get().getQuery(IDocumentTemplate.class);
		List<IDocumentTemplate> existingTemplates = query.execute();
		List<String> existingNames = new ArrayList<>();
		for (IDocumentTemplate template : existingTemplates) {
			existingNames.add(template.getTitle());
		}
		return existingNames;
	}
}

