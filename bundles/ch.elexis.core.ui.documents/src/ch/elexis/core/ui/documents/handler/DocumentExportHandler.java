package ch.elexis.core.ui.documents.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;

public class DocumentExportHandler extends AbstractHandler implements IHandler {
	private static Logger logger = LoggerFactory.getLogger(DocumentExportHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		List<?> selections = selection.toList();
		Set<Object> types = new HashSet<>();

		for (Object first : selections) {
			if (!types.contains(first.getClass())) {
				if (first instanceof IDocument) {
					if (types.stream().noneMatch(t -> t instanceof IDocument)) {
						types.add(first);
					}
				} else {
					types.add(first.getClass());
				}
			}
		}

		if (types.size() > 1) {
			MessageDialog.openInformation(shell, "Export Information",
					"Es können nicht Ordner und Dokumente gleichzeitig exportiert werden.");
		} else {
			Object selectionType = selection.getFirstElement();
			if (selectionType != null) {
				if (selectionType instanceof IDocument) {
					for (Object documentToExport : selections) {
						openExportDialog(shell, (IDocument) documentToExport);
					}
				} else if (selectionType instanceof FilterCategory) {
					openExportFilterCategoryDialog(shell, (FilterCategory) selectionType, selection);
				}
			}
		}
		return null;
	}

	private void openExportDialog(Shell shell, IDocument document) {
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setFileName(document.getTitle());
		fd.setFilterExtensions(createExtensionFilter(document));
		String fname = fd.open();

		if (fname != null) {
			try {
				if (DocumentStoreServiceHolder.getService().saveContentToFile(document, fname) == null) {
					SWTHelper.showError(Messages.Core_Error_while_exporting,
							Messages.DocumentView_exportErrorEmptyText);
				}
			} catch (ElexisException e) {
				logger.error("cannot export file", e); //$NON-NLS-1$
				SWTHelper.showError(Messages.Core_Error_while_exporting, Messages.DocumentView_exportErrorText);
			}
		}
	}

	private void openExportFilterCategoryDialog(Shell shell, FilterCategory filterCategory,
			IStructuredSelection selection) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE | SWT.MULTI);
		fileDialog.setFileName(filterCategory.getName());
		String filterCategoryName = fileDialog.open();

		if (filterCategoryName != null) {
			for (int i = 0; i < selection.size(); i++) {
				FilterCategory fc = (FilterCategory) selection.toList().get(i);
				File folder = new File(fileDialog.getFilterPath() + File.separator + fc.getName());
				folder.mkdir();

				IPatient activePatient = ContextServiceHolder.get().getActivePatient().orElse(null);
				List<IDocument> documentList = DocumentStoreServiceHolder.getService()
						.getDocumentsByCategory(activePatient.getId(), fc);

				Set<String> titles = new HashSet<String>();
				for (int a = 0; a < documentList.size(); a++) {
					IDocument document = (IDocument) documentList.get(a);

					String _titel = document.getTitle();
					if (titles.contains(document.getTitle())) {
						_titel = document.getTitle() + "(" + a + ")";
					} else {
						titles.add(document.getTitle());
					}

					if (document.getContent() != null) {
						File newFile = new File(
								folder.getAbsoluteFile() + File.separator + _titel + "." + document.getExtension());

						try (InputStream inputStream = documentList.get(a).getContent();
								FileOutputStream outputStream = new FileOutputStream(newFile)) {
							if (inputStream != null) {
								IOUtils.copy(inputStream, outputStream);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private String[] createExtensionFilter(IDocument document) {
		List<String> filterExtensions = new ArrayList<>();
		if (document.getExtension() != null && !document.getExtension().isEmpty()) {
			filterExtensions.add("*." + document.getExtension()); //$NON-NLS-1$
		}
		filterExtensions.add("*.*"); //$NON-NLS-1$
		return filterExtensions.toArray(new String[0]);
	}
}
