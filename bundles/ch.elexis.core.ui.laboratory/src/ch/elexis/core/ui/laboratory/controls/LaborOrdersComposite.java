package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.laboratory.Activator;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.laboratory.actions.LabOrderSetObservationDateAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultOrderDeleteAction;
import ch.elexis.core.ui.laboratory.controls.util.LabOrderEditingSupport;
import ch.elexis.data.LabOrder;
import ch.elexis.data.Patient;
import ch.elexis.data.LabOrder.State;
import ch.rgw.tools.TimeTool;

public class LaborOrdersComposite extends Composite {

	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;

	private TableViewer viewer;

	private int sortColumn = -1;
	private boolean revert = false;

	private boolean reloadPending;

	private boolean includeDone;
	private Map<String, IOutputLog> orderLogEntries = Collections.emptyMap();
	private Button btnHistory;
	private Job reloadJob;
	private long reloadGeneration;
	private final ISchedulingRule reloadRule = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	private Patient actPatient;
	private Composite toolComposite;
	private ToolBarManager toolbar;
	public static final String OUTPUTLOG_EXTERNES_LABOR = "Externes Labor"; //$NON-NLS-1$
	private boolean isGroupHighlightingEnabled = false;

	private static final String CFG_HIGHLIGHT_ENABLED = "labor/orders/highlight/enabled"; //$NON-NLS-1$
	private static final String CFG_HIGHLIGHT_COLOR = "labor/orders/highlight/color"; //$NON-NLS-1$
	private static final String DEFAULT_COLOR_HEX = "FDF9D9"; //$NON-NLS-1$
	private Color highlightColor;

	public LaborOrdersComposite(Composite parent, int style) {
		super(parent, style);
		loadConfiguration();
		createContent();
		selectPatient(ElexisEventDispatcher.getSelectedPatient());
	}

	public boolean isGroupHighlightingEnabled() {
		return isGroupHighlightingEnabled;
	}

	public void setGroupHighlightingEnabled(boolean value) {
		if (value != isGroupHighlightingEnabled) {
			isGroupHighlightingEnabled = value;
			ConfigServiceHolder.setUser(CFG_HIGHLIGHT_ENABLED, value);
			viewer.refresh();
		}
	}

	private void loadConfiguration() {
		isGroupHighlightingEnabled = ConfigServiceHolder.getUser(CFG_HIGHLIGHT_ENABLED, false);
		String hexColor = ConfigServiceHolder.getUser(CFG_HIGHLIGHT_COLOR, DEFAULT_COLOR_HEX);
		highlightColor = CoreUiUtil.getColorForString(hexColor);
	}

	private String rgbToHex(RGB rgb) {
		return String.format("%02X%02X%02X", rgb.red, rgb.green, rgb.blue); //$NON-NLS-1$
	}

	private RGB convertToPastel(RGB original) {
		double whiteFactor = 0.85;
		int r = (int) ((original.red * (1 - whiteFactor)) + (255 * whiteFactor));
		int g = (int) ((original.green * (1 - whiteFactor)) + (255 * whiteFactor));
		int b = (int) ((original.blue * (1 - whiteFactor)) + (255 * whiteFactor));
		return new RGB(r, g, b);
	}

	private void createContent() {
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());

		toolComposite = new Composite(body, SWT.NONE);

		GridLayout toolLayout = new GridLayout(3, false);
		toolLayout.marginWidth = 0;
		toolLayout.marginHeight = 0;
		toolLayout.horizontalSpacing = 15;
		toolComposite.setLayout(toolLayout);

		toolComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

		tk.adapt(toolComposite);

