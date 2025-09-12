package ch.elexis.core.ui.commands;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;
import ch.elexis.data.Mandant;

public class CreateMandantTemplateCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IMandator mandant = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if (mandant == null) {
			return null;
		}

		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();

			// get brief via text template and clone it
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				Brief template = textTemplate.getTemplate();
				if (template != null) {
					Brief specTemplate = new Brief(template.getBetreff(), null, CoreHub.getLoggedInContact(),
							NoPoUtil.loadAsPersistentObject(mandant, Mandant.class), null, Brief.TEMPLATE);
					specTemplate.save(template.loadBinary(), template.getMimeType());

					TextTemplate specTextTemplate = new TextTemplate(specTemplate.getBetreff(), StringUtils.EMPTY,
							specTemplate.getMimeType());
					specTextTemplate.addFormTemplateReference(specTemplate);

					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
				}
			}
		}
		return null;
	}
}
