package ch.elexis.core.ui.documents.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.documents.service.LocalLockServiceHolder;
import ch.elexis.core.ui.documents.views.DocumentsMetaDataDialog;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.SWTHelper;

public class DocumentCrudHandler extends AbstractHandler implements IHandler {
	private static Logger logger = LoggerFactory.getLogger(DocumentCrudHandler.class);

	public static final String CMD_NEW_DOCUMENT = "ch.elexis.core.ui.documents.commandCreate"; //$NON-NLS-1$
	public static final String CMD_UPDATE_DOCUMENT = "ch.elexis.core.ui.documents.commandUpdate"; //$NON-NLS-1$
	public static final String CMD_DELETE_DOCUMENT = "ch.elexis.core.ui.documents.commandDelete"; //$NON-NLS-1$

	public static final String PARAM_DOC_CATEGORY = "documents.category"; //$NON-NLS-1$
	public static final String PARAM_FILE_PATH = "documents.file.path"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);
		if (patient != null && patient.getId() != null) {
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

			switch (event.getCommand().getId()) {
			case CMD_NEW_DOCUMENT: {
				String category = null;
				String path = null;
				if (event.getParameter(PARAM_FILE_PATH) == null) {
					FileDialog fd = new FileDialog(shell, SWT.OPEN);
					path = fd.open();
				} else {
					path = event.getParameter(PARAM_FILE_PATH);
					category = event.getParameter(PARAM_DOC_CATEGORY);
				}
				if (path != null) {
					File file = new File(path);
					if (validateFile(file)) {
						IDocument document = DocumentStoreServiceHolder.getService().createDocument(null,
								patient.getId(), path, category);
						if (document != null) {
							return openMetaDataDialog(shell, document, file, ElexisEvent.EVENT_CREATE);
						}
					}
				}
				break;
			}

			case CMD_UPDATE_DOCUMENT: {
				ISelection selection = HandlerUtil.getCurrentSelection(event);
				if (selection instanceof StructuredSelection && !((StructuredSelection) selection).isEmpty()) {
					List<?> iDocuments = ((StructuredSelection) selection).toList();
					for (Object documentToEdit : iDocuments) {
						if (documentToEdit instanceof IDocument) {
							return openMetaDataDialog(shell, (IDocument) documentToEdit, null,
									ElexisEvent.EVENT_UPDATE);
						}
					}
				}
				break;
			}

			case CMD_DELETE_DOCUMENT:
				ISelection selection = HandlerUtil.getCurrentSelection(event);
				if (selection instanceof StructuredSelection && !((StructuredSelection) selection).isEmpty()) {
					List<?> iDocuments = ((StructuredSelection) selection).toList();
					for (Object documentToEdit : iDocuments) {
						if (documentToEdit instanceof IDocument) {
							openDeleteDialog(shell, (IDocument) documentToEdit, ElexisEvent.EVENT_DELETE);
						}
					}
					break;
				}
			}
		} else {
			SWTHelper.showInfo("Kein Patient ausgewählt", "Bitte wählen Sie einen Patienten aus!");
		}
		return null;
	}

	private boolean validateFile(File file) {
		if (!file.canRead()) {
			SWTHelper.showError(Messages.Core_Unable_to_read_file,
					MessageFormat.format(Messages.Core_cant_read_file_0, file));
			return false;
		} else if (file.getName().length() > 255) {
			SWTHelper.showError(Messages.Core_Unable_to_read_file, Messages.Core_Filename_too_long);
			return false;
		}
		return true;
	}

	private void openDeleteDialog(Shell shell, IDocument document, int eventType) {
		if (SWTHelper.askYesNo(Messages.Core_Really_delete_caption,
				MessageFormat.format(Messages.Core_Really_delete_0, document.getTitle()))) {

			Optional<Identifiable> documentPo = DocumentStoreServiceHolder.getService().getPersistenceObject(document);
			// we can only lock IPersistentObject based ...
			if (documentPo.isPresent()) {
				AcquireLockBlockingUi.aquireAndRun(documentPo.get(), new ILockHandler() {
					@Override
					public void lockFailed() {
						// no change required
					}

					@Override
					public void lockAcquired() {
						DocumentStoreServiceHolder.getService().removeDocument(document);
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, document);
					}
				});
			} else {
				DocumentStoreServiceHolder.getService().removeDocument(document);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, document);
			}
		}
	}

	private Optional<IDocument> openMetaDataDialog(Shell shell, IDocument document, File file, int eventType) {
		if (eventType == ElexisEvent.EVENT_CREATE) {
			Optional<IDocument> newDocument = openMetaDataDialogNoLocking(shell, document, file, eventType);

			newDocument.ifPresent(doc -> {
				Optional<Identifiable> documentPo = DocumentStoreServiceHolder.getService().getPersistenceObject(doc);
				documentPo.ifPresent(po -> {
					LocalLockServiceHolder.get().acquireLock(po);
					LocalLockServiceHolder.get().releaseLock(po);
				});
			});
			return newDocument;
		} else {
			Optional<Identifiable> documentPo = DocumentStoreServiceHolder.getService().getPersistenceObject(document);
			if (documentPo.isPresent()) {
				AcquireLockBlockingUi.aquireAndRun(documentPo.get(), new ILockHandler() {
					@Override
					public void lockFailed() {
						// no change required
					}

					@Override
					public void lockAcquired() {
						openMetaDataDialogNoLocking(shell, document, file, eventType);
					}
				});
			} else {
				return openMetaDataDialogNoLocking(shell, document, file, eventType);
			}
		}
		return Optional.empty();
	}

	private Optional<IDocument> openMetaDataDialogNoLocking(Shell shell, IDocument document, File file, int eventType) {
		DocumentsMetaDataDialog documentsMetaDataDialog = new DocumentsMetaDataDialog(document, shell);
		if (documentsMetaDataDialog.open() == Dialog.OK) {
			try {
				if (file != null) {
					try (InputStream fin = new FileInputStream(file)) {
						IDocument savedDocument = DocumentStoreServiceHolder.getService().saveDocument(document, fin);
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, savedDocument);
						return Optional.of(savedDocument);
					}
				} else {
					DocumentStoreServiceHolder.getService().saveDocument(document);
				}
			} catch (IOException e) {
				logger.error("file not found", e); //$NON-NLS-1$
				SWTHelper.showError(Messages.Core_Error_while_reading, Messages.Core_Error_Reading_Please_check_log);
			} catch (ElexisException e) {
				logger.error("cannot save", e); //$NON-NLS-1$
				SWTHelper.showError(Messages.Core_Error_with_document, Messages.DocumentView_saveErrorText);
			}
			// publish changes
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, document);
		}
		return Optional.empty();
	}
}
