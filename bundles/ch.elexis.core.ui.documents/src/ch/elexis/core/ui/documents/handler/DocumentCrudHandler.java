package ch.elexis.core.ui.documents.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.documents.Messages;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.documents.views.DocumentsMetaDataDialog;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;

public class DocumentCrudHandler extends AbstractHandler implements IHandler {
	private static Logger logger = LoggerFactory.getLogger(DocumentCrudHandler.class);
	
	public static final String CMD_NEW_DOCUMENT = "ch.elexis.core.ui.documents.commandCreate";
	private static final String CMD_UPDATE_DOCUMENT = "ch.elexis.core.ui.documents.commandUpdate";
	private static final String CMD_DELETE_DOCUMENT = "ch.elexis.core.ui.documents.commandDelete";
	
	public static final String PARAM_DOC_CATEGORY = "documents.category";
	public static final String PARAM_FILE_PATH = "documents.file.path";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
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
						IDocument document =
							DocumentStoreServiceHolder.getService().createDocument(null,
							patient.getId(), path, category);
						if (document != null) {
							openMetaDataDialog(shell, document, file, ElexisEvent.EVENT_CREATE);
						}
					}
				}
				break;
			}
			
			case CMD_UPDATE_DOCUMENT: {
				ISelection selection = HandlerUtil.getCurrentSelection(event);
				if (selection instanceof StructuredSelection
					&& !((StructuredSelection) selection).isEmpty()) {
					List<?> iDocuments = ((StructuredSelection) selection).toList();
					for (Object documentToEdit : iDocuments) {
						if (documentToEdit instanceof IDocument) {
							openMetaDataDialog(shell, (IDocument) documentToEdit, null,
								ElexisEvent.EVENT_UPDATE);
						}
					}
				}
				break;
			}
			
			case CMD_DELETE_DOCUMENT:
				ISelection selection = HandlerUtil.getCurrentSelection(event);
				if (selection instanceof StructuredSelection
					&& !((StructuredSelection) selection).isEmpty()) {
					List<?> iDocuments = ((StructuredSelection) selection).toList();
					for (Object documentToEdit : iDocuments) {
						if (documentToEdit instanceof IDocument) {
							openDeleteDialog(shell, (IDocument) documentToEdit,
								ElexisEvent.EVENT_DELETE);
						}
					}
					break;
				}
			}
		}
		else {
			SWTHelper.showInfo("Kein Patient ausgewählt", "Bitte wählen Sie einen Patienten aus!");
		}
		return null;
	}
	
	private boolean validateFile(File file){
		if (!file.canRead()) {
			SWTHelper.showError(Messages.DocumentView_cantReadCaption,
				MessageFormat.format(Messages.DocumentView_cantReadText, file));
			return false;
		} else if (file.getName().length() > 255) {
			SWTHelper.showError(Messages.DocumentView_cantReadCaption,
				Messages.DocumentView_fileNameTooLong);
			return false;
		}
		return true;
	}
	
	private void openDeleteDialog(Shell shell, IDocument document, int eventType){
		if (SWTHelper.askYesNo(Messages.DocumentView_reallyDeleteCaption, MessageFormat
			.format(Messages.DocumentView_reallyDeleteContents, document.getTitle()))) {
			
			Optional<Identifiable> documentPo =
				DocumentStoreServiceHolder.getService().getPersistenceObject(document);
			// we can only lock IPersistentObject based ...
			if (documentPo.isPresent()) {
				AcquireLockBlockingUi.aquireAndRun(documentPo.get(), new ILockHandler() {
					@Override
					public void lockFailed(){
						// no change required
					}
					
					@Override
					public void lockAcquired(){
						DocumentStoreServiceHolder.getService().removeDocument(document);
						ElexisEventDispatcher.getInstance().fire(new ElexisEvent(document,
							IDocument.class, eventType, ElexisEvent.PRIORITY_NORMAL));
					}
				});
			} else {
				DocumentStoreServiceHolder.getService().removeDocument(document);
				ElexisEventDispatcher.getInstance().fire(new ElexisEvent(document, IDocument.class,
					eventType, ElexisEvent.PRIORITY_NORMAL));
			}
		}
	}
	
	private void openMetaDataDialog(Shell shell, IDocument document, File file, int eventType){
		if (eventType == ElexisEvent.EVENT_CREATE) {
			Optional<IDocument> newDocument =
				openMetaDataDialogNoLocking(shell, document, file, eventType);
			
			newDocument.ifPresent(doc -> {
				Optional<Identifiable> documentPo =
					DocumentStoreServiceHolder.getService().getPersistenceObject(doc);
				documentPo.ifPresent(po -> {
					CoreHub.getLocalLockService().acquireLock(po);
					CoreHub.getLocalLockService().releaseLock(po);
				});
			});
		} else {
			Optional<Identifiable> documentPo =
				DocumentStoreServiceHolder.getService().getPersistenceObject(document);
			if (documentPo.isPresent()) {
				AcquireLockBlockingUi.aquireAndRun(documentPo.get(), new ILockHandler() {
					@Override
					public void lockFailed(){
						// no change required
					}
					
					@Override
					public void lockAcquired(){
						openMetaDataDialogNoLocking(shell, document, file, eventType);
					}
				});
			} else {
				openMetaDataDialogNoLocking(shell, document, file, eventType);
			}
		}
	}
	
	private Optional<IDocument> openMetaDataDialogNoLocking(Shell shell, IDocument document,
		File file, int eventType){
		DocumentsMetaDataDialog documentsMetaDataDialog =
			new DocumentsMetaDataDialog(document, shell);
		if (documentsMetaDataDialog.open() == Dialog.OK) {
			try {
				IDocument savedDocument = DocumentStoreServiceHolder.getService()
					.saveDocument(document, file != null ? new FileInputStream(file) : null);
				ElexisEventDispatcher.getInstance().fire(new ElexisEvent(savedDocument,
					IDocument.class, eventType, ElexisEvent.PRIORITY_NORMAL));
				return Optional.of(savedDocument);
			} catch (FileNotFoundException e) {
				logger.error("file not found", e);
				SWTHelper.showError(Messages.DocumentView_importErrorCaption,
					Messages.DocumentView_importErrorText2);
			} catch (ElexisException e) {
				logger.error("cannot save", e);
				SWTHelper.showError(Messages.DocumentView_saveErrorCaption,
					Messages.DocumentView_saveErrorText);
			}
		}
		return Optional.empty();
	}
}
