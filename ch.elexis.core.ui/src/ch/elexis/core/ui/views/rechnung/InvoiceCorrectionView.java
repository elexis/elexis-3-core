package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailBlatt2;
import ch.elexis.data.Anwender;
import ch.elexis.data.Rechnung;
import ch.elexis.data.dto.FallDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO.DiagnosesDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO.KonsultationDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO.LeistungDTO;

public class InvoiceCorrectionView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.rechnung.InvoiceCorrectionView";
	private static Logger logger = LoggerFactory.getLogger(InvoiceCorrectionView.class);
	
	private InvoiceComposite invoiceComposite;
	
	private Rechnung actualInvoice;
	
	private final ElexisEventListenerImpl eeli_rn = new ElexisUiEventListenerImpl(Rechnung.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE
			| ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED) {
		
		public void runInUi(ElexisEvent ev){
			switch (ev.getType()) {
			case ElexisEvent.EVENT_UPDATE:
				reload((Rechnung) ev.getObject());
				break;
			case ElexisEvent.EVENT_DESELECTED: // fall thru
				reload(null);
				break;
			case ElexisEvent.EVENT_DELETE:
				reload(null);
				break;
			case ElexisEvent.EVENT_SELECTED:
				reload((Rechnung) ev.getObject());
				break;
			}
		}
	};
	
	private final ElexisEventListenerImpl eeli_user =
		new ElexisUiEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			
			public void runInUi(ElexisEvent ev){
				reload(actualInvoice);
			}
		};
	
	public void reload(Rechnung rechnung){
		if (invoiceComposite != null) {
			actualInvoice = rechnung;
			InvoiceCorrectionDTO invoiceCorrectionDTO = new InvoiceCorrectionDTO(actualInvoice);
			Composite parent = invoiceComposite.getParent();
			invoiceComposite.dispose();
			invoiceComposite = new InvoiceComposite(parent);
			invoiceComposite.createComponents(invoiceCorrectionDTO);
			parent.layout(true, true);
		}
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(1, false));
		invoiceComposite = new InvoiceComposite(parent);
		invoiceComposite.createComponents(new InvoiceCorrectionDTO());
		ElexisEventDispatcher.getInstance().addListeners(eeli_rn, eeli_user);
		Rechnung selected = (Rechnung) ElexisEventDispatcher.getSelected(Rechnung.class);
		if (selected != null) {
			reload(selected);
		}
	}
	

	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_rn, eeli_user);
		super.dispose();
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
		
		public void createComponents(InvoiceCorrectionDTO invoiceCorrectionDTO){
			wrapper = new Composite(this, SWT.NONE);
			wrapper.setLayout(new GridLayout(1, false));
			wrapper.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			InvoiceHeaderComposite invoiceHeaderComposite = new InvoiceHeaderComposite(wrapper);
			InvoiceContentComposite invoiceContentComposite = new InvoiceContentComposite(wrapper);
			InvoiceBottomComposite invoiceBottomComposite = new InvoiceBottomComposite(wrapper);
			
			invoiceHeaderComposite.createComponents(invoiceCorrectionDTO);
			if (invoiceCorrectionDTO != null && invoiceCorrectionDTO.getId() != null) {
				invoiceContentComposite.createComponents(invoiceCorrectionDTO);
				invoiceBottomComposite.createComponents();
			}
			
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
			"Rechnung", "Patient", "Telefon Versicherer", "Sachbearbeiter/in", "Rückweisungsgrund"
		};
		
		public InvoiceHeaderComposite(Composite parent){
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(4, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		}
		
		public void createComponents(InvoiceCorrectionDTO invoiceCorrectionDTO){
			
			Label lblTitle = new Label(this, SWT.NONE);
			lblTitle.setText("Rechnungsangaben");
			lblTitle.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
			lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 10, 1));
			Color colWhite = UiDesk.getColor(UiDesk.COL_WHITE);
			this.setBackground(colWhite);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			String[] invoiceDetails = invoiceCorrectionDTO.getInvoiceDetails();
			if (invoiceDetails.length == lbls.length) {
				int i = 0;
				for (String lbl : lbls) {
					String detailText = invoiceDetails[i++];
					new Label(this, SWT.NONE).setText(lbl);
					CLabel text = new CLabel(this, SWT.BORDER);
					text.setBackground(colWhite);
					text.setLayoutData(
						i != 5 ? gd : new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
					text.setText(detailText != null ? detailText : "");
				}
			} else {
				logger.error("cannot load invoice header data - values size mismatch [expected: "
					+ lbls.length + ", current: " + invoiceDetails.length + "]");
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
		
		public void createComponents(InvoiceCorrectionDTO invoiceCorrectionDTO){
			if (invoiceCorrectionDTO.getFallDTO() != null) {
				InvoiceContentHeaderComposite invoiceContentHeaderComposite =
					new InvoiceContentHeaderComposite(this);
				invoiceContentHeaderComposite.createComponents(invoiceCorrectionDTO.getFallDTO());
			}
			
			InvoiceContentMiddleComposite invoiceContentMiddleComposite =
				new InvoiceContentMiddleComposite(this);
			invoiceContentMiddleComposite.createComponents(invoiceCorrectionDTO);
		}
	}
	
	class InvoiceContentHeaderComposite extends Composite {
		public InvoiceContentHeaderComposite(Composite parent){
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(1, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		}
		
		public void createComponents(FallDTO fallDTO){
			this.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			Label lblTitle = new Label(this, SWT.NONE);
			lblTitle.setText("Fallangaben");
			lblTitle.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
			lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			
			FallDetailBlatt2 fallDetailBlatt2 = new FallDetailBlatt2(this, fallDTO, true);
			fallDetailBlatt2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			/*
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
			cbLaw.setSelection(new StructuredSelection(fallDTO.getAbrechnungsSystem()));
			
			Label lblAccidentDate = new Label(this, SWT.NONE);
			lblAccidentDate.setText("Unfall Datum");
			DateTime dateAccident = new DateTime(this, SWT.DATE | SWT.DROP_DOWN);
			TimeTool doa = new TimeTool(fallDTO.getBeginnDate());
			dateAccident.setDate(doa.get(Calendar.YEAR), doa.get(Calendar.MONTH),
				doa.get(Calendar.DAY_OF_MONTH));
			Label lblAccidentNr = new Label(this, SWT.NONE);
			lblAccidentNr.setText("Unfall Nummer");	
			CLabel txtAccidentNr = new CLabel(this, SWT.BORDER);
			txtAccidentNr.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			txtAccidentNr.setText(fallDTO.getNumber());
			txtAccidentNr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			
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
			cbRecipient.setSelection(new StructuredSelection(fallDTO.getReceiver()));
						
			Label lblGarant = new Label(this, SWT.NONE);
			lblGarant.setText("Kostenträger");
			
			CLabel text = new CLabel(this, SWT.BORDER);
			text.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			text.setText(fallDTO.getCostReceiver());
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			
			*/
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
		
		public void createComponents(InvoiceCorrectionDTO invoiceCorrectionDTO){
			for (KonsultationDTO konsultationDTO : invoiceCorrectionDTO.getKonsultationDTOs()) {
				Group group = new Group(this, SWT.BORDER);
				group.setText("Konsultation: " + konsultationDTO.getDate() + " - "
					+ konsultationDTO.getStateText());
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
				invoiceContentDiagnosisComposite.createComponents(konsultationDTO);
				invoiceContentKonsultationComposite.createComponents(konsultationDTO);
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
		
		private void createComponents(KonsultationDTO konsultationDTO){
			Composite tableArea = new Composite(this, SWT.NONE);
			tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			tableColumnLayout = new TableColumnLayout();
			tableArea.setLayout(tableColumnLayout);
			
			tableViewer = new TableViewer(tableArea, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
			Table table = tableViewer.getTable();
			table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			TableViewerColumn tcSize = createTableViewerColumn("Anzahl", 2, 0);
			TableViewerColumn tcServiceCode = createTableViewerColumn("Leistungscode", 4, 1);
			TableViewerColumn tcSericeText = createTableViewerColumn("Leistungstext", 12, 2);
			TableViewerColumn tcPrice = createTableViewerColumn("Preis", 3, 3);
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(konsultationDTO.getLeistungDTOs());
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
				LeistungDTO leistungDTO = (LeistungDTO) element;
				switch (colIdx) {
				case 0:
					return String.valueOf(leistungDTO.getCount());
				case 1:
					return leistungDTO.getCode();
				case 2:
					return leistungDTO.getText();
				case 3:
					return leistungDTO.getBruttoPreis().getAmountAsString();
				default:
					return "";
				}
				
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
		
		private void createComponents(KonsultationDTO konsultationDTO){
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
			tableViewer.setInput(konsultationDTO.getDiagnosesDTOs());
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
				DiagnosesDTO diagnosesDTO = (DiagnosesDTO) element;
				switch (colIdx) {
				case 0:
					return diagnosesDTO.getLabel();
				default:
					return "";
				}
			}
		}
	}
	
	class InvoiceBottomComposite extends Composite {
		public InvoiceBottomComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 0;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		
		public void createComponents(){
			Composite parent = new Composite(this, SWT.NONE);
			GridLayout gd = new GridLayout(3, false);
			gd.marginWidth = 0;
			gd.marginHeight = 2;
			parent.setLayout(gd);
			parent.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true, 1, 1));
			Button btnImmediateSent = new Button(parent, SWT.CHECK);
			btnImmediateSent.setText("sofort versenden");
			
			Button btnCorrection = new Button(parent, SWT.NONE);
			btnCorrection.setText("Korrigieren");
			
			Button btnCancel = new Button(parent, SWT.NONE);
			btnCancel.setText("Zurücksetzen");
		}
	}
	
}