		Button btnHighlight = tk.createButton(toolComposite, Messages.LaborOrdersComposite_btnGroupHighlighting,
				SWT.CHECK);
		btnHighlight.setSelection(isGroupHighlightingEnabled);
		btnHighlight.setToolTipText(Messages.LaborOrdersComposite_btnGroupHighlightingTooltip);
		btnHighlight.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		if (highlightColor != null && !highlightColor.isDisposed()) {
			btnHighlight.setBackground(highlightColor);
		}
		btnHighlight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setGroupHighlightingEnabled(btnHighlight.getSelection());
			}
		});

		btnHistory = tk.createButton(toolComposite, Messages.LaborOrdersComposite_actionTooltipShowHistory,
				SWT.CHECK);
		btnHistory.setSelection(includeDone);
		btnHistory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		btnHistory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setIncludeDone(btnHistory.getSelection());
			}
		});
		Composite iconComposite = new Composite(toolComposite, SWT.NONE);
		iconComposite.setLayout(new FillLayout());

		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		gd.horizontalIndent = 10;
		iconComposite.setLayoutData(gd);
		tk.adapt(iconComposite);

		toolbar = new ToolBarManager();
		tk.adapt(toolbar.createControl(iconComposite));
		viewer = new TableViewer(body, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setSorter(new LaborOrdersSorter(this));

		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					List<LaborOrderViewerItem> orders = selection.toList();
					if (!orders.isEmpty()) {
						mgr.add(new LabOrderSetObservationDateAction(orders, viewer));
						mgr.add(new LaborResultEditDetailAction(orders, viewer));
						mgr.add(new LaborResultOrderDeleteAction(orders, viewer));
					}
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				viewer.refresh(true);
			}
		});

		btnHighlight.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ColorDialog dlg = new ColorDialog(getShell());
				dlg.setText(Messages.AgendaFarben_Titel);
				if (highlightColor != null && !highlightColor.isDisposed()) {
					dlg.setRGB(highlightColor.getRGB());
				}
				RGB selectedRGB = dlg.open();
				if (selectedRGB != null) {
					RGB pastelRGB = convertToPastel(selectedRGB);
					String hex = rgbToHex(pastelRGB);
					ConfigServiceHolder.setUser(CFG_HIGHLIGHT_COLOR, hex);
					highlightColor = CoreUiUtil.getColorForString(hex);
					btnHighlight.setBackground(highlightColor);
					viewer.refresh();
				}
			}
		});

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(28);
		column.getColumn().setText(StringUtils.EMPTY);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof LaborOrderViewerItem item) {
					IOutputLog log = orderLogEntries.get(item.getLabOrder().getId());
					if (log != null && OUTPUTLOG_EXTERNES_LABOR.equals(log.getOutputterStatus())) {
						return Images.IMG_BLOOD_TEST.getImage();
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(80);
		column.getColumn().setText(Messages.Core_Status);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(0, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return LabOrder.getStateLabel(((LaborOrderViewerItem) element).getState());
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(125);
		column.getColumn().setText(Messages.Core_Date);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(1, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					TimeTool time = ((LaborOrderViewerItem) element).getTime();
					if (time != null) {
						return time.toString(TimeTool.FULL_GER);
					} else {
						return "???"; //$NON-NLS-1$
					}
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(140);
		column.getColumn()
				.setText(Messages.LaborOrdersComposite_columnObservationTime);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(1, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					TimeTool time = ((LaborOrderViewerItem) element).getObservationTime();
					if (time != null) {
						return time.toString(TimeTool.FULL_GER);
					} else {
						return "???"; //$NON-NLS-1$
					}
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.Order_ID);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(2, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getOrderId().orElse(StringUtils.EMPTY);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.Core_Group);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(3, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getOrderGroupName().orElse(StringUtils.EMPTY);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(300);
		column.getColumn().setText(Messages.Core_Parameter);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(4, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getLabItemLabel().orElse(StringUtils.EMPTY);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.Core_Value);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getLabResultString().orElse("?"); //$NON-NLS-1$
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				return getRowColor(element, LaborOrdersComposite.this);
			}
		});

		column.setEditingSupport(new LabOrderEditingSupport(viewer));

		form.setText(Messages.Core_No_patient_selected);
    }

	private boolean isGroupSelected(Object element) {
		if (viewer.getSelection() instanceof IStructuredSelection selection) {
			if (selection.isEmpty()) {
				return false;
			}
			Optional<String> selectedGroup = selection.stream().filter(item -> item instanceof LaborOrderViewerItem)
					.map(item -> ((LaborOrderViewerItem) item).getOrderGroupName()).findFirst()
					.orElse(Optional.empty());
			if (selectedGroup.isEmpty()) {
				return false;
			}
			if (element instanceof LaborOrderViewerItem item) {
				return selectedGroup.get().equals(item.getOrderGroupName().orElse(null));
			}
		}
		return false;
	}

	private Color getRowColor(Object element, LaborOrdersComposite composite) {
		if (composite.isGroupHighlightingEnabled()) {
			if (composite.isGroupSelected(element)) {
					return composite.highlightColor;
			}
		}
		return null;
	}

	public void selectPatient(Patient patient) {
		setRedraw(false);
		try {
			if (patient != null) {
				if (!patient.equals(actPatient)) {
					actPatient = patient;
					form.setText(actPatient.getLabel());
					reload();
				}
			} else {
				actPatient = null;
				form.setText(Messages.Core_No_patient_selected);
				invalidateReload();
				orderLogEntries = Collections.emptyMap();
				viewer.setInput(Collections.emptyList());
				setLoading(false);
			}
		} finally {
			setRedraw(true);
		}
	}

	public void setIncludeDone(boolean value) {
		if (value != includeDone) {
			includeDone = value;
			reload();
		}
	}

	/**
	 * If visible full reload from database
	 */
	public void reload() {
		long generation = ++reloadGeneration;
		cancelReloadJob();
		if (!isVisible()) {
			reloadPending = true;
			return;
		}
		reloadPending = false;
		if (actPatient == null) {
			orderLogEntries = Collections.emptyMap();
			viewer.setInput(Collections.emptyList());
			setLoading(false);
			return;
		}

		boolean mandatorOnly = ConfigServiceHolder
				.getUser(Preferences.LABSETTINGS_CFG_SHOW_MANDANT_ORDERS_ONLY, false);
		String mandatorId = mandatorOnly ? ContextServiceHolder.getActiveMandatorOrNull().getId() : null;
		LoadRequest request = new LoadRequest(generation, actPatient.getId(), mandatorId, includeDone);
		beginLoading();
		scheduleReload(request);
	}

	@Override
	public boolean setFocus() {
		if (reloadPending) {
			reload();
		}
		return super.setFocus();
	}

	private void scheduleReload(LoadRequest request) {
		Display display = getDisplay();
		reloadJob = new Job(Messages.Core_loading) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					LoadResult result = loadOrders(request, monitor);
					if (result == null || monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (!display.isDisposed()) {
						display.asyncExec(() -> applyLoadResult(request, result));
					}
					return Status.OK_STATUS;
				} catch (Exception exception) {
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					if (!display.isDisposed()) {
						display.asyncExec(() -> applyLoadFailure(request));
					}
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not load laboratory orders", //$NON-NLS-1$
							exception);
				}
			}
		};
		reloadJob.setPriority(Job.SHORT);
		reloadJob.setUser(false);
		reloadJob.setRule(reloadRule);
		reloadJob.schedule();
	}

	private LoadResult loadOrders(LoadRequest request, IProgressMonitor monitor) {
		List<LabOrder> orders = LabOrder.getLabOrders(request.patientId(), request.mandatorId(), null, null, null, null,
				request.includeDone() ? null : State.ORDERED);
		if (monitor.isCanceled()) {
			return null;
		}
		List<LaborOrderViewerItem> viewerItems = new ArrayList<>();
		for (LabOrder order : orders) {
			if (monitor.isCanceled()) {
				return null;
			}
			viewerItems.add(new LaborOrderViewerItem(viewer, order));
		}
		Map<String, IOutputLog> loadedLogEntries = loadOrderLogEntries(viewerItems);
		if (monitor.isCanceled()) {
			return null;
		}
		viewerItems.sort(new Comparator<LaborOrderViewerItem>() {
			@Override
			public int compare(LaborOrderViewerItem lo1, LaborOrderViewerItem lo2) {
				String prio1 = lo1.getLabItemPrio().orElse(StringUtils.EMPTY);
				String prio2 = lo2.getLabItemPrio().orElse(StringUtils.EMPTY);
				return prio1.compareTo(prio2);
			}
		});
		return new LoadResult(viewerItems, loadedLogEntries);
	}

	private Map<String, IOutputLog> loadOrderLogEntries(List<LaborOrderViewerItem> viewerItems) {
		if (viewerItems.isEmpty()) {
			return Collections.emptyMap();
		}
		List<String> orderIds = viewerItems.stream().map(item -> item.getLabOrder().getId()).toList();
		IQuery<IOutputLog> query = CoreModelServiceHolder.get().getQuery(IOutputLog.class);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.IN, orderIds);
		Map<String, IOutputLog> loadedEntries = new HashMap<>();
		for (IOutputLog entry : query.execute()) {
			loadedEntries.putIfAbsent(entry.getObjectId(), entry);
		}
		return loadedEntries;
	}

	private void beginLoading() {
		setRedraw(false);
		try {
			orderLogEntries = Collections.emptyMap();
			viewer.setInput(Collections.emptyList());
			setLoading(true);
		} finally {
			setRedraw(true);
		}
	}

	private void applyLoadResult(LoadRequest request, LoadResult result) {
		if (!isCurrent(request)) {
			return;
		}
		reloadJob = null;
		if (!isVisible()) {
			reloadPending = true;
			++reloadGeneration;
			setLoading(false);
			return;
		}
		setRedraw(false);
		try {
			orderLogEntries = result.logEntries();
			viewer.setInput(result.viewerItems());
			setLoading(false);
		} finally {
			setRedraw(true);
		}
	}

	private void applyLoadFailure(LoadRequest request) {
		if (!isCurrent(request)) {
			return;
		}
		reloadJob = null;
		orderLogEntries = Collections.emptyMap();
		viewer.setInput(Collections.emptyList());
		setLoading(false);
	}

	private boolean isCurrent(LoadRequest request) {
		return !isDisposed() && viewer != null && !viewer.getControl().isDisposed()
				&& request.generation() == reloadGeneration && actPatient != null
				&& request.patientId().equals(actPatient.getId()) && request.includeDone() == includeDone;
	}

	private void setLoading(boolean loading) {
		if (viewer == null || viewer.getControl().isDisposed()) {
			return;
		}
		viewer.getControl().setEnabled(!loading);
		viewer.getControl().setCursor(loading ? getDisplay().getSystemCursor(SWT.CURSOR_WAIT) : null);
		if (btnHistory != null && !btnHistory.isDisposed()) {
			btnHistory.setEnabled(!loading);
		}
	}

	private void invalidateReload() {
		++reloadGeneration;
		cancelReloadJob();
	}

	private void cancelReloadJob() {
		if (reloadJob != null) {
			reloadJob.cancel();
			reloadJob = null;
		}
	}

	@Override
	public void dispose() {
		invalidateReload();
		super.dispose();
	}

	private record LoadRequest(long generation, String patientId, String mandatorId, boolean includeDone) {
	}

	private record LoadResult(List<LaborOrderViewerItem> viewerItems, Map<String, IOutputLog> logEntries) {
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public int getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(int sortColumn) {
		this.sortColumn = sortColumn;
	}

	public boolean isRevert() {
		return revert;
	}

	public void setRevert(boolean revert) {
		this.revert = revert;
	}

	private static class LaborOrdersSortSelection extends SelectionAdapter {
		private int columnIndex;
		private LaborOrdersComposite composite;

		public LaborOrdersSortSelection(int columnIndex, LaborOrdersComposite composite) {
			this.columnIndex = columnIndex;
			this.composite = composite;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (composite.getSortColumn() == columnIndex) {
				composite.setRevert(!composite.isRevert());
			} else {
				composite.setRevert(false);
			}
			composite.setSortColumn(columnIndex);
			composite.getViewer().refresh();
		}
	}

	private static class LaborOrdersSorter extends ViewerSorter {
		private LaborOrdersComposite composite;

		public LaborOrdersSorter(LaborOrdersComposite composite) {
			this.composite = composite;
		}

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			if (e1 instanceof LaborOrderViewerItem && e2 instanceof LaborOrderViewerItem) {
				LaborOrderViewerItem labOrder1 = (LaborOrderViewerItem) e1;
				LaborOrderViewerItem labOrder2 = (LaborOrderViewerItem) e2;
				switch (composite.getSortColumn()) {
				case 0:
					if (composite.isRevert()) {
						return labOrder1.getState().name().compareTo(labOrder2.getState().name());
					} else {
						return labOrder2.getState().name().compareTo(labOrder1.getState().name());
					}
				case 1:
					if (composite.isRevert()) {
						return labOrder1.getTime().compareTo(labOrder2.getTime());
					} else {
						return labOrder2.getTime().compareTo(labOrder1.getTime());
					}
				case 2:
					String orderId1 = labOrder1.getOrderId().orElse(StringUtils.EMPTY);
					String orderId2 = labOrder2.getOrderId().orElse(StringUtils.EMPTY);

					if (composite.isRevert()) {
						try {
							return Integer.decode(orderId1).compareTo(Integer.decode(orderId2));
						} catch (NumberFormatException ne) {
							// ignore just compare the strings ...
						}
						return (orderId1.compareTo(orderId2));
					} else {
						try {
							return Integer.decode(orderId2).compareTo(Integer.decode(orderId1));
						} catch (NumberFormatException ne) {
							// ignore just compare the strings ...
						}
						return orderId2.compareTo(orderId1);
					}
				case 3:
					if (composite.isRevert()) {
						return labOrder1.getOrderGroupName().orElse(StringUtils.EMPTY)
								.compareTo(labOrder2.getOrderGroupName().orElse(StringUtils.EMPTY));
					} else {
						return labOrder2.getOrderGroupName().orElse(StringUtils.EMPTY)
								.compareTo(labOrder1.getOrderGroupName().orElse(StringUtils.EMPTY));
					}
				case 4:
					if (composite.isRevert()) {
						return labOrder1.getLabItemLabel().orElse(StringUtils.EMPTY)
								.compareTo(labOrder2.getLabItemLabel().orElse(StringUtils.EMPTY));
					} else {
						return labOrder2.getLabItemLabel().orElse(StringUtils.EMPTY)
								.compareTo(labOrder1.getLabItemLabel().orElse(StringUtils.EMPTY));
					}
				default:
					// sort by time and item prio
					int timeCompare = labOrder2.getTime().compareTo(labOrder1.getTime());
					if (timeCompare == 0) {
						String prio1 = labOrder1.getLabItemPrio().orElse(StringUtils.EMPTY);
						String prio2 = labOrder2.getLabItemPrio().orElse(StringUtils.EMPTY);
						if (StringUtils.isNumeric(prio1) && StringUtils.isNumeric(prio2)) {
							try {
								return Integer.valueOf(prio1).compareTo(Integer.valueOf(prio2));
							} catch (NumberFormatException nfe) {
								// ignore
							}
						}
						return prio1.compareTo(prio2);
					}
					return timeCompare;
				}
			} else {
				return 0;
			}
		}
	}
	public static IOutputLog getOrderLogEntry(ILabOrder order) {
		if (order == null) {
			return null;
		}
		IQuery<IOutputLog> query = CoreModelServiceHolder.get().getQuery(IOutputLog.class);
		query.and(ModelPackage.Literals.IOUTPUT_LOG__OBJECT_ID, COMPARATOR.EQUALS, order.getId());
		return query.execute().isEmpty() ? null : query.execute().get(0);
	}
}
