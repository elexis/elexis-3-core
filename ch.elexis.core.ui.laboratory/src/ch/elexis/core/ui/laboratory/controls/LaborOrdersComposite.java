package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.actions.LabOrderSetObservationDateAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.core.ui.laboratory.controls.util.LabOrderEditingSupport;
import ch.elexis.core.ui.laboratory.preferences.LabSettings;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborOrdersComposite extends Composite {
	
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	
	private TreeViewer viewer;
	
	private int sortColumn = 1;
	private boolean revert = false;
	
	private Patient actPatient;
	
	public LaborOrdersComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
		selectPatient(ElexisEventDispatcher.getSelectedPatient());
	}
	
	private void createContent(){
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		
		viewer = new TreeViewer(body, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		viewer.setContentProvider(new LaborOrdersContentProvider());
		viewer.setSorter(new LaborOrdersSorter(this));
		
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					ArrayList<LabOrder> orders = new ArrayList<LabOrder>();
					for (Object object : selection.toList()) {
						if (object instanceof LabOrder) {
							orders.add((LabOrder) object);
						}
					}
					if (!orders.isEmpty()) {
						mgr.add(new RemoveLaborOrdersAction(selection.toList()));
						mgr.add(new LabOrderSetObservationDateAction(orders, viewer));
						mgr.add(new LaborResultEditDetailAction(orders, viewer));
					}
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnState);
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof State) {
					return LabOrder.getStateLabel(((State) element));
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(125);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnDate);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(1, this));
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof LabOrder) {
					TimeTool time = ((LabOrder) element).getTime();
					if (time != null) {
						return ((LabOrder) element).getTime().toString(TimeTool.FULL_GER);
					} else {
						return "???";
					}
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnOrdernumber);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(2, this));
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof LabOrder) {
					return ((LabOrder) element).get(LabOrder.FLD_ORDERID);
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnGroup);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(3, this));
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof LabOrder) {
					return ((LabOrder) element).get(LabOrder.FLD_GROUPNAME);
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(300);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnParameter);
		column.getColumn().addSelectionListener(new LaborOrdersSortSelection(4, this));
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof LabOrder) {
					if (((LabOrder) element).getLabItem() != null) {
						return ((LabOrder) element).getLabItem().getLabel();
					}
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(75);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnValue);
		column.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof LabOrder) {
					LabResult result = ((LabOrder) element).getLabResult();
					if (result != null) {
						return getNonEmptyResultString(result);
					} else {
						return "?"; //$NON-NLS-1$
					}
				}
				return ""; //$NON-NLS-1$
			}
			
			private String getNonEmptyResultString(LabResult labResult){
				String result = labResult.getResult();
				if (result != null && result.isEmpty()) {
					return "?"; //$NON-NLS-1$
				}
				if (labResult.getItem().getTyp() == typ.TEXT) {
					if (labResult.isLongText()) {
						result = labResult.getComment();
						if (result.length() > 20) {
							result = result.substring(0, 20);
						}
					}
				}
				return result;
			}
		});
		
		column.setEditingSupport(new LabOrderEditingSupport(viewer));
		
		form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
	}
	
	public void selectPatient(Patient patient){
		setRedraw(false);
		if (patient != null) {
			actPatient = patient;
			form.setText(actPatient.getLabel());
			viewer.setInput(getOrders());
			viewer.setExpandedElements(new Object[] {
				LabOrder.State.ORDERED
			});
		} else {
			actPatient = patient;
			form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
		}
		setRedraw(true);
	}
	
	public void reload(){
		setRedraw(false);
		if (actPatient != null) {
			viewer.setInput(getOrders());
			viewer.setExpandedElements(new Object[] {
				LabOrder.State.ORDERED
			});
		}
		setRedraw(true);
	}
	
	private List<LabOrder> getOrders(){
		List<LabOrder> ret = new ArrayList<LabOrder>();
		List<LabOrder> orders = null;
		if (CoreHub.userCfg.get(LabSettings.LABORDERS_SHOWMANDANTONLY, false)) {
			orders =
				LabOrder.getLabOrders(actPatient, CoreHub.actMandant, null, null, null, null, null);
		} else {
			orders = LabOrder.getLabOrders(actPatient, null, null, null, null, null, null);
		}
		
		// Sorting by priority of labItem
		if (orders != null) {
			Collections.sort(orders, new Comparator<LabOrder>() {
				@Override
				public int compare(LabOrder lo1, LabOrder lo2){
					String prio1 = "";
					String prio2 = "";
					if (lo1.getLabItem() != null && lo1.getLabItem().getPrio() != null) {
						prio1 = lo1.getLabItem().getPrio();
					}
					if (lo2.getLabItem() != null && lo2.getLabItem().getPrio() != null) {
						prio2 = lo2.getLabItem().getPrio();
					}
					return prio1.compareTo(prio2);
				}
			});
			ret.addAll(orders);
		}
		return ret;
	}
	
	public TreeViewer getViewer(){
		return viewer;
	}
	
	public int getSortColumn(){
		return sortColumn;
	}
	
	public void setSortColumn(int sortColumn){
		this.sortColumn = sortColumn;
	}
	
	public boolean isRevert(){
		return revert;
	}
	
	public void setRevert(boolean revert){
		this.revert = revert;
	}
	
	private static class LaborOrdersSortSelection extends SelectionAdapter {
		private int columnIndex;
		private LaborOrdersComposite composite;
		
		public LaborOrdersSortSelection(int columnIndex, LaborOrdersComposite composite){
			this.columnIndex = columnIndex;
			this.composite = composite;
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e){
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
		
		public LaborOrdersSorter(LaborOrdersComposite composite){
			this.composite = composite;
		}
		
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2){
			if (e1 instanceof LabOrder && e2 instanceof LabOrder) {
				LabOrder labOrder1 = (LabOrder) e1;
				LabOrder labOrder2 = (LabOrder) e2;
				switch (composite.getSortColumn()) {
				case 1:
					if (composite.isRevert()) {
						return labOrder1.getTime().compareTo(labOrder2.getTime());
					} else {
						return labOrder2.getTime().compareTo(labOrder1.getTime());
					}
				case 2:
					String orderId1 = labOrder1.get(LabOrder.FLD_ORDERID);
					String orderId2 = labOrder2.get(LabOrder.FLD_ORDERID);
					
					if (composite.isRevert()) {
						if (orderId1 != null && orderId2 != null) {
							try {
								return Integer.decode(orderId1).compareTo(Integer.decode(orderId2));
							} catch (NumberFormatException ne) {
								// ignore just compare the strings ...
							}
						}
						return (orderId1.compareTo(orderId2));
					} else {
						if (orderId1 != null && orderId2 != null) {
							try {
								return Integer.decode(orderId2).compareTo(Integer.decode(orderId1));
							} catch (NumberFormatException ne) {
								// ignore just compare the strings ...
							}
						}
						return (orderId2.compareTo(orderId1));
					}
				case 3:
					if (composite.isRevert()) {
						return (labOrder1.get(LabOrder.FLD_GROUPNAME).compareTo(labOrder2
							.get(LabOrder.FLD_GROUPNAME)));
					} else {
						return (labOrder2.get(LabOrder.FLD_GROUPNAME).compareTo(labOrder1
							.get(LabOrder.FLD_GROUPNAME)));
					}
				case 4:
					if (composite.isRevert()) {
						return (labOrder1.getLabItem().getLabel().compareTo(labOrder2.getLabItem()
							.getLabel()));
					} else {
						return (labOrder2.getLabItem().getLabel().compareTo(labOrder1.getLabItem()
							.getLabel()));
					}
				default:
					return 0;
				}
			} else {
				return 0;
			}
		}
	}
	
	private static class RemoveLaborOrdersAction extends Action {
		private List<?> selectedOrders;
		
		public RemoveLaborOrdersAction(List list){
			super(Messages.LaborOrdersComposite_actionTitelRemoveWithResult);
			selectedOrders = list;
		}
		
		@Override
		public void run(){
			for (Object object : selectedOrders) {
				if (object instanceof LabOrder) {
					LabResult result = ((LabOrder) object).getLabResult();
					if (result != null) {
						result.delete();
					}
					((LabOrder) object).delete();
				}
			}
			ElexisEventDispatcher.reload(LabOrder.class);
		}
	}
	
	public void expandAll(){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.expandAll();
		}
	}
	
	public void collapseAll(){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.collapseAll();
		}
	}
}
