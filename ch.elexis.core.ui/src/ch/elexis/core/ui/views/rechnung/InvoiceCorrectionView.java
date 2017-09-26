package ch.elexis.core.ui.views.rechnung;

import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_CLASS;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_VIEWID;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.BillingUtil.BillCallback;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.FallSelectionDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.FallDetailBlatt2;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.codesystems.DiagnosenView;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.core.ui.views.contribution.ViewContributionHelper;
import ch.elexis.core.ui.views.rechnung.InvoiceCorrectionWizard.Page2;
import ch.elexis.data.Anwender;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.dto.DiagnosesDTO;
import ch.elexis.data.dto.FallDTO;
import ch.elexis.data.dto.FallDTO.IFallChanged;
import ch.elexis.data.dto.InvoiceCorrectionDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO.IInvoiceCorrectionChanged;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO.OperationType;
import ch.elexis.data.dto.KonsultationDTO;
import ch.elexis.data.dto.LeistungDTO;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class InvoiceCorrectionView extends ViewPart {
	
	public static final String ID = "ch.elexis.core.ui.views.rechnung.InvoiceCorrectionView";
	private InvoiceComposite invoiceComposite;
	
	private Rechnung actualInvoice;
	private InvoiceCorrectionDTO invoiceCorrectionDTO = new InvoiceCorrectionDTO();
	
	private InvoiceBottomComposite invoiceBottomComposite;
	
	private static final Logger log = LoggerFactory.getLogger(InvoiceCorrectionView.class);
	
	@SuppressWarnings("unchecked")
	private final List<IViewContribution> detailComposites = Extensions.getClasses(VIEWCONTRIBUTION,
		VIEWCONTRIBUTION_CLASS, VIEWCONTRIBUTION_VIEWID, RnDetailView.ID);
	
	private final ElexisEventListenerImpl eeli_rn = new ElexisUiEventListenerImpl(Rechnung.class,
		ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE | ElexisEvent.EVENT_SELECTED) {
		
		public void runInUi(ElexisEvent ev){
			switch (ev.getType()) {
			case ElexisEvent.EVENT_UPDATE:
				reloadSameInvoice((Rechnung) ev.getObject());
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
	
	private void reloadSameInvoice(Rechnung invoiceToReload){
		if (actualInvoice != null && invoiceToReload != null
			&& StringUtils.equals(actualInvoice.getId(), invoiceToReload.getId())) {
			reload(invoiceToReload);
		}
	}
	
	private void reload(Rechnung rechnung){
		if (invoiceComposite != null) {
			if (rechnung != null && rechnung.exists()) {
				actualInvoice = Rechnung.load(rechnung.getId());
				invoiceCorrectionDTO = new InvoiceCorrectionDTO(actualInvoice);
			} else if (actualInvoice != null && actualInvoice.exists()) {
				actualInvoice = Rechnung.load(actualInvoice.getId());
				invoiceCorrectionDTO = new InvoiceCorrectionDTO(actualInvoice);
			} else {
				actualInvoice = null;
				invoiceCorrectionDTO = new InvoiceCorrectionDTO();
			}
			Composite parent = invoiceComposite.getParent();
			invoiceComposite.dispose();
			invoiceComposite = new InvoiceComposite(parent);
			invoiceComposite.createComponents(invoiceCorrectionDTO);
			parent.layout(true, true);
			
			if (invoiceCorrectionDTO.getInvoiceNumber() != null
				&& !invoiceCorrectionDTO.getErrors().isEmpty()) {
				StringBuilder builder = new StringBuilder();
				for (ElexisException e : invoiceCorrectionDTO.getErrors()) {
					builder.append("\n" + e.getMessage());
				}
				MessageDialog.openWarning(getSite().getShell(), "Rechnungskorrektur",
					"Die Rechnung " + invoiceCorrectionDTO.getInvoiceNumber()
						+ " konnte nicht vollständig geladen werden.\n\nDetails: "
						+ builder.toString());
			}
		}
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(1, false));
		invoiceComposite = new InvoiceComposite(parent);
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
		if (invoiceComposite != null) {
			invoiceComposite.updateScrollBars();
		}
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
			invoiceBottomComposite = new InvoiceBottomComposite(wrapper);
			
			invoiceHeaderComposite.createComponents(invoiceCorrectionDTO);
			if (invoiceCorrectionDTO != null) {
				if (invoiceCorrectionDTO.getId() != null) {
					invoiceContentComposite.createComponents(invoiceCorrectionDTO);
					invoiceBottomComposite.createComponents();
				}
				invoiceCorrectionDTO.register(new IInvoiceCorrectionChanged() {
					@Override
					public void changed(InvoiceCorrectionDTO invoiceCorrectionDTO){
						if (invoiceBottomComposite != null) {
							invoiceBottomComposite.refresh(true);
						}
					}
				});
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
			"Rechnung", "Status", "Patient", "Rechnungsbetrag"
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
					Text text = new Text(this, SWT.BORDER | SWT.READ_ONLY);
					text.setBackground(colWhite);
					text.setLayoutData(gd);
					text.setText(detailText != null ? detailText : "");
				}
			}
			new Label(this, SWT.NONE).setText("Bemerkung");
			Text txtMulti =
				new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
			GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
			gd2.heightHint = 50;
			txtMulti.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			txtMulti.setLayoutData(gd2);
			txtMulti.setText(invoiceCorrectionDTO.getBemerkung() != null
					? invoiceCorrectionDTO.getBemerkung() : "");
			
			if (invoiceCorrectionDTO.getNewInvoiceNumber() != null) {
				if (invoiceCorrectionDTO.getNewInvoiceNumber().isEmpty()) {
					//TODO show a text how to handle if an invoice cannot be corrected
				} else {
					new Label(this, SWT.NONE).setText("Korrigierte Rechnung");
					Link btnNewInvoice = new Link(this, SWT.NONE);
					btnNewInvoice.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
					btnNewInvoice.setText("<A>Rechnung "
						+ invoiceCorrectionDTO.getNewInvoiceNumber() + " öffnen</A>");
					btnNewInvoice.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e){
							Rechnung r =
								Rechnung.getFromNr(invoiceCorrectionDTO.getNewInvoiceNumber());
							if (r != null) {
								reload(r);
							} else {
								MessageDialog.openError(getShell(), "Fehler",
									"Die Rechnung mit der Nummer: "
										+ invoiceCorrectionDTO.getNewInvoiceNumber()
										+ " konnte nicht geöffnet werden.\nBitte versuchen Sie diesn manuell zu öffnen.");
							}
						}
					});
				}
			}
			
			if (actualInvoice != null && !detailComposites.isEmpty()) {
				Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
				separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 10, 1));
				List<IViewContribution> filtered = ViewContributionHelper
					.getFilteredAndPositionSortedContributions(detailComposites, 0);
				for (IViewContribution ivc : filtered) {
					new Label(this, SWT.NONE).setText(ivc.getLocalizedTitle());
					
					Composite mainComposite = new Composite(this, SWT.NONE);
					mainComposite.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
					mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
					mainComposite.setLayout(new GridLayout(1, false));
					ivc.initComposite(mainComposite);
				}
				
				detailComposites.forEach(dc -> dc.setDetailObject(actualInvoice, null));
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
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
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
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 0;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		}
		
		public void createComponents(FallDTO fallDTO){
			this.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			FormToolkit tk = UiDesk.getToolkit();
			ScrolledForm form = tk.createScrolledForm(this);
			form.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			Composite body = form.getBody();
			GridLayout gd1 = new GridLayout();
			gd1.marginWidth = 0;
			gd1.marginHeight = 0;
			body.setLayout(gd1);
			ExpandableComposite expandable = WidgetFactory.createExpandableComposite(tk, form, ""); //$NON-NLS-1$
			expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			expandable.setExpanded(false);
			expandable.setText("Fallangaben");
			expandable.addExpansionListener(new ExpansionAdapter() {
				
				@Override
				public void expansionStateChanged(ExpansionEvent e){
					invoiceComposite.updateScrollBars();
				}
			});
			Composite group = tk.createComposite(expandable, SWT.NONE);
			GridLayout gd = new GridLayout(2, false);
			gd.marginWidth = 0;
			gd.marginHeight = 0;
			group.setLayout(gd);
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			expandable.setClient(group);
			
			FallDetailBlatt2 fallDetailBlatt2 = new FallDetailBlatt2(group, fallDTO, true,
				actualInvoice == null || !actualInvoice.isCorrectable());
			GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			gd2.heightHint = 340;
			fallDetailBlatt2.setLayoutData(gd2);
		}
	}
	
	class InvoiceContentMiddleComposite extends Composite {
		
		public InvoiceContentMiddleComposite(Composite parent){
			super(parent, SWT.NONE);
			GridLayout gd = new GridLayout(1, false);
			gd.marginWidth = 0;
			gd.marginHeight = 5;
			setLayout(gd);
			setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		}
		
		public void createComponents(InvoiceCorrectionDTO invoiceCorrectionDTO){
			
			FormToolkit tk = UiDesk.getToolkit();
			for (KonsultationDTO konsultationDTO : invoiceCorrectionDTO.getKonsultationDTOs()) {
				ScrolledForm form = tk.createScrolledForm(this);
				form.setBackground(UiDesk.getColor(UiDesk.COL_LIGHTGREY));
				form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				Composite body = form.getBody();
				GridLayout gd1 = new GridLayout();
				gd1.marginWidth = 1;
				gd1.marginHeight = 1;
				body.setLayout(gd1);
				ExpandableComposite expandable =
					WidgetFactory.createExpandableComposite(tk, form, ""); //$NON-NLS-1$
				expandable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				expandable.setExpanded(false);
				expandable.addExpansionListener(new ExpansionAdapter() {
					
					@Override
					public void expansionStateChanged(ExpansionEvent e){
						
						invoiceComposite.updateScrollBars();
						
						if ((boolean) e.data) {
							Konsultation originKons = Konsultation.load(konsultationDTO.getId());
							ElexisEventDispatcher.fireSelectionEvent(originKons);
						} else {
							ElexisEventDispatcher.clearSelection(Konsultation.class);
						}
					}
				});
				Composite group = tk.createComposite(expandable, SWT.NONE);
				
				GridLayout gd = new GridLayout(2, false);
				gd.marginWidth = 0;
				gd.marginHeight = 0;
				group.setLayout(gd);
				group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				expandable.setClient(group);
				updateKonsTitleText(expandable, konsultationDTO);
				
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
						DateSelectorDialog dlg = new DateSelectorDialog(getShell(),
							new TimeTool(konsultationDTO.getDate()));
						if (dlg.open() == Dialog.OK) {
							TimeTool date = dlg.getSelectedDate();
							String newDate = date.toString(TimeTool.DATE_GER);
							if (!StringUtils.equals(newDate, konsultationDTO.getDate())) {
								konsultationDTO.setDate(newDate);
								invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
									OperationType.KONSULTATION_CHANGE_DATE, konsultationDTO, null));
								updateKonsTitleText(expandable, konsultationDTO);
							}
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
							Mandant srcMandant = konsultationDTO.getMandant();
							Mandant selectedMandant = (Mandant) ksl.getSelection();
							if (selectedMandant != null) {
								Rechnungssteller dstRechnungsteller =
									selectedMandant.getRechnungssteller();
								if (srcMandant != null) {
									Rechnungssteller srcRechnungsteller =
										srcMandant.getRechnungssteller();
									if (!StringUtils.equals(srcRechnungsteller.getId(),
										dstRechnungsteller.getId())) {
										
										if (!MessageDialog.openQuestion(getShell(),
											"Rechnungskorrektur",
											"Der Rechnungsteller des ausgewählten Mandants ist "
												+ dstRechnungsteller.getLabel()
												+ ".\nDieser unterscheidet sich zu dem bisherigen Rechnungsteller "
												+ srcRechnungsteller.getLabel()
												+ ".\n\nWollen Sie trotzdem den Mandanten "
												+ selectedMandant.getLabel() + " auswählen ?")) {
											return;
											
										}
										
									}
								}
								konsultationDTO.setMandant(selectedMandant);
								invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
									OperationType.KONSULTATION_CHANGE_MANDANT, konsultationDTO,
									null));
								updateKonsTitleText(expandable, konsultationDTO);
							} else {
								MessageDialog.openWarning(getShell(), "Rechnungskorrektur",
									"Mandantenauswahl fehlerhaft. Der Mandant konnte nicht geändert werden.");
							}
						}
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
		
		public void updateKonsTitleText(ExpandableComposite expandableComposite,
			KonsultationDTO konsultationDTO){
			expandableComposite.setText("Konsultation: " + konsultationDTO.getDate() + " Mandant: "
				+ konsultationDTO.getMandant().getLabel());
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
				public void changed(FallDTO fallDTO, boolean triggersRecalc){
					if (triggersRecalc) {
						for (KonsultationDTO konsultationDTO : invoiceCorrectionDTO
							.getKonsultationDTOs()) {
							for (LeistungDTO leistungDTO : konsultationDTO.getLeistungDTOs()) {
								leistungDTO.calcPrice(konsultationDTO, fallDTO);
							}
						}
					}
					if (invoiceBottomComposite != null) {
						invoiceBottomComposite.refresh(true);
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
						LeistungDTO leistungDTO =
							new LeistungDTO(art, invoiceCorrectionDTO.getFallDTO());
						konsultationDTO.getLeistungDTOs().add(leistungDTO);
						leistungDTO.calcPrice(konsultationDTO, invoiceCorrectionDTO.getFallDTO());
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.LEISTUNG_ADD, konsultationDTO, leistungDTO));
						tableViewer.refresh();
						invoiceComposite.updateScrollBars();
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
					return "Leistung auf neuen Fall/Kons transferieren";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					LeistungDTO leistungDTO = getSelection();
					if (leistungDTO != null) {
						FallSelectionDialog fallSelectionDialog =
							new FallSelectionDialog(getShell(),
								"Bitte wählen Sie einen Fall aus, auf das die u.a. Leistung transferiert werden soll.\n"
									+ leistungDTO.getText(),
								invoiceCorrectionDTO.getFallDTO());
						if (fallSelectionDialog.open() == MessageDialog.OK) {
							if (fallSelectionDialog.getSelectedFall().isPresent()) {
								konsultationDTO.getLeistungDTOs().remove(leistungDTO);
								IFall iFall = fallSelectionDialog.getSelectedFall().get();
								InvoiceHistoryEntryDTO existingEntry = invoiceCorrectionDTO
									.getHistoryEntryForLeistungTransferFromCache(iFall);
								
								if (existingEntry != null
									&& existingEntry.getItem() instanceof List<?>) {
									List<LeistungDTO> leistungen =
										(List<LeistungDTO>) existingEntry.getItem();
									leistungen.add(leistungDTO);
								} else {
									List<LeistungDTO> leistungen = new ArrayList<>();
									leistungen.add(leistungDTO);
									invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
										OperationType.LEISTUNG_TRANSFER_TO_FALL_KONS,
										konsultationDTO, leistungen, iFall));
									
								}
								tableViewer.refresh();
								invoiceComposite.updateScrollBars();
							}
							
						}
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
						invoiceComposite.updateScrollBars();
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
			
			tableViewer = new TableViewer(tableArea, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);
			Table table = tableViewer.getTable();
			table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			TableViewerColumn tcDiagnosisText =
				createTableViewerColumn("Behandlungsdiagnose", 1, 0);
			
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			PersistentObjectDropTarget.IReceiver dtr = new PersistentObjectDropTarget.IReceiver() {
				
				public boolean accept(PersistentObject o){
					return true;
				}
				
				public void dropped(PersistentObject o, DropTargetEvent ev){
					if (o instanceof IDiagnose) {
						IDiagnose art = (IDiagnose) o;
						DiagnosesDTO dto = new DiagnosesDTO(art);
						konsultationDTO.getDiagnosesDTOs().add(dto);
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.DIAGNOSE_ADD, konsultationDTO, dto));
						tableViewer.refresh();
						invoiceComposite.updateScrollBars();
					}
				}
			};
			PersistentObjectDropTarget dropTarget =
				new PersistentObjectDropTarget("rechnungskorrekturBehandlungen", this, dtr); //$NON-NLS-1$
			
			MenuManager menuManager = new MenuManager();
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Diagnose hinzufügen";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					
					try {
						DiagnosenView iViewPart =
							(DiagnosenView) getSite().getPage().showView(DiagnosenView.ID);
						CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
					} catch (PartInitException e) {
						LoggerFactory.getLogger(InvoiceCorrectionDTO.class)
							.error("cannot init diagnose viewpart", e);
					}
				}
			});
			menuManager.add(new Action() {
				@Override
				public String getText(){
					return "Diagnose entfernen";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return null;
				}
				
				@Override
				public void run(){
					DiagnosesDTO dto = getSelection();
					if (dto != null) {
						konsultationDTO.getDiagnosesDTOs().remove(dto);
						invoiceCorrectionDTO.addToCache(new InvoiceHistoryEntryDTO(
							OperationType.DIAGNOSE_REMOVE, konsultationDTO, dto));
						tableViewer.refresh();
						invoiceComposite.updateScrollBars();
					}
				}
			});
			
			tableViewer.getTable().setMenu(menuManager.createContextMenu(tableViewer.getTable()));
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setInput(konsultationDTO.getDiagnosesDTOs());
		}
		
		public DiagnosesDTO getSelection(){
			if (tableViewer != null) {
				StructuredSelection structuredSelection =
					(StructuredSelection) tableViewer.getSelection();
				if (!structuredSelection.isEmpty()) {
					return (DiagnosesDTO) structuredSelection.getFirstElement();
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
		
		Button btnCancel;
		Button btnCorrection;
		
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
			
			btnCorrection = new Button(parent, SWT.NONE);
			btnCorrection.setEnabled(false);
			btnCorrection.setText("Rechnungskorrektur starten..");
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
			
			btnCancel = new Button(parent, SWT.NONE);
			btnCancel.setText("Zurücksetzen");
			btnCancel.setEnabled(false);
			btnCancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					log.debug("invoice correction: invoice reset");
					reload(actualInvoice);
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
						"Rechnungskorrektur", "Die Rechnung wurde erfolgreich zurückgesetzt.");
				}
			});
			
			this.setVisible(actualInvoice != null && actualInvoice.isCorrectable());
		}
		
		public void refresh(boolean hasChanges){
			if (invoiceCorrectionDTO != null) {
				if (btnCancel != null && hasChanges != btnCancel.isEnabled()) {
					btnCancel.setEnabled(hasChanges);
				}
				if (btnCorrection != null && hasChanges != btnCorrection.isEnabled()) {
					btnCorrection.setEnabled(hasChanges);
				}
			}
		}
	}
	
	/**
	 * Copies the actual fall, merge the copied fall with changes, transfer cons, storno the old
	 * invoice
	 */
	private Result<String> doBillCorrection(Rechnung actualInvoice){
		log.debug("invoice correction: start");
		if (actualInvoice != null && actualInvoice.isCorrectable()) {
			if (actualInvoice.getFall() != null && invoiceCorrectionDTO != null
				&& invoiceCorrectionDTO.getFallDTO() != null) {
				try {
					log.debug("invoice correction: invoice number [{}]", actualInvoice.getNr());
					invoiceCorrectionDTO.updateHistory();
					
					InvoiceCorrectionWizardDialog wizardDialog = new InvoiceCorrectionWizardDialog(
						getSite().getShell(), invoiceCorrectionDTO);
					wizardDialog.addPageChangedListener(new IPageChangedListener() {
						
						@Override
						public void pageChanged(PageChangedEvent event){
							
							if (event.getSelectedPage() instanceof Page2) {
								log.debug("invoice correction: processable changes {}",
									invoiceCorrectionDTO.getHistory().stream()
										.map(item -> item.getOperationType())
										.collect(Collectors.toList()));
								Page2 page = (Page2) event.getSelectedPage();
								InvoiceCorrectionDTO invoiceCorrectionDTO =
									page.getInvoiceCorrectionDTO();
								
								BillingUtil.doBillCorrection(invoiceCorrectionDTO,
									new BillCallback() {
										
										@Override
										public List<Konsultation> storno(Rechnung rechnung){
											RnDialogs.StornoDialog stronoDlg =
												new RnDialogs.StornoDialog(
													UiDesk.getDisplay().getActiveShell(), rechnung,
													true);
											if (stronoDlg.open() == Dialog.OK) {
												return stronoDlg.getKonsultations();
											} else {
												page.getTxtOutput().setText(
													"Die Rechnungskorrektur wurde durch den Benutzer abgebrochen.");
												return null;
											}
										}
									});
									
								page.updateProcess();
								if (invoiceCorrectionDTO.getOutputText() != null) {
									page.getTxtOutput()
										.setText(invoiceCorrectionDTO.getOutputText());
								}
							}
						}
						
					});
					
					int state = wizardDialog.open();
					if (invoiceCorrectionDTO.getOutputText() != null) {
						
						setInvoiceCorrectionInfo(actualInvoice);
						
						if (invoiceCorrectionDTO.isCorrectionSuccess()) {
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
							
							log.debug("invoice correction: successfully finished");
							return new Result<String>("ok");
						}
						log.debug("invoice correction: failed with warnings");
						return new Result<String>(SEVERITY.WARNING, 2, "warn", null, false);
					}
				} catch (Exception e) {
					log.error("invoice correction: failed with errors [{}]", actualInvoice.getId(),
						e);
					setInvoiceCorrectionInfo(actualInvoice);
					return new Result<String>(SEVERITY.ERROR, 2, "error",
						"Die Rechnungskorrektur konnte nicht vollständig durchgeführt werden.\nFür mehr Details, beachten Sie bitte das Log-File.",
						false);
				}
			}
		}
		return null;
	}
	
	private void setInvoiceCorrectionInfo(Rechnung actualInvoice){
		if (actualInvoice != null && invoiceCorrectionDTO != null) {
			actualInvoice.setExtInfo(Rechnung.INVOICE_CORRECTION,
				StringUtils.isEmpty(invoiceCorrectionDTO.getNewInvoiceNumber()) ? ""
						: invoiceCorrectionDTO.getNewInvoiceNumber());
		}
	}
	
	private boolean changePriceDialog(LeistungDTO leistungDTO){
		Money oldPrice = leistungDTO.getPrice();
		String p = oldPrice.getAmountAsString();
		Money customPrice;
		double factor = 1.0;
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
					double scaleFactor = 1.0 + (percent / 100.0);
					leistungDTO.setScale2(scaleFactor);
					customPrice = leistungDTO.getPrice();
				} else {
					customPrice = new Money(val);
					leistungDTO.setScale2(Double.valueOf(1));
				}
				if (customPrice != null) {
					leistungDTO.setTp(customPrice.getCents());
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
					leistungDTO.setScale2(secondaryScaleFactor);
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