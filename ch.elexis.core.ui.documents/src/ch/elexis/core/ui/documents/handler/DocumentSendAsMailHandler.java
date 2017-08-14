package ch.elexis.core.ui.documents.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.data.Patient;

public class DocumentSendAsMailHandler extends AbstractHandler implements IHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentSendAsMailHandler.class);
	
	private File attachmentsFolder;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			List<?> iDocuments = ((StructuredSelection) selection).toList();
			
			List<File> attachments = new ArrayList<File>();
			for (Object iDocument : iDocuments) {
				if (iDocument instanceof IDocument) {
					Optional<File> tmpFile = getTempFile((IDocument) iDocument);
					if (tmpFile.isPresent()) {
						attachments.add(tmpFile.get());
					}
				}
			}
			if (!attachments.isEmpty()) {
				ICommandService commandService = (ICommandService) HandlerUtil
					.getActiveWorkbenchWindow(event).getService(ICommandService.class);
				try {
					String attachmentsString = getAttachmentsString(attachments);
					Command sendMailCommand =
						commandService.getCommand("ch.elexis.core.mail.ui.sendMail");
					
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("ch.elexis.core.mail.ui.sendMail.attachments", attachmentsString);
					Patient patient = ElexisEventDispatcher.getSelectedPatient();
					if (patient != null) {
						params.put("ch.elexis.core.mail.ui.sendMail.subject",
							"Patient: " + patient.getLabel());
					}
					
					ParameterizedCommand parametrizedCommmand =
						ParameterizedCommand.generateCommand(sendMailCommand, params);
					PlatformUI.getWorkbench().getService(IHandlerService.class)
						.executeCommand(parametrizedCommmand, null);
				} catch (Exception ex) {
					throw new RuntimeException("ch.elexis.core.mail.ui.sendMail not found", ex);
				}
			}
			removeTempAttachments(attachments);
		}
		return null;
	}
	
	private Optional<File> getTempFile(IDocument iDocument){
		File tmpDir = CoreHub.getTempDir();
		attachmentsFolder = new File(tmpDir, "_att" + System.currentTimeMillis() + "_");
		attachmentsFolder.mkdir();
		File tmpFile =
			new File(attachmentsFolder, iDocument.getTitle().endsWith(iDocument.getExtension())
					? iDocument.getTitle() : iDocument.getTitle() + "." + iDocument.getExtension());
		try (FileOutputStream fout = new FileOutputStream(tmpFile)) {
			Optional<InputStream> content =
				DocumentStoreServiceHolder.getService().loadContent(iDocument);
			if (content.isPresent()) {
				IOUtils.copy(content.get(), fout);
				content.get().close();
			}
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Fehler",
				"Dokument konnte nicht exportiert werden.");
			logger.error("Could not export IDocument.", e);
		}
		if (tmpFile != null && tmpFile.exists()) {
			return Optional.of(tmpFile);
		}
		return Optional.empty();
	}
	
	private void removeTempAttachments(List<File> attachments){
		for (File file : attachments) {
			file.delete();
		}
		if (attachmentsFolder != null && attachmentsFolder.exists()) {
			attachmentsFolder.delete();
		}
	}
	
	private String getAttachmentsString(List<File> attachments){
		StringBuilder sb = new StringBuilder();
		for (File file : attachments) {
			if (sb.length() > 0) {
				sb.append(":::");
			}
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}
}
