package ch.elexis.core.ui.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;

public class SendBriefAsMailHandler extends AbstractHandler implements IHandler {

	private static final Logger logger = LoggerFactory.getLogger(SendBriefAsMailHandler.class);

	private File attachmentsFolder;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IDocumentLetter> selectedDocument = ContextServiceHolder.get().getTyped(IDocumentLetter.class);
		if (selectedDocument.isPresent()) {
			List<?> iDocuments = Collections.singletonList(selectedDocument.get());

			if (!iDocuments.isEmpty()) {
				ICommandService commandService = (ICommandService) HandlerUtil.getActiveWorkbenchWindow(event)
						.getService(ICommandService.class);
				try {
					@SuppressWarnings("unchecked")
					String documentsString = AttachmentsUtil.getDocumentsString((List<IDocument>) (List<?>) iDocuments);
					Command sendMailCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMail"); //$NON-NLS-1$

					HashMap<String, String> params = new HashMap<String, String>();
					params.put("ch.elexis.core.mail.ui.sendMail.documents", documentsString); //$NON-NLS-1$
					Patient patient = ElexisEventDispatcher.getSelectedPatient();
					if (patient != null) {
						params.put("ch.elexis.core.mail.ui.sendMail.subject", "Patient: " + patient.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
					}

					ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(sendMailCommand,
							params);
					PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(parametrizedCommmand,
							null);
				} catch (Exception ex) {
					throw new RuntimeException("ch.elexis.core.mail.ui.sendMail not found", ex); //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	private Optional<File> getTempFile(Brief brief) {
		File tmpDir = CoreHub.getTempDir();
		attachmentsFolder = new File(tmpDir, "_att" + System.currentTimeMillis() + "_"); //$NON-NLS-1$ //$NON-NLS-2$
		attachmentsFolder.mkdir();
		File tmpFile = new File(attachmentsFolder, brief.getBetreff() + "." + brief.getMimeType()); //$NON-NLS-1$
		try (FileOutputStream fout = new FileOutputStream(tmpFile)) {
			byte[] arr = brief.loadBinary();
			if (arr != null && arr.length > 0) {
				fout.write(arr);
			}
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Fehler",
					"Brief konnte nicht exportiert werden.");
			logger.error("Could not export Brief.", e); //$NON-NLS-1$
		}
		if (tmpFile != null && tmpFile.exists()) {
			return Optional.of(tmpFile);
		}
		return Optional.empty();
	}

	private void removeTempAttachments(List<File> attachments) {
		for (File file : attachments) {
			file.delete();
		}
		if (attachmentsFolder != null && attachmentsFolder.exists()) {
			attachmentsFolder.delete();
		}
	}

	private String getAttachmentsString(List<File> attachments) {
		StringBuilder sb = new StringBuilder();
		for (File file : attachments) {
			if (sb.length() > 0) {
				sb.append(":::"); //$NON-NLS-1$
			}
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}
}
