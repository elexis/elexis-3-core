package ch.elexis.core.ui.documents.handler;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.documents.dialogs.DocumentsSelectionDialog;
import ch.elexis.core.ui.documents.views.DocumentsTreeContentProvider;
import ch.elexis.core.ui.icons.Images;
import ch.rgw.tools.TimeTool;

public class OpenSelectionHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell = HandlerUtil.getActiveShell(event);
		DocumentsSelectionDialog dialog = new DocumentsSelectionDialog(
				activeShell, new SelectionDialogLabelProvider(), new DocumentsTreeContentProvider(null)
						.selectFilterCategory(new StructuredSelection(new FilterCategory(null, StringUtils.EMPTY))),
				SWT.NONE);
		dialog.setInput(ContextServiceHolder.get().getActivePatient().orElse(null));
		dialog.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ICategory && e2 instanceof ICategory) {
					String cat1 = ((ICategory) e1).getName();
					String cat2 = ((ICategory) e2).getName();
					return cat1.compareToIgnoreCase(cat2);
				} else {
					IDocument dh1 = (IDocument) e1;
					IDocument dh2 = (IDocument) e2;
					return dh2.getLastchanged().compareTo(dh1.getLastchanged());
				}
			}
		});
		if (dialog.open() == Window.OK) {
			return Arrays.asList(dialog.getResult());
		}
		return null;
	}

	private class SelectionDialogLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICategory) {
				return Images.IMG_FOLDER.getImage();
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ICategory) {
				return ((ICategory) element).getName();
			} else if (element instanceof IDocument) {
				IDocument document = (IDocument) element;
				return new TimeTool(document.getLastchanged()).toString(TimeTool.FULL_GER) + ", " + document.getTitle(); //$NON-NLS-1$
			}
			return super.getText(element);
		}
	}
}
