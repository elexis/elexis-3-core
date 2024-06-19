package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.interfaces.ILabItem;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.actions.LaborParameterEditAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultOrderDeleteAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultSetNonPathologicAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultSetPathologicAction;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.core.ui.laboratory.controls.util.ChangeNewDateSelection;
import ch.elexis.core.ui.laboratory.controls.util.ChangeResultsDateSelection;
import ch.elexis.core.ui.laboratory.controls.util.DisplayDoubleClickListener;
import ch.elexis.core.ui.laboratory.controls.util.LabResultEditingSupport;
import ch.elexis.core.ui.laboratory.controls.util.LaborResultsLabelProvider;
import ch.elexis.core.ui.laboratory.views.LaborView;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class LaborResultsComposite extends Composite {

	public static final String ID = "ch.elexis.LaborResultsComposite"; //$NON-NLS-1$
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	private Patient actPatient;
	public TreeViewer viewer;
	private TreeViewerFocusCellManager focusCell;
	private TreeViewerColumn newColumn;
	private int newColumnIndex;
	public final static String COLUMN_DATE_KEY = "labresult.date"; //$NON-NLS-1$
	private List<TreeViewerColumn> resultColumns = new ArrayList<TreeViewerColumn>();
	private LaborResultsContentProvider contentProvider = new LaborResultsContentProvider();
	private int columnOffset = 0;
	private boolean reloadPending;
	private static final int COLUMNS_PER_PAGE = 7;
	private TreeViewerColumn checkboxColumn;
	private List<TreeItem> selectedItems = new ArrayList<>();
	private LaborView parentLaborView;

	private LaborChartPopupManager laborChartPopupManager;

	private boolean showHistogramPopup = ConfigServiceHolder.getUser(Preferences.LABSETTINGS_HISTOGRAM_POPUP, false);
	private boolean mouseTrackListenerAdded = false;
	private MouseTrackAdapter mouseTrackListener;

	public LaborResultsComposite(Composite parent, int style, LaborView parentLaborView) {
		super(parent, style);
		this.parentLaborView = parentLaborView;
		laborChartPopupManager = new LaborChartPopupManager(actPatient);
		createContent();
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public int getColumnOffset() {
		return columnOffset;
	}

	public void setColumnOffset(int newOffset) {
		List<TimeTool> dates = contentProvider.getDates();
		if (dates.size() <= COLUMNS_PER_PAGE) {
			columnOffset = 0;
		} else {
			if ((newOffset + COLUMNS_PER_PAGE <= dates.size()) && (newOffset >= 0)) {
				columnOffset = newOffset;
			}
		}
	}

	private void setInitialColumnOffset() {
		List<TimeTool> dates = contentProvider.getDates();
		int offset = dates.size() - COLUMNS_PER_PAGE;
		if (offset > 0) {
			setColumnOffset(offset);
		} else {
			setColumnOffset(0);
		}
	}

	private void createContent() {
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());

		viewer = new TreeViewer(body, SWT.CHECK | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		// Listener for checkbox selection
		viewer.getTree().addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				TreeItem item = (TreeItem) event.item;
				if (item.getChecked()) {
					if (selectedItems.size() >= 5) {
						item.setChecked(false);
						MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						messageBox.setMessage("Sie können maximal 5 Elemente auswählen.");
						messageBox.setText("Auswahlgrenze erreicht");
						messageBox.open();
					} else {
						selectedItems.add(item);
					}
				} else {
					selectedItems.remove(item);
				}

				parentLaborView.setSelectedItems(selectedItems);
			}
		});

		// Checkbox column
		checkboxColumn = new TreeViewerColumn(viewer, SWT.NONE);
		checkboxColumn.getColumn().setWidth(0);
		checkboxColumn.getColumn().setText("");
		checkboxColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});

		viewer.setContentProvider(contentProvider);

		focusCell = new TreeViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		viewer.addDoubleClickListener(new DisplayDoubleClickListener(this));

		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				List<LabResult> results = getSelectedResults();
				if (results != null) {
					mgr.add(new LaborResultSetPathologicAction(results, viewer));
					mgr.add(new LaborResultSetNonPathologicAction(results, viewer));
					mgr.add(new LaborResultEditDetailAction(results, viewer));
					mgr.add(new LaborResultOrderDeleteAction(results, viewer));
					mgr.add(new Separator());
					mgr.add(new LaborParameterEditAction(results, viewer));
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));

		// Parameter column (with tree structure)
		TreeViewerColumn parameterColumn = new TreeViewerColumn(viewer, SWT.NONE);
		parameterColumn.getColumn().setWidth(200);
		parameterColumn.getColumn().setText(Messages.Core_Parameter);
		parameterColumn.setLabelProvider(new ColumnLabelProvider() {
			private StringBuilder sb = new StringBuilder();

			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					String groupName = (String) element;
					if ((groupName.length() > 2) && groupName.charAt(1) == ' ') {
						groupName = groupName.substring(2);
					}
					return groupName;
				} else if (element instanceof LaborItemResults) {
					sb.setLength(0);
					ILabItem item = ((LaborItemResults) element).getFirstResult().getItem();
					sb.append("       " + item.getKuerzel()).append(" - ").append(item.getName()).append(" [") //$NON-NLS-1$ //$NON-NLS-2$
							.append(item.getUnit()).append("]"); //$NON-NLS-1$
					return sb.toString();
				}
				return StringUtils.EMPTY;
			}
		});

		// Reference column
		TreeViewerColumn referenceColumn = new TreeViewerColumn(viewer, SWT.NONE);
		referenceColumn.getColumn().setWidth(100);
		referenceColumn.getColumn().setText(Messages.Core_Reference);
		referenceColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LaborItemResults) {
					if (actPatient.getGeschlecht().equals(Person.MALE)) {
						return ((LaborItemResults) element).getFirstResult().getRefMale();
					} else {
						return ((LaborItemResults) element).getFirstResult().getRefFemale();
					}
				}
				return StringUtils.EMPTY;
			}
		});

		// New column
		newColumn = new TreeViewerColumn(viewer, SWT.NONE);
		newColumn.getColumn().setWidth(5);
		TimeTool now = new TimeTool();
		newColumn.getColumn().setText("Neu (" + now.toString(TimeTool.DATE_GER) + ")");
		newColumn.getColumn().setData(COLUMN_DATE_KEY, now);
		newColumn.getColumn().addSelectionListener(new ChangeNewDateSelection(newColumn, this));
		newColumn.setLabelProvider(new LaborResultsLabelProvider(newColumn));
		newColumn.setEditingSupport(new LabResultEditingSupport(this, viewer, newColumn));
		newColumnIndex = 2;

		// Result columns
		for (int i = 0; i < COLUMNS_PER_PAGE; i++) {
			TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
			column.getColumn().setWidth(75);
			column.getColumn().setText(StringUtils.EMPTY);
			column.setLabelProvider(new LaborResultsLabelProvider(column));
			column.getColumn().addSelectionListener(new ChangeResultsDateSelection(column, this));
			resultColumns.add(column);
		}
		if (showHistogramPopup) {
			addTreeMouseTrackListener();
		}
	}

	private void addTreeMouseTrackListener() {
		mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) {
				if (laborChartPopupManager != null && !laborChartPopupManager.dispose()) {
					laborChartPopupManager.dispose();
				}
				Point point = new Point(e.x, e.y);
				TreeItem item = viewer.getTree().getItem(point);
				laborChartPopupManager.dispose();
				createChartPopup(item, e);
			}
			@Override
			public void mouseExit(MouseEvent e) {
				if (laborChartPopupManager != null && !laborChartPopupManager.dispose()) {
					laborChartPopupManager.dispose();
				}
			}
		};
		viewer.getTree().addMouseTrackListener(mouseTrackListener);
		mouseTrackListenerAdded = true;
	}

	public void createChartPopup(TreeItem item, MouseEvent e) {
		Object data = item.getData();
		if (data instanceof LaborItemResults) {
			laborChartPopupManager.createChartPopup(item, e, viewer, actPatient);
		}
	}

	public List<LabResult> getSelectedResults() {
		ViewerCell cell = focusCell.getFocusCell();
		return getSelectedResults(cell);
	}

	private List<LabResult> getSelectedResults(ViewerCell cell) {
		if (cell != null && cell.getColumnIndex() > 2) {
			TreeViewerColumn column = resultColumns.get(cell.getColumnIndex() - 4);
			TimeTool time = (TimeTool) column.getColumn().getData(COLUMN_DATE_KEY);
			if ((time != null) && (cell.getElement() instanceof LaborItemResults)) {
				LaborItemResults results = (LaborItemResults) cell.getElement();
				return results.getResult(time.toString(TimeTool.DATE_COMPACT));
			}
		}
		return null;
	}

	public String[] getPrintHeaders() {
		ArrayList<String> ret = new ArrayList<>();
		TreeColumn[] columns = viewer.getTree().getColumns();
		for (TreeColumn treeColumn : columns) {
			if (treeColumn != newColumn.getColumn()) {
				ret.add(treeColumn.getText());
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

	public TreeItem[] getPrintRows() {
		ArrayList<TreeItem> ret = new ArrayList<TreeItem>();
		getAllItems(viewer.getTree(), ret);
		return ret.toArray(new TreeItem[ret.size()]);
	}

	private void getAllItems(Tree tree, List<TreeItem> allItems) {
		for (TreeItem item : tree.getItems()) {
			getAllItems(item, allItems);
		}
	}

	private void getAllItems(TreeItem currentItem, List<TreeItem> allItems) {
		TreeItem[] children = currentItem.getItems();
		for (int i = 0; i < children.length; i++) {
			allItems.add(children[i]);
			getAllItems(children[i], allItems);
		}
	}

	/**
	 * Reload all content and update the Viewer
	 */
	public void reload() {
		viewer.getContentProvider().inputChanged(viewer, null, LabResult.getGrouped(actPatient));

		for (int i = 0; i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, null);
			resultColumns.get(i).getColumn().setText(StringUtils.EMPTY);
		}

		List<TimeTool> dates = contentProvider.getDates();
		for (int i = 0; i < dates.size() && i < resultColumns.size() && ((i + columnOffset) < dates.size()); i++) {
			resultColumns.get(i).getColumn().setText(dates.get(i + columnOffset).toString(TimeTool.DATE_GER));
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, dates.get(i + columnOffset));
		}
		viewer.refresh();
	}

	/**
	 * Reload all content and update the Viewer with the data of the Patient.
	 */
	public void selectPatient(Patient patient) {
		actPatient = patient;
		if (!isVisible()) {
			reloadPending = true;
			return;
		}
		setRedraw(false);
		resetCheckboxes();
		viewer.setInput(LabResult.getGrouped(actPatient));

		TimeTool now = new TimeTool();
		newColumn.getColumn().setData(COLUMN_DATE_KEY, now);
		newColumn.getColumn().setText("Neu (" + now.toString(TimeTool.DATE_GER) + ")");

		setInitialColumnOffset();
		for (int i = 0; i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, null);
			resultColumns.get(i).getColumn().setText(StringUtils.EMPTY);
		}

		List<TimeTool> dates = contentProvider.getDates();
		for (int i = 0; i < dates.size() && i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn().setText(dates.get(i + columnOffset).toString(TimeTool.DATE_GER));
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, dates.get(i + columnOffset));
		}

		viewer.expandAll();
		setRedraw(true);
	}

	public void resetCheckboxes() {
		for (TreeItem item : selectedItems) {
			item.setChecked(false);
		}
		selectedItems.clear();
		parentLaborView.setSelectedItems(selectedItems);
	}

	@Override
	public boolean setFocus() {
		if (reloadPending) {
			selectPatient(actPatient);
			reloadPending = false;
		}
		boolean showHistogramPopup = ConfigServiceHolder.getUser(Preferences.LABSETTINGS_HISTOGRAM_POPUP, false);
		if (showHistogramPopup) {
			if (!mouseTrackListenerAdded) {
				addTreeMouseTrackListener();
				mouseTrackListenerAdded = true;
			}
		} else {
			if (mouseTrackListenerAdded) {
				viewer.getTree().removeMouseTrackListener(mouseTrackListener);
				mouseTrackListenerAdded = false;
				ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
			}
		}
		if (laborChartPopupManager != null && !laborChartPopupManager.dispose()) {
			laborChartPopupManager.dispose();
		}

		return super.setFocus();
	}

	public Patient getPatient() {
		return actPatient;
	}

	public void expandAll() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.expandAll();
		}
	}

	public void collapseAll() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.collapseAll();
		}
	}

	public void toggleNewColumn() {
		if ((newColumn.getColumn().getWidth() > 10)) {
			newColumn.getColumn().setWidth(5);
		} else {
			newColumn.getColumn().setWidth(100);
		}
		viewer.refresh();
	}

	public int[] getSkipIndex() {
		int[] ret = new int[1];
		ret[0] = newColumnIndex;
		return ret;
	}

	public void showCheckboxes(boolean show) {
		if (show) {
			checkboxColumn.getColumn().setWidth(70);
		} else {
			checkboxColumn.getColumn().setWidth(0);
		}
		viewer.refresh();
	}

	public List<TreeItem> getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(List<TreeItem> selectedItems) {
		this.selectedItems = selectedItems;
	}
}
