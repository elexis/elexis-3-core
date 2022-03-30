package ch.elexis.core.ui.documents.dialogs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.documents.views.DocumentsTreeContentProvider;

public class DocumentsSelectionDialog extends CheckedTreeSelectionDialog {

	private LastSevenViewerFilter lastSevenFilter;

	public DocumentsSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider,
			int style) {
		super(parent, labelProvider, contentProvider, style);
		setStatusLineAboveButtons(false);
		lastSevenFilter = new LastSevenViewerFilter();
	}

	@Override
	public void create() {
		super.create();
		if (getTreeViewer().getContentProvider() instanceof DocumentsTreeContentProvider) {
			((DocumentsTreeContentProvider) getTreeViewer().getContentProvider()).setViewer(getTreeViewer());
		}
	}

	@Override
	protected Composite createSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(composite.getFont());
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		buttonComposite.setLayoutData(data);
		Button lastSevenButton = new Button(buttonComposite, SWT.TOGGLE);
		lastSevenButton.setText("Dokumente der letzten 7 Tage");
		lastSevenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastSevenButton.getSelection()) {
					getTreeViewer().addFilter(lastSevenFilter);
					getTreeViewer().expandAll();
				} else {
					getTreeViewer().removeFilter(lastSevenFilter);
					getTreeViewer().refresh();
				}
			}
		});
		return buttonComposite;
	}

	private class LastSevenViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IDocument) {
				IDocument document = (IDocument) element;
				LocalDate lastChange = LocalDateTime
						.ofInstant(document.getLastchanged().toInstant(), ZoneId.systemDefault()).toLocalDate();
				return ChronoUnit.DAYS.between(lastChange, LocalDate.now()) < 8;
			}
			return true;
		}
	}
}
