package ch.elexis.core.mail.ui.dialogs;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.icons.Images;

public class AddAttachmentAction extends Action implements IAction {

	private AttachmentsComposite composite;

	public AddAttachmentAction(AttachmentsComposite attachmentsComposite) {
		this.composite = attachmentsComposite;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.IMG_NEW.getImageDescriptor();
	}

	@Override
	public void run() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command openSelectionCommand = commandService.getCommand("ch.elexis.core.ui.documents.commandOpenSelection");
		ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(openSelectionCommand,
				Collections.emptyMap());

		try {
			Object selection = PlatformUI.getWorkbench().getService(IHandlerService.class)
					.executeCommand(parametrizedCommmand, null);
			if (selection instanceof List) {
				for (Object selected : (List<?>) selection) {
					if (selected instanceof IDocument) {
						composite.addDocument((IDocument) selected);
					}
				}
			}
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
			LoggerFactory.getLogger(getClass()).error("Error adding document", e);
		}
	}
}
