package ch.elexis.core.ui.views.rechnung;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailBlatt2;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.core.ui.views.rechnung.InvoiceCorrectionWizard.Page2;
import ch.elexis.data.Anwender;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.dto.DiagnosesDTO;
import ch.elexis.data.dto.FallDTO;
import ch.elexis.data.dto.FallDTO.IFallChanged;
import ch.elexis.data.dto.InvoiceCorrectionDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO.OperationType;
import ch.elexis.data.dto.KonsultationDTO;
import ch.elexis.data.dto.LeistungDTO;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class InvoiceCorrectionView extends ViewPart {
	
	public static final String ID = "ch.elexis.core.ui.views.rechnung.InvoiceCorrectionView";
	private static Logger logger = LoggerFactory.getLogger(InvoiceCorrectionView.class);
	
	private InvoiceComposite invoiceComposite;
	
	private Rechnung actualInvoice;
	private InvoiceCorrectionDTO invoiceCorrectionDTO;
	
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
		if (invoiceComposite != null && rechnung != null) {
			actualInvoice = Rechnung.load(rechnung.getId());
			invoiceCorrectionDTO = new InvoiceCorrectionDTO(actualInvoice);
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
		invoiceCorrectionDTO = new InvoiceCorrectionDTO();
		invoiceComposite.createComponents(invoiceCorrectionDTO);
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
			"Rechnung", "Status", "Patient", "Telefon Versicherer", "Sachbearbeiter/in",
			"Rückweisungsgrund", "Bemerkung"
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
					
					if (i == 6 || i == 7) {
						Text txtMulti = new Text(this,
							SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
						GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
						gd2.heightHint = 50;
						txtMulti.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
						txtMulti.setLayoutData(gd2);
						txtMulti.setText(detailText != null ? detailText : "");
					} else {
						
						CLabel text = new CLabel(this, SWT.BORDER);
						text.setBackground(colWhite);
						text.setLayoutData(
							i == 3 ? new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1) : gd);
						text.setText(detailText != null ? detailText : "");
					}
					
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
			
			FallDetailBlatt2 fallDetailBlatt2 = new FallDetailBlatt2(this, fallDTO, true,
				actualInvoice == null || !actualInvoice.isCorrectable());
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd.heightHint = 340;
			fallDetailBlatt2.setLayoutData(gd);
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
				Composite group = new Composite(this, SWT.BORDER);
				
				GridLayout gd = new GridLayout(2, false);
				gd.marginWidth = 0;
				gd.marginHeight = 0;
				group.setLayout(gd);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
				Label lblKonsTitle = new Label(group, SWT.NONE);
				lblKonsTitle.setFont(SWTResourceManager.getFont("Noto Sans", 9, SWT.BOLD));
				updateKonsTitleText(lblKonsTitle, konsultationDTO);
				
				ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
				tbManager.add(new Action("Datum ändern") {
					
					@Override
					public ImageDescriptor getImageDescriptor(){
						return Images.IMG_CALENDAR.getImageDescriptor();
					}
					
					@Override
					public String getToolTipText(){
						return "Datum ändern";
					}
					
					@Override
					public void run(){
						DateSelectorDialog dlg = new DateSelectorDialog(getShell());
						if (dlg.open() == Dialog.OK) {
							TimeTool date = dlg.getSelectedDate();
							konsultationDTO.setDate(date.toString(TimeTool.DATE_GER));
							invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
								OperationType.KONSULTATION_CHANGE_DATE, konsultationDTO, null));
							updateKonsTitleText(lblKonsTitle, konsultationDTO);
						}
					}
				});
				tbManager.add(new Action() {
					@Override
					public String getText(){
						return "Mandant ändern";
					}
					
					@Override
					public ImageDescriptor getImageDescriptor(){
						return Images.IMG_MANN.getImageDescriptor();
					}
					
					@Override
					public void run(){
						KontaktSelektor ksl = new KontaktSelektor(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							Mandant.class, "Mandant auswählen",
							"Auf wen soll diese Kons verrechnet werden?", new String[] {
								Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2
						});
						if (ksl.open() == Dialog.OK) {
							Mandant selectedMandant = (Mandant) ksl.getSelection();
							konsultationDTO.setMandant(selectedMandant);
							invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
								OperationType.KONSULTATION_CHANGE_MANDANT, konsultationDTO, null));
							updateKonsTitleText(lblKonsTitle, konsultationDTO);
						}
					}
				});
				tbManager.add(new Action() {
					@Override
					public String getText(){
						return "Fallzuordnung ändern";
					}
					
					@Override
					public ImageDescriptor getImageDescriptor(){
						return Images.IMG_DOC_SYS.getImageDescriptor();
					}
					
					@Override
					public void run(){
					
					}
				});
					
				ToolBar toolbar = tbManager.createControl(group);
				// align toolbar right
				GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false)
					.applyTo(toolbar);
				InvoiceContentDiagnosisComposite invoiceContentDiagnosisComposite =
					new InvoiceContentDiagnosisComposite(group);
				InvoiceContentKonsultationComposite invoiceContentKonsultationComposite =
					new InvoiceContentKonsultationComposite(group);
				
				invoiceContentDiagnosisComposite.createComponents(konsultationDTO);
				invoiceContentKonsultationComposite.createComponents(konsultationDTO);
			}
			
		}
		
		public void updateKonsTitleText(Label lblKonsTitle, KonsultationDTO konsultationDTO){
			lblKonsTitle.setText("Konsultation: " + konsultationDTO.getDate() + " Mandant: "
				+ konsultationDTO.getMandant().getLabel());
			lblKonsTitle.getParent().layout();
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
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
		}
		
		private void createComponents(KonsultationDTO konsultationDTO){
			Composite tableArea = new Composite(this, SWT.NONE);
			tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tableColumnLayout = new TableColumnLayout();
			tableArea.setLayout(tableColumnLayout);
			
			tableViewer = new TableViewer(tableArea, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
			Table table = tableViewer.getTable();
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			TableViewerColumn tcSize = createTableViewerColumn("Anzahl", 1, 0);
			TableViewerColumn tcServiceCode = createTableViewerColumn("Leistungscode", 4, 1);
			TableViewerColumn tcSericeText = createTableViewerColumn("Leistungstext", 12, 2);
			TableViewerColumn tcPrice = createTableViewerColumn("Preis", 3, 3);
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(konsultationDTO.getLeistungDTOs());
			tableViewer.setComparator(new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2){
					return ObjectUtils.compare(((LeistungDTO) e1).getLastUpdate(),
						((LeistungDTO) e2).getLastUpdate());
				}
			});
			
			invoiceCorrectionDTO.getFallDTO().register(new IFallChanged() {
				
				@Override
				public void changed(FallDTO fallDTO){
					for (KonsultationDTO konsultationDTO : invoiceCorrectionDTO
						.getKonsultationDTOs()) {
						for (LeistungDTO leistungDTO : konsultationDTO.getLeistungDTOs()) {
							leistungDTO.calcPrice(konsultationDTO, fallDTO);
						}
					}
					tableViewer.refresh();
				}
				
			});
			
			PersistentObjectDropTarget.IReceiver dtr = new PersistentObjectDropTarget.IReceiver() {
				
				public boolean accept(PersistentObject o){
					return true;
				}
				
				public void dropped(PersistentObject o, DropTargetEvent ev){
					if (o instanceof IVerrechenbar) {
						IVerrechenbar art = (IVerrechenbar) o;
						LeistungDTO leistungDTO = new LeistungDTO(art);
						konsultationDTO.getLeistungDTOs().add(leistungDTO);
						leistungDTO.calcPrice(konsultationDTO, invoiceCorrectionDTO.getFallDTO());
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.LEISTUNG_ADD, konsultationDTO, leistungDTO));
						tableViewer.refresh();
					}
				}
			};
			PersistentObjectDropTarget dropTarget =
				new PersistentObjectDropTarget("rechnungskorrektur", this, dtr); //$NON-NLS-1$
			
			MenuManager menuManager = new MenuManager();
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Anzahl ändern";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					LeistungDTO leistungDTO = getSelection();
					if (leistungDTO != null && changeQuantityDialog(leistungDTO)) {
						leistungDTO.calcPrice(konsultationDTO, invoiceCorrectionDTO.getFallDTO());
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.LEISTUNG_CHANGE_COUNT, konsultationDTO, leistungDTO));
						tableViewer.refresh();
						
					}
				}
			});
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Preis ändern";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					LeistungDTO leistungDTO = getSelection();
					if (leistungDTO != null && changePriceDialog(leistungDTO)) {
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.LEISTUNG_CHANGE_PRICE, konsultationDTO, leistungDTO));
						tableViewer.refresh();
					}
				}
			});
			menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Leistung hinzufügen";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					
					try {
						LeistungenView iViewPart =
							(LeistungenView) getSite().getPage().showView(LeistungenView.ID);
						CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
						CTabItem[] tabItems = iViewPart.ctab.getItems();
						for (CTabItem tab : tabItems) {
							ICodeElement ics = (ICodeElement) tab.getData();
							if (ics instanceof Artikel) {
								iViewPart.ctab.setSelection(tab);
								break;
							}
						}
						iViewPart.setFocus();
					} catch (PartInitException e) {
						LoggerFactory.getLogger(InvoiceCorrectionDTO.class)
							.error("cannot init leistungen viewpart", e);
					}
				}
			});
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Leistung entfernen";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					LeistungDTO leistungDTO = getSelection();
					if (leistungDTO != null) {
						konsultationDTO.getLeistungDTOs().remove(leistungDTO);
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.LEISTUNG_REMOVE, konsultationDTO, leistungDTO));
						tableViewer.refresh();
					}
				}
			});
			
			tableViewer.getTable().setMenu(menuManager.createContextMenu(tableViewer.getTable()));
			
		}
		
		public LeistungDTO getSelection(){
			if (tableViewer != null) {
				StructuredSelection structuredSelection =
					(StructuredSelection) tableViewer.getSelection();
				if (!structuredSelection.isEmpty()) {
					return (LeistungDTO) structuredSelection.getFirstElement();
				}
			}
			return null;
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
					return leistungDTO.getPrice() != null
							? leistungDTO.getPrice().getAmountAsString() : "0";
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
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		}
		
		private void createComponents(KonsultationDTO konsultationDTO){
			Composite tableArea = new Composite(this, SWT.NONE);
			tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
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
			
			Button btnCorrection = new Button(parent, SWT.NONE);
			btnCorrection.setText("Validieren");
			btnCorrection.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					Result<String> res = doBillCorrection(actualInvoice);
					if (res != null) {
						if (SEVERITY.ERROR.equals(res.getSeverity())) {
							MessageDialog.openError(Display.getDefault().getActiveShell(),
								"Rechnungskorrektur", res.get());
						}
						reload(actualInvoice);
					}
				}
			});
			
			Button btnCancel = new Button(parent, SWT.NONE);
			btnCancel.setText("Zurücksetzen");
			
			btnCancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					reload(actualInvoice);
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
						"Rechnungskorrektur", "Die Rechnung wurde erfolgreich zurückgesetzt.");
				}
			});
			
			this.setVisible(actualInvoice != null && actualInvoice.isCorrectable());
		}
	}
	
	/**
	 * Copies the actual fall, merge the copied fall with changes, transfer cons, storno the old
	 * invoice
	 */
	private Result<String> doBillCorrection(Rechnung actualInvoice){
		
		if (actualInvoice != null && actualInvoice.isCorrectable()) {
			if (actualInvoice.getFall() != null && invoiceCorrectionDTO != null
				&& invoiceCorrectionDTO.getFallDTO() != null) {
				try {
					invoiceCorrectionDTO.updateHistory();
					
					InvoiceCorrectionWizardDialog wizardDialog = new InvoiceCorrectionWizardDialog(
						getSite().getShell(), invoiceCorrectionDTO);
					wizardDialog.addPageChangedListener(new IPageChangedListener() {
						
						@Override
						public void pageChanged(PageChangedEvent event){
							
							if (event.getSelectedPage() instanceof Page2) {
								Page2 page = (Page2) event.getSelectedPage();
								InvoiceCorrectionDTO invoiceCorrectionDTO =
									page.getInvoiceCorrectionDTO();
								
								// batch change history
								boolean terminated = false;
								StringBuilder output = new StringBuilder();
								Rechnung rechnung = Rechnung.load(invoiceCorrectionDTO.getId());
								Optional<Fall> srcFall = Optional.empty();
								Optional<Fall> copyFall = Optional.empty();
								List<Konsultation> releasedKonsultations = new ArrayList<>();
								LeistungDTO leistungDTO = null;
								Konsultation konsultation = null;
								Verrechnet verrechnet = null;
								try {
									
									for (InvoiceHistoryEntryDTO historyEntryDTO : invoiceCorrectionDTO
										.getHistory()) {
										
										Object base = historyEntryDTO.getBase();
										Object item = historyEntryDTO.getItem();
										OperationType operationType =
											historyEntryDTO.getOperationType();
										
										// storno
										switch (operationType) {
										case RECHNUNG_STORNO:
											releasedKonsultations.addAll(rechnung.stornoBill(true));
											break;
										case RECHNUNG_NEW:
											Result<Rechnung> rechnungResult =
												Rechnung.build(releasedKonsultations);
											if (!rechnungResult.isOK()) {
												
												for (msg message : rechnungResult.getMessages()) {
													if (message.getSeverity() != SEVERITY.OK) {
														if (output.length() > 0) {
															output.append("\n");
														}
														output.append(message.getText());
													}
												}
												terminated = true;
											} else {
												output.append("Die Rechnung " + rechnung.getNr()
													+ " wurde erfolgreich korrigiert - Neue Rechnungsnummer lautet: "
													+ rechnungResult.get().getNr());
											}
											break;
										case FALL_COPY:
											srcFall = Optional.of(rechnung.getFall());
											copyFall = Optional.of(srcFall.get().createCopy());
											break;
										case FALL_CHANGE:
											copyFall.get()
												.persistDTO(invoiceCorrectionDTO.getFallDTO());
											break;
										case FALL_KONSULTATION_TRANSER:
											releasedKonsultations.clear();
											Konsultation[] consultations =
												srcFall.get().getBehandlungen(true);
											if (consultations != null) {
												for (Konsultation openedKons : consultations) {
													if (openedKons.exists()) {
														Rechnung bill = openedKons.getRechnung();
														if (bill == null) {
															openedKons
																.transferToFall(copyFall.get());
															releasedKonsultations.add(openedKons);
															
															// if validation of cons is failed the bill correction will be reseted
															Result<?> result = BillingUtil
																.getBillableResult(openedKons);
															if (!result.isOK()) {
																addToOutput(output, result);
																terminated = true;
																resetCorrection(srcFall.get(),
																	copyFall.get(),
																	releasedKonsultations);
															}
														}
													}
												}
											}
											break;
										case KONSULTATION_CHANGE_DATE:
											Konsultation.load(((KonsultationDTO) base).getId())
												.setDatum(((KonsultationDTO) base).getDate(), true);
											break;
										case KONSULTATION_CHANGE_MANDANT:
											Konsultation.load(((KonsultationDTO) base).getId())
												.setMandant(((KonsultationDTO) base).getMandant());
											
											break;
										case LEISTUNG_ADD:
											konsultation =
												Konsultation.load(((KonsultationDTO) base).getId());
											leistungDTO = (LeistungDTO) item;
											Result<IVerrechenbar> res = konsultation
												.addLeistung(leistungDTO.getIVerrechenbar());
											if (res.isOK()) {
												verrechnet = konsultation
													.getVerrechnet(leistungDTO.getIVerrechenbar());
												if (verrechnet != null) {
													leistungDTO.setVerrechnet(verrechnet);
												}
											} else {
												addToOutput(output, res);
												verrechnet = null;
											}
											if (verrechnet == null) {
												addToOutput(output,
													"Die Leistung "
														+ leistungDTO.getIVerrechenbar().getText()
														+ " konnte nicht verrechnet werden.");
												terminated = true;
											}
											
											break;
										case LEISTUNG_REMOVE:
											leistungDTO = (LeistungDTO) item;
											if (leistungDTO.getVerrechnet() != null) {
												Result<Verrechnet> resRemove = Konsultation
													.load(((KonsultationDTO) base).getId())
													.removeLeistung(leistungDTO.getVerrechnet());
												
												if (resRemove.isOK()) {
													((LeistungDTO) item).setVerrechnet(null);
												} else {
													addToOutput(output,
														"Die Leistung "
															+ leistungDTO.getVerrechnet().getText()
															+ " konnte nicht entfernt werden.");
													terminated = true;
												}
											}
											break;
										case LEISTUNG_CHANGE_COUNT:
											leistungDTO = (LeistungDTO) item;
											verrechnet = leistungDTO.getVerrechnet();
											if (verrechnet != null) {
												IStatus ret = verrechnet
													.changeAnzahlValidated(leistungDTO.getCount());
												if (ret.isOK()) {
													verrechnet.setSecondaryScaleFactor(
														leistungDTO.getPriceSecondaryScaleFactor());
													verrechnet.setText(leistungDTO.getPriceText());
												} else {
													addToOutput(output, ret.getMessage());
													terminated = true;
												}
											}
											break;
										case LEISTUNG_CHANGE_PRICE:
											leistungDTO = (LeistungDTO) item;
											verrechnet = leistungDTO.getVerrechnet();
											if (verrechnet != null) {
												verrechnet.setTP(leistungDTO.getPrice().getCents());
												verrechnet.setSecondaryScaleFactor(
													leistungDTO.getPriceSecondaryScaleFactor());
											}
											break;
										default:
											break;
										}
										
										page.getTxtOutput().setText(output.toString());
										invoiceCorrectionDTO.setOutputText(output.toString());
										if (terminated) {
											break;
										} else {
											historyEntryDTO.setSuccess(true);
											page.setChecked(historyEntryDTO, true);
										}
									}
									
								} catch (Exception e) {
									e.printStackTrace();
								}
								
							}
						}
						
						private void addToOutput(StringBuilder output, Result<?> res){
							StringBuilder warnings = new StringBuilder();
							for (msg message : res.getMessages()) {
								if (message.getSeverity() != SEVERITY.OK) {
									if (output.length() > 0) {
										warnings.append(" / ");
									}
									warnings.append(message.getText());
								}
							}
							if (warnings.length() > 0) {
								output.append(warnings.toString());
							}
						}
						
						private void addToOutput(StringBuilder output, String warning){
							if (output.length() > 0) {
								output.append("\n");
							}
							if (warning.length() > 0) {
								output.append(warning);
							}
						}
					});
					
					int state = wizardDialog.open();
					if (invoiceCorrectionDTO.getOutputText() != null) {
						// set bemerkung text
						StringBuilder txtBemerkung = new StringBuilder();
						if (txtBemerkung != null) {
							txtBemerkung.append(actualInvoice.getBemerkung());
						}
						if (txtBemerkung.length() > 0) {
							txtBemerkung.append("\n");
						}
						txtBemerkung.append(invoiceCorrectionDTO.getOutputText());
						actualInvoice.setBemerkung(txtBemerkung.toString());
						
						// return state
						if (invoiceCorrectionDTO.isCorrectionSuccess()) {
							return new Result<String>("ok");
						}
						return new Result<String>(SEVERITY.WARNING, 2, "warn", null, false);
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(InvoiceCorrectionView.class)
						.error("invoice correction error [{}]", actualInvoice.getId(), e);
					
					return new Result<String>(SEVERITY.ERROR, 2, "error",
						"Die Rechnungskorrektur konnte nicht vollständig durchgeführt werden.\nFür mehr Details, beachten Sie bitte das Log-File.",
						false);
				}
			}
		}
		return null;
	}
	
	private void resetCorrection(Fall srcFall, Fall copyFall,
		List<Konsultation> transferedConsultations){
		// reset
		for (Konsultation k : transferedConsultations) {
			k.transferToFall(srcFall);
		}
		copyFall.delete();
	}
	
	private boolean changePriceDialog(LeistungDTO leistungDTO){
		Money oldPrice = leistungDTO.getPrice();
		String p = oldPrice.getAmountAsString();
		InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
			Messages.VerrechnungsDisplay_changePriceForService, //$NON-NLS-1$
			Messages.VerrechnungsDisplay_enterNewPrice, p, //$NON-NLS-1$
			null);
		if (dlg.open() == Dialog.OK) {
			try {
				String val = dlg.getValue().trim();
				Money newPrice = new Money(oldPrice);
				if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
					val = val.substring(0, val.length() - 1);
					double percent = Double.parseDouble(val);
					double factor = 1.0 + (percent / 100.0);
					leistungDTO.setPriceSecondaryScaleFactor(factor);
					leistungDTO.setCustomPrice(leistungDTO.getPrice());
				} else {
					newPrice = new Money(val);
					leistungDTO.setCustomPrice(newPrice);
					leistungDTO.setPriceSecondaryScaleFactor(Double.valueOf(1));
				}
				return true;
			} catch (ParseException ex) {
				SWTHelper.showError(Messages.VerrechnungsDisplay_badAmountCaption, //$NON-NLS-1$
					Messages.VerrechnungsDisplay_badAmountBody); //$NON-NLS-1$
			}
		}
		return false;
	}
	
	private boolean changeQuantityDialog(LeistungDTO leistungDTO){
		String p = Integer.toString(leistungDTO.getCount());
		InputDialog dlg =
			new InputDialog(UiDesk.getTopShell(), Messages.VerrechnungsDisplay_changeNumberCaption, //$NON-NLS-1$
				Messages.VerrechnungsDisplay_changeNumberBody, //$NON-NLS-1$
				p, null);
		if (dlg.open() == Dialog.OK) {
			try {
				String val = dlg.getValue();
				if (!StringTool.isNothing(val)) {
					int changeAnzahl;
					double secondaryScaleFactor = 1.0;
					String text = leistungDTO.getIVerrechenbar().getText();
					
					if (val.indexOf(StringConstants.SLASH) > 0) {
						changeAnzahl = 1;
						String[] frac = val.split(StringConstants.SLASH);
						secondaryScaleFactor =
							Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
						text = leistungDTO.getIVerrechenbar().getText() + " (" + val //$NON-NLS-1$
							+ Messages.VerrechnungsDisplay_Orininalpackungen;
					} else if (val.indexOf('.') > 0) {
						changeAnzahl = 1;
						secondaryScaleFactor = Double.parseDouble(val);
						text = leistungDTO.getIVerrechenbar().getText() + " ("
							+ Double.toString(secondaryScaleFactor) + ")";
					} else {
						changeAnzahl = Integer.parseInt(dlg.getValue());
					}
					
					leistungDTO.setCount(changeAnzahl);
					leistungDTO.setPriceSecondaryScaleFactor(secondaryScaleFactor);
					leistungDTO.setPriceText(text);
					return true;
				}
			} catch (NumberFormatException ne) {
				SWTHelper.showError(Messages.VerrechnungsDisplay_invalidEntryCaption, //$NON-NLS-1$
					Messages.VerrechnungsDisplay_invalidEntryBody); //$NON-NLS-1$
			}
		}
		
		return false;
	}
	
}
