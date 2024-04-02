package ch.elexis.core.ui.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.data.Patient;

public class SendHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICommandService commandService = (ICommandService) HandlerUtil.getActiveWorkbenchWindow(event)
				.getService(ICommandService.class);
		try {
			Command sendMailCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMail"); //$NON-NLS-1$

			HashMap<String, String> params = new HashMap<>();
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			if (patient != null) {
				params.put("ch.elexis.core.mail.ui.sendMail.subject", "Patient: " + patient.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			Optional<IDocumentLetter> selectedDocument = ContextServiceHolder.get().getTyped(IDocumentLetter.class);
			if (selectedDocument.isPresent()) {
				List<?> iDocuments = Collections.singletonList(selectedDocument.get());

				@SuppressWarnings("unchecked")
				String documentsString = AttachmentsUtil.getDocumentsString((List<IDocument>) (List<?>) iDocuments);
				params.put("ch.elexis.core.mail.ui.sendMail.documents", documentsString); //$NON-NLS-1$
			}

			ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(sendMailCommand, params);
			PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(parametrizedCommmand, null);
		} catch (Exception ex) {
			throw new RuntimeException("ch.elexis.core.mail.ui.sendMail not found", ex); //$NON-NLS-1$
		}
		return null;
	}
}
