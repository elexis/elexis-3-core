package ch.elexis.core.findings.ui.views;

import java.util.Collections;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.findings.ui.views.nattable.DragAndDropSupport;
import ch.elexis.core.findings.ui.views.nattable.DynamicDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicHeaderDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicRowDataProvider;
import ch.elexis.core.findings.ui.views.nattable.FindingsNatTableTooltip;
import ch.elexis.core.findings.ui.views.nattable.LabelDataProvider;
import ch.elexis.core.findings.ui.views.nattable.NatTableWrapper;
import ch.elexis.core.findings.ui.views.nattable.NatTableWrapper.IDoubleClickListener;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;

public class FindingsViewDynamic extends ViewPart implements IActivationListener {
	
	private CodesSelectionComposite codeSelectionComposite;
	
	private NatTable natTable;
	private DynamicDataProvider dataProvider;
	private DynamicHeaderDataProvider headerDataProvider;
	private DynamicRowDataProvider rowDataProvider;
	
	private final ElexisUiEventListenerImpl eeli_find =
		new ElexisUiEventListenerImpl(IFinding.class,
			ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_DELETE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				refresh();
			}
		};
	
	private final ElexisUiEventListenerImpl eeli_code =
		new ElexisUiEventListenerImpl(ICoding.class,
			ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_DELETE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				codeRefresh();
			}
		};
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			refresh();
		}
	};
	
	private NatTableWrapper wrapper;
	
	public FindingsViewDynamic(){
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(SWTHelper.createGridLayout(true, 1));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		codeSelectionComposite = new CodesSelectionComposite(main, SWT.NONE);
		codeSelectionComposite
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		// NatTable setup
		dataProvider = new DynamicDataProvider();
		headerDataProvider = new DynamicHeaderDataProvider();
		rowDataProvider = new DynamicRowDataProvider(dataProvider);
		headerDataProvider.setShownCodings(Collections.emptyList());
		
		// wrap the data provider with a label data provider
		DataLayer bodyDataLayer =
			new DataLayer(new LabelDataProvider(dataProvider, new ObservationLabelProvider()));
		// disable drawing cells lines
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer) {
			private CellLayerPainter painter = new CellLayerPainter();
			
			@Override
			public ILayerPainter getLayerPainter(){
				return painter;
			}
		};
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		
		// build the column header layer stack
		DataLayer columnHeaderDataLayer = new DataLayer(headerDataProvider);
		ILayer columnHeaderLayer =
			new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		
		// build the row header layer
		DataLayer rowHeaderDataLayer = new DataLayer(rowDataProvider);
		ILayer rowHeaderLayer =
			new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);
		
		// build the corner layer
		IDataProvider cornerDataProvider =
			new DefaultCornerDataProvider(headerDataProvider, rowDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		// build the grid layer
		GridLayer gridLayer =
			new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

		natTable =
			new NatTable(main, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, gridLayer, false);
		natTable.setBackground(natTable.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		wrapper = new NatTableWrapper(natTable, dataProvider, selectionLayer);
		wrapper.addContextMenu("ch.elexis.core.findings.ui.views.FindingsView", getSite());
		getSite().setSelectionProvider(wrapper);
		wrapper.configure();
		
		natTable.setTheme(new ModernNatTableThemeConfiguration());
		
		codeSelectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				updateCodingsSelection(selection);
			}
		});
		updateCodingsSelection((StructuredSelection) codeSelectionComposite.getSelection());
		
		wrapper.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(NatTableWrapper source, ISelection selection){
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				if (!structuredSelection.isEmpty()) {
					for (Object o : structuredSelection.toList()) {
						if (o instanceof IFinding) {
							FindingsUiUtil.executeCommand("ch.elexis.core.findings.ui.commandEdit",
								(IFinding) o);
						}
					}
				}
			}
		});
		
		// add DnD support
		DragAndDropSupport dndSupport = new DragAndDropSupport(wrapper);
		Transfer[] transfer = {
			TextTransfer.getInstance()
		};
		natTable.addDragSupport(DND.DROP_COPY, transfer, dndSupport);
		
		atachTooltip();
		
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_find, eeli_code);
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	private void atachTooltip(){
		DefaultToolTip toolTip = new FindingsNatTableTooltip(natTable, dataProvider);
		toolTip.setPopupDelay(250);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));
	}
	
	private void updateCodingsSelection(StructuredSelection selection){
		dataProvider.setShownCodings(selection.toList());
		headerDataProvider.setShownCodings(selection.toList());
		natTable.refresh(true);
	}
	
	public void refresh(){
		dataProvider.reload(ElexisEventDispatcher.getSelectedPatient());
		natTable.refresh();
	}
	
	public void codeRefresh(){
		codeSelectionComposite.refresh();
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_find, eeli_code);
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}

	@Override
	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visible(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
}
