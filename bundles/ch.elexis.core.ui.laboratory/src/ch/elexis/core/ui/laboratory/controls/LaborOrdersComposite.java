package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.actions.LabOrderSetObservationDateAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultOrderDeleteAction;
import ch.elexis.core.ui.laboratory.controls.util.LabOrderEditingSupport;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborOrdersComposite extends Composite {

	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;

	private TableViewer viewer;

	private int sortColumn = -1;
	private boolean revert = false;

	private boolean reloadPending;

	private boolean includeDone;

	private Patient actPatient;
	private Composite toolComposite;
	private ToolBarManager toolbar;

	public LaborOrdersComposite(Composite parent, int style) {
		super(parent, style);

		createContent();
		selectPatient(ElexisEventDispatcher.getSelectedPatient());
	}

	private void createContent() {
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());

		toolComposite = new Composite(body, SWT.NONE);
		toolComposite.setLayout(new FillLayout(SWT.VERTICAL));
		toolComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		toolbar = new ToolBarManager();
		toolbar.add(new Action("", Action.AS_CHECK_BOX) {

			@Override
			public String getText() {
				return Messages.LaborOrdersComposite_actionTooltipShowHistory;
			}

			@Override
			public String getToolTipText() {
				return Messages.LaborOrdersComposite_actionTooltipShowHistory;
			}

			@Override
			public void run() {
				setIncludeDone(!includeDone);
			}
		});
		tk.adapt(toolbar.createControl(toolComposite));
		tk.adapt(toolComposite);

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

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnState);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(0, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return LabOrder.getStateLabel(((LaborOrderViewerItem) element).getState());
				}
				return ""; //$NON-NLS-1$
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(125);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnDate);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(1, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					TimeTool time = ((LaborOrderViewerItem) element).getTime();
					if (time != null) {
						return time.toString(TimeTool.FULL_GER);
					} else {
						return "???";
					}
				}
				return ""; //$NON-NLS-1$
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnOrdernumber);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(2, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getOrderId().orElse("");
				}
				return ""; //$NON-NLS-1$
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnGroup);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(3, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getOrderGroupName().orElse("");
				}
				return ""; //$NON-NLS-1$
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(300);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnParameter);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(4, this));
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getLabItemLabel().orElse("");
				}
				return ""; //$NON-NLS-1$
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnValue);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LaborOrderViewerItem) {
					return ((LaborOrderViewerItem) element).getLabResultString().orElse("?");
				}
				return ""; //$NON-NLS-1$
			}
		});

		column.setEditingSupport(new LabOrderEditingSupport(viewer));

		form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
	}

	public void selectPatient(Patient patient) {
		setRedraw(false);
		if (patient != null) {
			if (!patient.equals(actPatient)) {
				actPatient = patient;
				form.setText(actPatient.getLabel());
				reload();
			}
		} else {
			actPatient = patient;
			form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
		}
		setRedraw(true);
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
		if (!isVisible()) {
			reloadPending = true;
			return;
		}
		setRedraw(false);
		reloadPending = false;
		if (actPatient != null) {
			viewer.setInput(getOrders());
		}
		setRedraw(true);
	}

	@Override
	public boolean setFocus() {
		if (reloadPending) {
			reload();
		}
		return super.setFocus();
	}

	private List<LaborOrderViewerItem> getOrders() {
		if (actPatient != null) {
			List<LabOrder> orders = null;
			if (ConfigServiceHolder.getUser(Preferences.LABSETTINGS_CFG_SHOW_MANDANT_ORDERS_ONLY, false)) {
				orders = LabOrder.getLabOrders(actPatient, CoreHub.actMandant, null, null, null, null,
						includeDone ? null : State.ORDERED);
			} else {
				orders = LabOrder.getLabOrders(actPatient, null, null, null, null, null,
						includeDone ? null : State.ORDERED);
			}
			// Sorting by priority of labItem
			if (orders != null) {
				List<LaborOrderViewerItem> viewerItems = new ArrayList<>();
				orders.forEach(order -> viewerItems.add(new LaborOrderViewerItem(viewer, order)));

				Collections.sort(viewerItems, new Comparator<LaborOrderViewerItem>() {

					@Override
					public int compare(LaborOrderViewerItem lo1, LaborOrderViewerItem lo2) {
						String prio1 = lo1.getLabItemPrio().orElse("");
						String prio2 = lo2.getLabItemPrio().orElse("");
						return prio1.compareTo(prio2);
					}
				});
				return viewerItems;
			}
		}
		return Collections.emptyList();
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
					String orderId1 = labOrder1.getOrderId().orElse("");
					String orderId2 = labOrder2.getOrderId().orElse("");

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
						return labOrder1.getOrderGroupName().orElse("")
								.compareTo(labOrder2.getOrderGroupName().orElse(""));
					} else {
						return labOrder2.getOrderGroupName().orElse("")
								.compareTo(labOrder1.getOrderGroupName().orElse(""));
					}
				case 4:
					if (composite.isRevert()) {
						return labOrder1.getLabItemLabel().orElse("").compareTo(labOrder2.getLabItemLabel().orElse(""));
					} else {
						return labOrder2.getLabItemLabel().orElse("").compareTo(labOrder1.getLabItemLabel().orElse(""));
					}
				default:
					// sort by time and item prio
					int timeCompare = labOrder2.getTime().compareTo(labOrder1.getTime());
					if (timeCompare == 0) {
						String prio1 = labOrder1.getLabItemPrio().orElse("");
						String prio2 = labOrder2.getLabItemPrio().orElse("");
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

}
