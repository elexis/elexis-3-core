package ch.elexis.core.findings.ui.views;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.TimeTool;
import jakarta.inject.Inject;

public class FindingsView extends ViewPart implements IRefreshable {

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private TableViewer viewer;

	private FindingsComparator comparator;

	private String searchTitle;

	@Optional
	@Inject
	void crudFinding(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") IFinding finding) {
		CoreUiUtil.runAsyncIfActive(() -> {
			refresh();
		}, viewer);
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			refresh();
		}, viewer);
	}

	public FindingsView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(SWTHelper.createGridLayout(true, 1));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Text tSearch = new Text(main, SWT.BORDER);
		tSearch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		tSearch.setMessage("Suche");
		// Add search listener
		ModifyListener searchListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				searchTitle = tSearch.getText();
				refresh();
			}
		};
		tSearch.addModifyListener(searchListener);

		Composite c = new Composite(main, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		c.setLayout(tableColumnLayout);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer = new TableViewer(c, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		comparator = new FindingsComparator();
		viewer.setComparator(comparator);

		viewer.addFilter(new ViewFilterProvider());

		TableViewerColumn tableViewerColumnDateTime = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblcCol = tableViewerColumnDateTime.getColumn();
		tblcCol.setText("Erfassungsdatum");
		tableColumnLayout.setColumnData(tblcCol, new ColumnWeightData(20, true));
		tblcCol.addSelectionListener(getSelectionAdapter(tblcCol, 1));

		tableViewerColumnDateTime = new TableViewerColumn(viewer, SWT.NONE);
		tblcCol = tableViewerColumnDateTime.getColumn();
		tblcCol.setText("Befund");
		tableColumnLayout.setColumnData(tblcCol, new ColumnWeightData(80, true));
		tblcCol.addSelectionListener(getSelectionAdapter(tblcCol, 3));

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new FindingsLabelProvider());
		refresh();

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection structuredSelection = (StructuredSelection) event.getSelection();
				if (!structuredSelection.isEmpty()) {
					Object o = structuredSelection.getFirstElement();
					if (o instanceof IFinding) {
						FindingsUiUtil.executeCommand("ch.elexis.core.findings.ui.commandEdit", (IFinding) o);
					}
				}

			}
		});

		final Transfer[] dragTransferTypes = new Transfer[] { TextTransfer.getInstance() };

		viewer.addDragSupport(DND.DROP_COPY, dragTransferTypes, new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				StringBuilder sb = new StringBuilder();
				if (selection != null && !selection.isEmpty()) {
					IObservation observation = (IObservation) selection.getFirstElement();
					sb.append(((PersistentObject) observation).storeToString()).append(","); //$NON-NLS-1$
				}
				event.data = sb.toString().replace(",$", StringUtils.EMPTY); //$NON-NLS-1$
			}
		});

		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getTable());
		// set the menu on the SWT widget
		viewer.getTable().setMenu(menu);
		// register the menu with the framework
		getSite().registerContextMenu(menuManager, viewer);

		// make the viewer selection available
		getSite().setSelectionProvider(viewer);

		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void refresh() {
		viewer.setInput(getFindings(ContextServiceHolder.get().getActivePatient().orElse(null)));
	}

	public List<IFinding> getFindings(IPatient patient) {
		if (patient != null) {
			String patientId = patient.getId();
			List<IFinding> items = getObservations(patientId);
			/*
			 * TODO currently only observations needed
			 * items.addAll(getConditions(patientId));
			 * items.addAll(getClinicalImpressions(patientId));
			 * items.addAll(getPrecedureRequest(patientId));
			 */
			return items;
		}
		return Collections.emptyList();
	}

	private List<IFinding> getObservations(String patientId) {
		return FindingsServiceComponent.getService().getPatientsFindings(patientId, IObservation.class).stream()
				.filter(item -> {
					IObservation iObservation = (IObservation) item;
					ObservationCategory category = iObservation.getCategory();
					if (category == ObservationCategory.VITALSIGNS || category == ObservationCategory.SOAP_SUBJECTIVE
							|| category == ObservationCategory.SOAP_OBJECTIVE) {

						return !iObservation.isReferenced();
					}
					return false;
				}).collect(Collectors.toList());
	};

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				viewer.getTable().setSortDirection(comparator.getDirection());
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	class FindingsLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			IFinding item = (IFinding) element;

			switch (columnIndex) {
			case 0: {
				if (item instanceof IObservation) {
					IObservation ob = (IObservation) item;
					return ob.getEffectiveTime().isPresent()
							? new TimeTool(ob.getEffectiveTime().get()).toString(TimeTool.FULL_GER)
							: StringUtils.EMPTY;
				}
				break;

			}
			case 1: {
				return item.getText().isPresent() ? item.getText().get() : StringUtils.EMPTY;
			}
			default:
				break;
			}
			return StringUtils.EMPTY;
		}
	}

	class FindingsComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public FindingsComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			IFinding p1 = (IFinding) e1;
			IFinding p2 = (IFinding) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
			case 1:
				LocalDateTime t1 = LocalDateTime.MIN;
				LocalDateTime t2 = LocalDateTime.MIN;
				if (p1 instanceof IObservation) {
					t1 = ((IObservation) p1).getEffectiveTime().orElse(LocalDateTime.MIN);
				}
				if (p2 instanceof IObservation) {
					t2 = ((IObservation) p2).getEffectiveTime().orElse(LocalDateTime.MIN);
				}
				rc = t1.compareTo(t2);
				break;
			case 2:
				String txt1 = p1.getText().orElse(StringUtils.EMPTY);
				String txt2 = p2.getText().orElse(StringUtils.EMPTY);
				rc = txt1.toLowerCase().compareTo(txt2.toLowerCase());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}

	class ViewFilterProvider extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (searchTitle != null && !searchTitle.isEmpty()) {
				String searchText = searchTitle.toLowerCase();

				if (element instanceof IFinding) {
					IFinding iFinding = (IFinding) element;
					String text = iFinding.getText().orElse(StringUtils.EMPTY);
					if (text.toLowerCase().contains(searchText)) {
						return true;
					}
				}
				return false;
			}
			return true;
		}

	}
}
