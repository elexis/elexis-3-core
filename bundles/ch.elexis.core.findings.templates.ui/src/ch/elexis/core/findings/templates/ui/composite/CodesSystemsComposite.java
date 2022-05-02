package ch.elexis.core.findings.templates.ui.composite;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.ui.dlg.CodeCreateDialog;
import ch.elexis.core.findings.templates.ui.dlg.MergeLocalCodeDialog;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.icons.Images;

public class CodesSystemsComposite extends Composite {

	private TableViewer tableViewer;

	private int comparatorColumn;
	private int comparatorDirection = SWT.DOWN;

	public CodesSystemsComposite(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(4, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public void createContens() {
		Label lblCodeSystem = new Label(this, SWT.NONE);
		lblCodeSystem.setText("Code System:");
		Label lblCodeSystemText = new Label(this, SWT.NONE);
		lblCodeSystemText.setText(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());

		Button btnAdd = new Button(this, SWT.PUSH);
		btnAdd.setImage(Images.IMG_NEW.getImage());
		btnAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 2, 1));
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodeCreateDialog codeDialog = new CodeCreateDialog(getShell());
				if (codeDialog.open() == MessageDialog.OK) {
					loadTable();
				}
			}
		});

		tableViewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		TableViewerColumn col = createTableViewerColumn("Code", 300, 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ILocalCoding iCoding = (ILocalCoding) element;
				return getLabel(iCoding);
			}
		});
		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparatorColumn = 0;
				comparatorDirection = (comparatorDirection == SWT.UP ? SWT.DOWN : SWT.UP);
				tableViewer.refresh();
			}
		});

		col = createTableViewerColumn("Sequenz", 50, 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				ILocalCoding iCoding = (ILocalCoding) element;
				return Integer.toString(iCoding.getPrio());
			};
		});
		col.setEditingSupport(new EditingSupport(tableViewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof ILocalCoding && value instanceof String) {
					((ILocalCoding) element).setPrio(Integer.parseInt((String) value));
					FindingsServiceHolder.findingsModelService.save((ILocalCoding) element);
					tableViewer.update((ILocalCoding) element, null);
					ElexisEventDispatcher.getInstance()
							.fire(new ElexisEvent(null, ICoding.class, ElexisEvent.EVENT_RELOAD));
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof ILocalCoding) {
					return Integer.toString(((ILocalCoding) element).getPrio());
				}
				return null;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof ILocalCoding) {
					TextCellEditor ret = new TextCellEditor(tableViewer.getTable());
					ret.setValidator(new org.eclipse.jface.viewers.ICellEditorValidator() {
						@Override
						public String isValid(Object value) {
							if (value instanceof String) {
								try {
									Integer.parseInt((String) value);
								} catch (NumberFormatException e) {
									return "[" + value + "] ist nicht numerisch";
								}
							}
							return null;
						}
					});
					return ret;
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof ILocalCoding;
			}
		});
		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparatorColumn = 1;
				comparatorDirection = (comparatorDirection == SWT.UP ? SWT.DOWN : SWT.UP);
				tableViewer.refresh();
			}
		});

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);

		tableViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ILocalCoding left = (ILocalCoding) e1;
				ILocalCoding right = (ILocalCoding) e2;
				if (left != null && right != null) {
					if (comparatorColumn == 0) {
						if (comparatorDirection == SWT.DOWN) {
							return getLabel(left).compareTo(getLabel(right));
						} else {
							return getLabel(right).compareTo(getLabel(left));
						}
					} else if (comparatorColumn == 1) {
						if (comparatorDirection == SWT.DOWN) {
							return Integer.valueOf(left.getPrio()).compareTo(Integer.valueOf(right.getPrio()));
						} else {
							return Integer.valueOf(right.getPrio()).compareTo(Integer.valueOf(left.getPrio()));
						}
					}
				}
				return super.compare(viewer, e1, e2);
			}
		});

		loadTable();
		createContextMenu(tableViewer);
	}

	private String getLabel(ILocalCoding iCoding) {
		StringBuilder stringBuilder = new StringBuilder();
		for (ICoding mappedCoding : iCoding.getMappedCodes()) {

			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(mappedCoding.getSystem());
			stringBuilder.append(": ");
			stringBuilder.append(mappedCoding.getCode());
		}
		return iCoding != null
				? iCoding.getDisplay() + " (" + iCoding.getCode() + ")"
						+ (stringBuilder.length() > 0 ? (" [" + stringBuilder.toString() + "]") : StringUtils.EMPTY)
				: StringUtils.EMPTY;
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setData(title);
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}

	private void createContextMenu(Viewer viewer) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mgr) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.getFirstElement() instanceof ICoding) {
					fillContextMenu(mgr, selection.toArray());
				}
			}
		});

		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	public void loadTable() {
		List<ICoding> codings = FindingsServiceHolder.codingService
				.getAvailableCodes(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		codings.sort((a, b) -> ObjectUtils.compare(a.getDisplay(), b.getDisplay()));
		tableViewer.setInput(codings);
	}

	private void fillContextMenu(IMenuManager contextMenu, Object[] objects) {
		contextMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		contextMenu.add(new Action("Entfernen") {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_DELETE.getImageDescriptor();
			}

			@Override
			public void run() {
				if (objects != null) {
					for (Object o : objects) {
						if (o instanceof ICoding) {
							ICoding coding = (ICoding) o;
							IQuery<IObservation> obsQuery = FindingsServiceHolder.findingsModelService
									.getQuery(IObservation.class);
							obsQuery.and("content", COMPARATOR.LIKE, "%\"system\":\"" + coding.getSystem()
									+ "\",\"code\":\"" + coding.getCode() + "\"%");
							List<IObservation> obsWithCode = obsQuery.execute();
							if (obsWithCode.isEmpty() || MessageDialog.openQuestion(getShell(), "Code entfernen",
									"Der Code wird noch in " + obsWithCode.size()
											+ " Befunden verwendet.\nSoll er trotzdem entfernt werden?")) {
								FindingsServiceHolder.codingService.removeLocalCoding((ICoding) o);
							}
						}
					}
					loadTable();
				}
			}
		});
		contextMenu.add(new Action("Codes vereinen") {
			@Override
			public void run() {
				MergeLocalCodeDialog dialog = new MergeLocalCodeDialog(getShell());
				dialog.create();
				if (!((IStructuredSelection) tableViewer.getSelection()).isEmpty()) {
					dialog.setSource(
							(ILocalCoding) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement());
				}
				dialog.open();
				loadTable();
			}
		});
	}
}
