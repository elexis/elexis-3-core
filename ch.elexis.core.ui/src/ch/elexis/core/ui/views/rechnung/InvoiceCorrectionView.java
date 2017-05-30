package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;

public class InvoiceCorrectionView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.rechnung.InvoiceCorrectionView";
	private static Logger logger = LoggerFactory.getLogger(InvoiceCorrectionView.class);
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(1, false));
		
		String[] todoValues = new String[] {
			"102", "SUVA", "06991212", "Uns wurde kein Unfall gemeldet", "Max Mustermann"
		};
		
		List<String[]> invoiences = new ArrayList<>();
		invoiences.add(new String[] {
			"1.1.2017", "1", "0000.0002", "Konsultation erste 5 min", "43.23"
		});
		
		List<String[]> diagnoses = new ArrayList<>();
		diagnoses.add(new String[] {
			"01 Kapitel I - Bestimmte infektöse und parasitäre Krankheiten - (ADO-B999)"
		});
		
		InvoiceComposite invoiceComposite = new InvoiceComposite(parent);
		invoiceComposite.createComponents(todoValues, diagnoses, invoiences);
	}
	
	@Override
	public void setFocus(){
		
	}
	
	class InvoiceComposite extends ScrolledComposite {
		Composite wrapper;
		
		public InvoiceComposite(Composite parent){
			super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			setLayout(new GridLayout(1, false));
			setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		}
		
		public void createComponents(String[] todoValues, List<String[]> diagnoses,
			List<String[]> invoinces){
			wrapper = new Composite(this, SWT.NONE);
			wrapper.setLayout(new GridLayout(1, false));
			wrapper.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			InvoiceHeaderComposite invoiceHeaderComposite = new InvoiceHeaderComposite(wrapper);
			InvoiceContentComposite invoiceContentComposite = new InvoiceContentComposite(wrapper);
			
			invoiceHeaderComposite.createComponents(todoValues);
			invoiceContentComposite.createComponents(diagnoses, invoinces);
			
			this.setContent(wrapper);
			this.setExpandHorizontal(true);
			this.setExpandVertical(true);
			updateScrollBars();
		}
		
		public void updateScrollBars(){
			if (wrapper != null) {
				this.setMinSize(wrapper.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				this.layout(true, true);
			}
			
		}
		
	}
	
	class InvoiceHeaderComposite extends Composite {
		
		String[] lbls = new String[] {
			"Rechnung", "Empfänger", "Telefon Versicherer", "Rückweisungsgrund", "Sachbearbeiter/in"
		};
		
		public InvoiceHeaderComposite(Composite parent){
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(6, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
		}
		
		public void createComponents(String[] values){
			
			Label lblTitle = new Label(this, SWT.NONE);
			lblTitle.setText("Rechnungsangaben");
			lblTitle.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
			lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 10, 1));
			
			this.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			if (values.length == lbls.length) {
				FormToolkit tk = UiDesk.getToolkit();
				int i = 0;
				for (String lbl : lbls) {
					tk.createLabel(this, lbl);
					tk.createLabel(this, values[i++]);
					if (i == 4) {
						tk.createLabel(this, "");
						tk.createLabel(this, "");
					}
				}
			} else {
				logger.error("cannot load invoice header data - values size mismatch [expected: "
					+ lbls.length + ", current: " + values.length + "]");
			}
		}
	}
	
	class InvoiceContentComposite extends Composite {
		public InvoiceContentComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 5;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		
		public void createComponents(List<String[]> diagnoses, List<String[]> invoinces){
			
			InvoiceContentHeaderComposite invoiceContentHeaderComposite =
				new InvoiceContentHeaderComposite(this);
			InvoiceContentMiddleComposite invoiceContentMiddleComposite =
				new InvoiceContentMiddleComposite(this);
			invoiceContentHeaderComposite.createComponents();
			invoiceContentMiddleComposite.createComponents(diagnoses, invoinces);
		}
	}
	
	class InvoiceContentHeaderComposite extends Composite {
		public InvoiceContentHeaderComposite(Composite parent){
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(10, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 10, 1));
		}
		
		public void createComponents(){
			this.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			Label lblTitle = new Label(this, SWT.NONE);
			lblTitle.setText("Fallangaben");
			lblTitle.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
			lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 10, 1));
			
			Label lblLaw = new Label(this, SWT.NONE);
			lblLaw.setText("Gesetz");
			
			ComboViewer cbLaw = new ComboViewer(this, SWT.BORDER);
			cbLaw.setContentProvider(ArrayContentProvider.getInstance());
			cbLaw.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					return String.valueOf(element);
				}
			});
			cbLaw.setInput(new String[] {
				"UVG", "KVG"
			});
			cbLaw.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event){
					ISelection selection = event.getSelection();
					if (selection instanceof StructuredSelection) {
						if (!selection.isEmpty()) {
						
						}
					}
				}
			});
						
			Label lblRecipient = new Label(this, SWT.NONE);
			lblRecipient.setText("Empfänger");
			
			ComboViewer cbRecipient = new ComboViewer(this, SWT.BORDER);
			cbRecipient.setContentProvider(ArrayContentProvider.getInstance());
			cbRecipient.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					return String.valueOf(element);
				}
			});
			cbRecipient.setInput(new String[] {
				"SUVA", "PATIENT"
			});
			cbRecipient.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event){
					ISelection selection = event.getSelection();
					if (selection instanceof StructuredSelection) {
						if (!selection.isEmpty()) {
						
						}
					}
				}
			});
						
			Label lblGarant = new Label(this, SWT.NONE);
			lblGarant.setText("Kostenträger");
			
			ComboViewer cbGarant = new ComboViewer(this, SWT.BORDER);
			cbGarant.setContentProvider(ArrayContentProvider.getInstance());
			cbGarant.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					return String.valueOf(element);
				}
			});
			cbGarant.setInput(new String[] {
				"SUVA", "PATIENT"
			});
			cbGarant.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event){
					ISelection selection = event.getSelection();
					if (selection instanceof StructuredSelection) {
						if (!selection.isEmpty()) {
						
						}
					}
				}
			});
						
			Label lblAccidentDate = new Label(this, SWT.NONE);
			lblAccidentDate.setText("Unfall Datum");
			DateTime dateAccident = new DateTime(this, SWT.DATE | SWT.DROP_DOWN);
			
			Label lblAccidentNr = new Label(this, SWT.NONE);
			lblAccidentNr.setText("Unfall Nummer");
			Label txtAccidentNr = new Label(this, SWT.NONE);
			txtAccidentNr.setText("343434");
		}
	}
	
	class InvoiceContentMiddleComposite extends Composite {
		
		public InvoiceContentMiddleComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 5;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		
		public void createComponents(List<String[]> diagnoses, List<String[]> invoinces){
			for (int i = 0; i < 3; i++) {
				Group group = new Group(this, SWT.BORDER);
				group.setText("Konsultation vom " + (i + 1) + ".1.2017");
				group.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
				GridLayout gd = new GridLayout(1, false);
				gd.marginWidth = 0;
				gd.marginHeight = 0;
				group.setLayout(gd);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				InvoiceContentDiagnosisComposite invoiceContentDiagnosisComposite =
					new InvoiceContentDiagnosisComposite(group);
				InvoiceContentKonsultationComposite invoiceContentKonsultationComposite =
					new InvoiceContentKonsultationComposite(group);
				invoiceContentDiagnosisComposite.createComponents(diagnoses);
				invoiceContentKonsultationComposite.createComponents(invoinces);
			}
			
		}
	}
	
	class InvoiceContentKonsultationComposite extends Composite {
		TableViewer tableViewer;
		TableColumnLayout tableColumnLayout;
		
		public InvoiceContentKonsultationComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 0;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		
		private void createComponents(List<String[]> values){
			Composite tableArea = new Composite(this, SWT.NONE);
			tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			tableColumnLayout = new TableColumnLayout();
			tableArea.setLayout(tableColumnLayout);
			
			tableViewer = new TableViewer(tableArea, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
			Table table = tableViewer.getTable();
			table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			TableViewerColumn tcKonsDate = createTableViewerColumn("Kons. Datum", 4, 0);
			TableViewerColumn tcSize = createTableViewerColumn("Anzahl", 2, 1);
			TableViewerColumn tcServiceCode = createTableViewerColumn("Leistungscode", 5, 2);
			TableViewerColumn tcSericeText = createTableViewerColumn("Leistungstext", 10, 3);
			TableViewerColumn tcPrice = createTableViewerColumn("Preis", 4, 4);
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(values);
		}
		
		private TableViewerColumn createTableViewerColumn(String title, int bound, int colIdx){
			final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			column.setText(title);
			tableColumnLayout.setColumnData(column,
				new ColumnWeightData(bound, ColumnWeightData.MINIMUM_WIDTH, true));
			column.setResizable(true);
			column.setMoveable(false);
			viewerColumn.setLabelProvider(new DefaultColumnLabelProvider(colIdx));
			return viewerColumn;
		}
		
		private class DefaultColumnLabelProvider extends ColumnLabelProvider {
			int colIdx;
			
			public DefaultColumnLabelProvider(int colIdx){
				this.colIdx = colIdx;
			}
			
			@Override
			public String getText(Object element){
				return ((String[]) element)[colIdx];
			}
		}
	}
	
	class InvoiceContentDiagnosisComposite extends Composite {
		
		TableViewer tableViewer;
		TableColumnLayout tableColumnLayout;
		
		public InvoiceContentDiagnosisComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 0;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		}
		
		private void createComponents(List<String[]> values){
			Composite tableArea = new Composite(this, SWT.NONE);
			tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			tableColumnLayout = new TableColumnLayout();
			tableArea.setLayout(tableColumnLayout);
			
			tableViewer = new TableViewer(tableArea,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
			Table table = tableViewer.getTable();
			table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			TableViewerColumn tcDiagnosisText =
				createTableViewerColumn("Behandlungsdiagnose", 1, 0);
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(values);
		}
		
		private TableViewerColumn createTableViewerColumn(String title, int bound, int colIdx){
			final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn column = viewerColumn.getColumn();
			column.setText(title);
			tableColumnLayout.setColumnData(column,
				new ColumnWeightData(bound, ColumnWeightData.MINIMUM_WIDTH, true));
			column.setResizable(true);
			column.setMoveable(false);
			viewerColumn.setLabelProvider(new DefaultColumnLabelProvider(colIdx));
			return viewerColumn;
		}
		
		private class DefaultColumnLabelProvider extends ColumnLabelProvider {
			int colIdx;
			
			public DefaultColumnLabelProvider(int colIdx){
				this.colIdx = colIdx;
			}
			
			@Override
			public String getText(Object element){
				return ((String[]) element)[colIdx];
			}
		}
	}
	
}
