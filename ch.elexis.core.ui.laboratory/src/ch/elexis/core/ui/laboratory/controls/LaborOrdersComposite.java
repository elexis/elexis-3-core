package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborOrdersComposite extends Composite {
	
	private final String SMALLER = "<";
	private final String BIGGER = ">";
	
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	
	private TreeViewer viewer;
	private TreeViewerFocusCellManager focusCell;
	
	private int sortColumn = 1;
	private boolean revert = false;
	
	private Patient actPatient;
	
	private TextCellEditor textCellEditor;
	
	public LaborOrdersComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
		selectPatient(ElexisEventDispatcher.getSelectedPatient());
	}
	
	private LabResult createResult(LabOrder order, Kontakt origin){
		LabResult result = order.createResult(origin);
		result.setTransmissionTime(new TimeTool());
		return result;
	}
	
	private void createContent(){
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		
		viewer =
			new TreeViewer(body, SWT.FULL_SELECTION | SWT.MULTI | SWT.LEFT | SWT.V_SCROLL
				| SWT.H_SCROLL);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		viewer.setContentProvider(new LaborOrdersContentProvider());
		viewer.setSorter(new LaborOrdersSorter(this));
		
		focusCell =
			new TreeViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		
		ColumnViewerEditorActivationStrategy actSupport =
			new ColumnViewerEditorActivationStrategy(viewer) {
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event){
					return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.KEYPAD_CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
				}
			};
		
		TreeViewerEditor.create(viewer, focusCell, actSupport,
			ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION);
		
		// set up validation of the cell editors
		textCellEditor = new TextCellEditor(viewer.getTree());
		textCellEditor.setValidator(new ICellEditorValidator() {
			@Override
			public String isValid(Object value){
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				LabOrder order = (LabOrder) selection.getFirstElement();
				if (order != null && value instanceof String) {
					if (order.getLabItem().getTyp() == typ.NUMERIC
						|| order.getLabItem().getTyp() == typ.ABSOLUTE) {
						try {
							String editedValue = (String) value;
							System.out.println(editedValue);
							if (editedValue.startsWith(SMALLER) || editedValue.startsWith(BIGGER)) {
								String nrValue =
									editedValue.replace(SMALLER, "").replace(BIGGER, "");
								editedValue = nrValue.trim();
							}
							Float.parseFloat(editedValue);
							
						} catch (NumberFormatException e) {
							return Messages.LaborOrdersComposite_validatorNotNumber;
						}
					}
				}
				return null;
			}
		});
		textCellEditor.addListener(new ICellEditorListener() {
			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState){
				if (newValidState) {
					textCellEditor.getControl().setBackground(
						Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				} else {
					textCellEditor.getControl().setBackground(
						Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				}
			}
			
			@Override
			public void cancelEditor(){
				textCellEditor.getControl().setBackground(
					Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
			
			@Override
			public void applyEditorValue(){
				textCellEditor.getControl().setBackground(
					Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					mgr.add(new RemoveLaborOrdersAction(selection.toList()));
				}
				if (selection != null && !selection.isEmpty()) {
					ArrayList<LabOrder> orders = new ArrayList<LabOrder>();
					for (Object object : selection.toList()) {
						if (object instanceof LabOrder) {
							orders.add((LabOrder) object);
						}
					}
					mgr.add(new LaborResultEditDetailAction(orders, viewer));
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.LaborOrdersComposite_columnState);
		column.setLabelProvider(new ColumnLabelProvider() {
			
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
					result = labResult.getComment();
					if (result.length() > 20) {
						result = result.substring(0, 20);
					}
				}
				return result;
			}
		});
		
		column.setEditingSupport(new EditingSupport(viewer) {
			protected boolean canEdit(Object element){
				return (element instanceof LabOrder)
					&& (((LabOrder) element).getLabItem().getTyp() != typ.FORMULA);
			}
			
			protected CellEditor getCellEditor(Object element){
				if (element instanceof LabOrder) {
					LabItem labItem = ((LabOrder) element).getLabItem();
					if (labItem.getTyp() == typ.DOCUMENT) {
						return null;
					} else {
						return textCellEditor;
					}
				}
				return null;
			}
			
			protected Object getValue(Object element){
				if (element instanceof LabOrder) {
					LabItem labItem = ((LabOrder) element).getLabItem();
					if (labItem.getTyp() == typ.DOCUMENT) {
						return "Doc"; //$NON-NLS-1$
					} else if (labItem.getTyp() == typ.TEXT) {
						LabResult result = ((LabOrder) element).getLabResult();
						if (result == null) {
							result =
								createResult((LabOrder) element, LabOrder.getOrCreateManualLabor());
						}
						return result.getComment();
					} else {
						LabResult result = ((LabOrder) element).getLabResult();
						if (result == null) {
							result =
								createResult((LabOrder) element, LabOrder.getOrCreateManualLabor());
						}
						return result.getResult();
					}
				}
				return "???"; //$NON-NLS-1$
			}
			
			protected void setValue(Object element, Object value){
				if (element instanceof LabOrder && value != null) {
					LabResult result = ((LabOrder) element).getLabResult();
					
					if (result.getItem().getTyp() == typ.TEXT) {
						result.setResult("Text"); //$NON-NLS-1$
						result.set(LabResult.COMMENT, value.toString());
						((LabOrder) element).setState(LabOrder.State.DONE);
					} else if (result.getItem().getTyp() == typ.DOCUMENT) {
						// dont know what todo ...
					} else {
						result.setResult(value.toString());
						((LabOrder) element).setState(LabOrder.State.DONE);
					}
					int columnIdx = focusCell.getFocusCell().getColumnIndex();
					ViewerRow row = focusCell.getFocusCell().getViewerRow();
					ViewerRow nextRow = row.getNeighbor(ViewerRow.BELOW, true);
					viewer.refresh();
					if (nextRow != null) {
						viewer.setSelection(new StructuredSelection(nextRow.getElement()), true);
						viewer.editElement(nextRow.getElement(), columnIdx);
					}
				}
			}
		});
		
		form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
	}
	
	public void selectPatient(Patient patient){
		setRedraw(false);
		if (patient != null) {
			actPatient = patient;
			form.setText(actPatient.getLabel());
			viewer.setInput(LabOrder.getLabOrders(actPatient, CoreHub.actMandant, null, null, null,
				null, null));
			viewer.expandAll();
		} else {
			actPatient = patient;
			form.setText(Messages.LaborOrdersComposite_NoPatientSelected);
		}
		setRedraw(true);
	}
	
	public void reload(){
		setRedraw(false);
		if (actPatient != null) {
			viewer.setInput(LabOrder.getLabOrders(actPatient, CoreHub.actMandant, null, null, null,
				null, null));
			viewer.expandAll();
		}
		setRedraw(true);
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
	
	private static class LaborOrdersContentProvider implements ITreeContentProvider {
		private List<LabOrder> orders;
		
		private List<LabOrder> open = new ArrayList<LabOrder>();
		private List<LabOrder> done = new ArrayList<LabOrder>();
		
		private State[] roots = {
			State.ORDERED, State.DONE
		};
		
		@Override
		public void dispose(){
			// TODO Auto-generated method stub
			
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			if (newInput instanceof List<?>) {
				orders = (List<LabOrder>) newInput;
			}
		}
		
		private void updateLists(){
			open.clear();
			done.clear();
			for (LabOrder labOrder : orders) {
				if (labOrder.getState() == State.ORDERED) {
					open.add(labOrder);
				} else {
					done.add(labOrder);
				}
			}
		}
		
		@Override
		public Object[] getElements(Object inputElement){
			updateLists();
			return roots;
		}
		
		@Override
		public Object[] getChildren(Object parentElement){
			if (parentElement instanceof State) {
				if (parentElement == State.DONE) {
					return done.toArray();
				} else if (parentElement == State.ORDERED) {
					return open.toArray();
				}
			}
			return null;
		}
		
		@Override
		public Object getParent(Object element){
			return null;
		}
		
		@Override
		public boolean hasChildren(Object element){
			if (element instanceof State) {
				if (element == State.DONE) {
					return !done.isEmpty();
				} else if (element == State.ORDERED) {
					return !open.isEmpty();
				}
			}
			return false;
		}
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
					if (composite.isRevert()) {
						return (labOrder1.get(LabOrder.FLD_ORDERID).compareTo(labOrder2
							.get(LabOrder.FLD_ORDERID)));
					} else {
						return (labOrder2.get(LabOrder.FLD_ORDERID).compareTo(labOrder1
							.get(LabOrder.FLD_ORDERID)));
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
