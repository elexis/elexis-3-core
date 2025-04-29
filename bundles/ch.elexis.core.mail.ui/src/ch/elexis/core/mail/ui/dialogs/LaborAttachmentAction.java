package ch.elexis.core.mail.ui.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.mail.ui.handlers.BriefeDocumentStoreHolder;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.ui.icons.Images;

public class LaborAttachmentAction extends Action implements IAction {

	private AttachmentsComposite composite;
	private Command createRocheLaborCommand;

	public LaborAttachmentAction(AttachmentsComposite attachmentsComposite, Command createRocheLaborCommand) {
		this.composite = attachmentsComposite;
		this.createRocheLaborCommand = createRocheLaborCommand;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.IMG_VIEW_LABORATORY.getImageDescriptor();
	}

	@Override
	public String getToolTipText() {
		return Messages.Create_Lab_attachment;
	}

	@Override
	public void run() {
		// now try to call the create roche labor command, is not part of core ...
		try {
			HashMap<String, String> params = new HashMap<>();
			ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(createRocheLaborCommand,
					params);
			Object pdfFile = PlatformUI.getWorkbench().getService(IHandlerService.class)
					.executeCommand(parametrizedCommmand, null);
			if (pdfFile instanceof File) {
				IPatient patient = ContextServiceHolder.get().getActivePatient().get();
				IDocument document = BriefeDocumentStoreHolder.get().createDocument(patient.getId(),
						"Laborblatt Mail"
								+ (StringUtils.isNoneBlank(composite.getPostfix()) ? " [" + composite.getPostfix() + "]"
										: StringUtils.EMPTY),
						BriefConstants.LABOR);
				document.setExtension("pdf");
				document.setStatus(DocumentStatus.SENT, true);
				try (FileInputStream fi = new FileInputStream((File) pdfFile)) {
					BriefeDocumentStoreHolder.get().saveDocument(document, fi);
				}

				String existingDocuments = composite.getDocuments();
				if (StringUtils.isEmpty(existingDocuments)) {
					composite.setDocuments(StoreToStringServiceHolder.getStoreToString(document));
				} else {
					composite.setDocuments(
							existingDocuments + ":::" + StoreToStringServiceHolder.getStoreToString(document));
				}
			}
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).warn("Error getting labor pdf", ex);
		}
		super.run();
	}
}
