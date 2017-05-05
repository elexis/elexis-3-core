package ch.elexis.core.ui.documents.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.documents.Messages;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;

public class DocumentExportHandler extends AbstractHandler implements IHandler {
	private static Logger logger = LoggerFactory.getLogger(DocumentExportHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			List<?> iDocuments = ((StructuredSelection) selection).toList();
			
			for (Object documentToExport : iDocuments) {
				if (documentToExport instanceof IDocument) {
					openExportDialog(shell, (IDocument) documentToExport);
				}
			}
		}
		return null;
	}
	
	private void openExportDialog(Shell shell, IDocument document){
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setFilterExtensions(createExtensionFilter(document));
		String fname = fd.open();
		if (fname != null) {
			try {
				if (DocumentStoreServiceHolder.getService().saveContentToFile(document,
					fname) == null) {
					SWTHelper.showError(Messages.DocumentView_exportErrorCaption,
						Messages.DocumentView_exportErrorEmptyText);
				}
			} catch (ElexisException e) {
				logger.error("cannot export file", e);
				SWTHelper.showError(Messages.DocumentView_exportErrorCaption,
					Messages.DocumentView_exportErrorText);
			}
		}
	}
	
	private String[] createExtensionFilter(IDocument document){
		List<String> filterExtensions = new ArrayList<>();
		if (document.getExtension() != null && !document.getExtension().isEmpty()) {
			filterExtensions.add("*." + document.getExtension());
		}
		filterExtensions.add("*.*");
		return filterExtensions.toArray(new String[0]);
	}
}
