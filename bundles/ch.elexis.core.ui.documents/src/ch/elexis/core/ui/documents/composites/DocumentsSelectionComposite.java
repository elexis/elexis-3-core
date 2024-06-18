package ch.elexis.core.ui.documents.composites;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.documents.views.DocumentsViewerComparator;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.rgw.tools.TimeTool;

public class DocumentsSelectionComposite extends Composite {

	private TableViewer viewer;
	private DocumentsTableContentProvider contentProvider;
	private DocumentsViewerComparator documentsViewerComparator;
	private String filterText;

	public DocumentsSelectionComposite(Composite parent, int style) {
		super(parent, style);

		createContent();
	}

	private void createContent() {
		setLayout(new GridLayout());

		viewer = new TableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		contentProvider = new DocumentsTableContentProvider(viewer);
		viewer.setContentProvider(contentProvider);

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 200;
		viewer.getTable().setLayoutData(gd);

		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(StringUtils.EMPTY);
		column.setWidth(20);
		column.setResizable(true);
		column.setMoveable(false);
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					if (!doc.getStatus().isEmpty()) {
						return doc.getStatus().stream().map(s -> s.getName())
								.reduce((u, t) -> u + StringConstants.COMMA + t).get();
					}
				}
				return super.getToolTipText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					java.util.Optional<DocumentStatus> sent = doc.getStatus().stream()
							.filter(s -> s == DocumentStatus.SENT).findFirst();
					if (sent.isPresent()) {
						return Images.IMG_OUTBOX.getImage();
					}
					return Images.IMG_INBOX.getImage();
				}
				return super.getImage(element);
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		column = viewerColumn.getColumn();
		column.setText(Messages.Core_Category);
		column.setWidth(150);
		column.setResizable(true);
		column.setMoveable(false);
		column.addSelectionListener(getSelectionAdapter(column, 2));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return doc.getCategory().getName();
				} else if (element instanceof ICategory) {
					ICategory cat = (ICategory) element;
					return cat.getName();
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ICategory) {
					return Images.IMG_FOLDER.getImage();
				}
				return super.getImage(element);
			};
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		column = viewerColumn.getColumn();
		column.setText(Messages.Core_Date);
		column.setWidth(100);
		column.setResizable(true);
		column.setMoveable(false);
		column.addSelectionListener(getSelectionAdapter(column, 3));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getLastchanged()).toString(TimeTool.DATE_GER);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getLastchanged()).toString(TimeTool.LARGE_GER);
				}
				return super.getToolTipText(element);
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		column = viewerColumn.getColumn();
		column.setText(Messages.Core_Title);
		column.setWidth(250);
		column.setResizable(true);
		column.setMoveable(false);
		column.addSelectionListener(getSelectionAdapter(column, 5));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return doc.getTitle();
				}
				return StringUtils.EMPTY;
			};

			@Override
			public Image getImage(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					java.util.Optional<Identifiable> opt = DocumentStoreServiceHolder.getService()
							.getPersistenceObject(doc);
					if (opt.isPresent() && LocalDocumentServiceHolder.getService().get().contains(opt.get())) {
						return Images.IMG_EDIT.getImage();
					}
				}
				return super.getImage(element);
			};
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		column = viewerColumn.getColumn();
		column.setText(Messages.Core_Keywords);
		column.setWidth(250);
		column.setResizable(true);
		column.setMoveable(false);
		column.addSelectionListener(getSelectionAdapter(column, 5));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return doc.getKeywords();
				}
				return StringUtils.EMPTY;
			};
		});

		documentsViewerComparator = new DocumentsViewerComparator();
		documentsViewerComparator.setBFlat(true);
		viewer.setComparator(documentsViewerComparator);

		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					if (filterText == null || filterText.isEmpty()) {
						return true;
					}
					String lowerFilterText = filterText.toLowerCase();
					return doc.getTitle().toLowerCase().contains(lowerFilterText)
							|| doc.getKeywords().toLowerCase().contains(lowerFilterText)
							|| doc.getCategory().getName().toLowerCase().contains(lowerFilterText)
							|| new TimeTool(doc.getLastchanged()).toString(TimeTool.DATE_GER).contains(lowerFilterText);
				}
				return true;
			}
		});
	}

	public void setFilter(String filterText) {
		this.filterText = filterText;
		viewer.refresh();
	}

	private SelectionListener getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				documentsViewerComparator.setColumn(index);
				viewer.getTable().setSortDirection(documentsViewerComparator.getDirection());
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	public void addDoubleClickListener(IDoubleClickListener listener) {
		viewer.addDoubleClickListener(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	public IStructuredSelection getSelection() {
		return viewer.getStructuredSelection();
	}

	public void setPatient(IPatient patient) {
		viewer.setInput(patient);
	}
}
