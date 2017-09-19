package ch.elexis.core.findings.templates.ui.views;

import java.time.LocalDateTime;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.findings.templates.ui.dlg.FindingsEditDialog;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

@Component(service = {})
public class FindingsView extends ViewPart implements IActivationListener {
	
	public static FindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	
	private TableViewer viewer;
	
	private FindingsComparator comparator;
	
	private final ElexisUiEventListenerImpl eeli_find =
		new ElexisUiEventListenerImpl(IFinding.class, ElexisEvent.EVENT_CREATE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				setInput();
			}
			
		};
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			setInput();
		}
	};
	
	public FindingsView(){}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsTemplateService(FindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService service){
		codingService = service;
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		c.setLayout(tableColumnLayout);
		
		viewer = new TableViewer(c,
			SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		comparator = new FindingsComparator();
		viewer.setComparator(comparator);
		
		TableViewerColumn tableViewerColumnDateTime = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblcCol = tableViewerColumnDateTime.getColumn();
		tblcCol.setText("Datum");
		tableColumnLayout.setColumnData(tblcCol, new ColumnWeightData(10, true));
		tblcCol.addSelectionListener(getSelectionAdapter(tblcCol, 1));
		
		tableViewerColumnDateTime = new TableViewerColumn(viewer, SWT.NONE);
		tblcCol = tableViewerColumnDateTime.getColumn();
		tblcCol.setText("Typ");
		tableColumnLayout.setColumnData(tblcCol, new ColumnWeightData(10, true));
		tblcCol.addSelectionListener(getSelectionAdapter(tblcCol, 2));
		
		tableViewerColumnDateTime = new TableViewerColumn(viewer, SWT.NONE);
		tblcCol = tableViewerColumnDateTime.getColumn();
		tblcCol.setText("Befund");
		tableColumnLayout.setColumnData(tblcCol, new ColumnWeightData(40, true));
		tblcCol.addSelectionListener(getSelectionAdapter(tblcCol, 3));
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new FindingsLabelProvider());
		setInput();
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event){
				StructuredSelection structuredSelection =
					(StructuredSelection) event.getSelection();
				if (!structuredSelection.isEmpty()) {
					Object o = structuredSelection.getFirstElement();
					if (o instanceof IFinding) {
						IFinding selection = (IFinding) o;
						FindingsEditDialog findingsEditDialog = new FindingsEditDialog(
							Display.getDefault().getActiveShell(), selection);
						if (findingsEditDialog.open() == MessageDialog.OK) {
							setInput();
						}
					}
				}
				
			}
		});
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_find);
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	private void setInput(){
		viewer.setInput(
			findingsTemplateService.getFindings(ElexisEventDispatcher.getSelectedPatient()));
	}
	
	@Override
	public void setFocus(){
		
	}
	
	@Override
	public void activation(boolean mode){}
	
	@Override
	public void visible(boolean mode){
		if (!mode) {
			
		}
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_find);
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
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
		public Image getColumnImage(Object element, int columnIndex){
			return null;
		}
		
		@Override
		public String getColumnText(Object element, int columnIndex){
			IFinding item = (IFinding) element;
			
			switch (columnIndex) {
			case 0: {
				if (item instanceof IObservation) {
					IObservation ob = (IObservation) item;
					return ob.getEffectiveTime().isPresent()
							? new TimeTool(ob.getEffectiveTime().get()).toString(TimeTool.FULL_GER)
							: "";
				}
				break;
				
			}
			case 1: {
				return findingsTemplateService.getTypeAsText(item);
			}
			case 2: {
				return item.getText().isPresent() ? item.getText().get() : "";
			}
			default:
				break;
			}
			return "";
		}
	}
	
	class FindingsComparator extends ViewerComparator {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;
		
		public FindingsComparator(){
			this.propertyIndex = 0;
			direction = DESCENDING;
		}
		
		public int getDirection(){
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}
		
		public void setColumn(int column){
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
		public int compare(Viewer viewer, Object e1, Object e2){
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
				rc = findingsTemplateService.getTypeAsText(p1).toLowerCase()
					.compareTo(findingsTemplateService.getTypeAsText(p2).toLowerCase());
				break;
			case 3:
				String txt1 = p1.getText().orElse("");
				String txt2 = p2.getText().orElse("");
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
}
