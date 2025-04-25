package ch.elexis.core.ui.mediorder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMedicationService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.dnd.GenericObjectDropTarget;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class MediorderPart implements IRefreshablePart {

	@Inject
	private EPartService partService;

	@Inject
	IEventBroker eventBroker;

	@Inject
	IContextService contextService;

	@Inject
	IStockService stockService;

	@Inject
	IOrderService orderService;

	@Inject
	IStoreToStringService storeToStringService;

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	ESelectionService selectionService;

	@Inject
	IMedicationService medicationService;

	@Inject
	IStickerService stickerService;

	@Inject
	IConfigService configService;

	@Inject
	ITextReplacementService textReplacementService;

	private TableViewer tableViewer;
	private TableViewer tableViewerDetails;
	private TableViewer tableViewerHistory;

	private Composite cDetails_table;
	private Composite cHistory_table;
	private StackLayout stackLayout;
	private Composite viewComposite;

	private StockComparator stockComparator;
	private MedicationComparator medicationComparator;
	private MedicationHistoryComparator medicationHistoryComparator;
	private final DateTimeFormatter dateFormatter;

	private MediorderStockFilter searchFilter;
	private MediorderHistoryFilter orderHistoryFilter;
	private WritableValue<IStock> selectedDetailStock;
	private IPatient actPatient;

	public Map<IStock, Integer> imageStockStates = new HashMap<IStock, Integer>();
	private List<IStock> filteredStocks = new ArrayList<>();
	private List<Integer> currentFilterValue;
	private boolean filterActive = false;
	private boolean isDetailsViewActive = true;

	private Preferences preferences = InstanceScope.INSTANCE.getNode("ch.elexis.core.ui.mediorder");

	private static final String CURRENT_FILTER_VALUE = "currentFilterValues";
	private static final String IS_FILTER_ACTIVE = "isFilterActive";
	private static final String LAST_ACTIVE_TABLEVIEWER = "lastActiveView";
	private static final String ONLY_NUMBER_REGEX = "\\d*";
	
	public MediorderPart() {
		dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		selectedDetailStock = new WritableValue<>();
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
		activePatient(contextService.getActivePatient().orElse(null));
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tableViewer.getControl())) {
				if (actPatient != patient) {
					actPatient = patient;
					java.util.Optional<IStock> patientStock = stockService.getPatientStock(patient);
					if (patientStock.isPresent()) {
						tableViewer.setSelection(new StructuredSelection(patientStock.get()));
						tableViewer.refresh();
					}
				}
			}
		});
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IStock.class.equals(clazz)) {
			refresh();
		}
	}

	@Override
	public void refresh(Map<Object, Object> filterParameters) {
		Object firstElement = tableViewer.getStructuredSelection().getFirstElement();
		List<IStock> stocks = filterActive ? MediorderPartUtil.calculateFilteredStocks(currentFilterValue)
				: getStocksExcludingAwaitingRequests();
		stocks.forEach(stock -> MediorderPartUtil.updateStockImageState(imageStockStates, (IStock) stock));
		tableViewer.setInput(stocks);
		if (tableViewer.contains(firstElement)) {
			tableViewer.setSelection(new StructuredSelection(firstElement));
		}
		tableViewer.refresh(true);
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService, IExtensionRegistry extensionRegistry) {
		parent.setLayout(new GridLayout(1, false));

		stockComparator = new StockComparator();
		medicationComparator = new MedicationComparator();
		medicationHistoryComparator = new MedicationHistoryComparator();

		createSearchBar(parent);

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setSashWidth(5);
		createPatientorderListViewer(extensionRegistry, sashForm);

		viewComposite = new Composite(sashForm, SWT.NONE);
		stackLayout = new StackLayout();
		viewComposite.setLayout(stackLayout);

		createPatientorderDetailViewer(viewComposite);
		createPatientorderHistory(viewComposite);

		stackLayout.topControl = cDetails_table;
		viewComposite.layout();
		addDragAndDrop();

		menuService.registerContextMenu(tableViewer.getTable(), "ch.elexis.core.ui.mediorder.popupmenu.viewer"); //$NON-NLS-1$
		menuService.registerContextMenu(tableViewerDetails.getTable(),
				"ch.elexis.core.ui.mediorder.popupmenu.viewerdetails"); //$NON-NLS-1$

		tableViewer.setInput(getStocksExcludingAwaitingRequests());
		applySavedFilter();
		refresh();

		selectedDetailStock.addChangeListener(ev -> selectionService.setSelection(selectedDetailStock.getValue()));
	}

	public boolean toggleViews() {
		isDetailsViewActive = !isDetailsViewActive;
		stackLayout.topControl = isDetailsViewActive ? cDetails_table : cHistory_table;
		viewComposite.layout();
		saveFilterStatus();
		return isDetailsViewActive;
	}

	private void createSearchBar(Composite parent) {
		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Text txtSearch = new Text(searchComposite, SWT.BORDER | SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		txtSearch.setMessage(Messages.Core_DoSearch);
		txtSearch.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				searchFilter.setSearchTerm(txtSearch.getText());
				tableViewer.refresh();
			}
		});
	}

	private void createPatientorderListViewer(IExtensionRegistry extensionRegistry, Composite parent) {
		Composite cStockTable = new Composite(parent, SWT.NONE);
		cStockTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout = new TableColumnLayout();
		cStockTable.setLayout(tcLayout);

		tableViewer = new TableViewer(cStockTable, SWT.FULL_SELECTION | SWT.SINGLE | SWT.NONE);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setComparator(stockComparator);

		searchFilter = new MediorderStockFilter();
		tableViewer.addFilter(searchFilter);

		tableViewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			selectedDetailStock.setValue((IStock) selection.getFirstElement());
		});

		// order status
		TableViewerColumn tvcOrderState = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcOrderState.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public Image getImage(Object element) {
				IStock stock = (IStock) element;
				int number = MediorderPartUtil.getImageForStock(imageStockStates, stock);
				return switch (number) {
				// Represent an inactive order in PEA
				case 0 -> Images.IMG_BULLET_GREY.getImage();
				case 1 -> Images.IMG_BULLET_GREEN.getImage();
				case 2 -> Images.IMG_BULLET_YELLOW.getImage();
				case 3 -> Images.IMG_BULLET_BLUE.getImage();
				case 4 -> Images.IMG_MAIL_SEND.getImage();
				default -> null;
				};
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});

		TableColumn tblclmntvcOrderState = tvcOrderState.getColumn();
		tcLayout.setColumnData(tblclmntvcOrderState, new ColumnWeightData(0, 20, true));
		tblclmntvcOrderState.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(0);
				refresh();
			}
		});

		// patient number
		TableViewerColumn tvcPatientNumber = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientNumber.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				return stock.getOwner().getCode();
			}
		});
		TableColumn tblclmntvcPatientNumber = tvcPatientNumber.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientNumber, new ColumnWeightData(10, 70, true));
		tblclmntvcPatientNumber.setText(Messages.Core_Patient_Number);
		tblclmntvcPatientNumber.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(1);
				refresh();
			}
		});

		// patient lastname
		TableViewerColumn tvcPatientLastName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientLastName
				.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IStock) e).getOwner().getLastName()));
		TableColumn tblclmntvcPatientLastName = tvcPatientLastName.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientLastName, new ColumnWeightData(30, 200, true));
		tblclmntvcPatientLastName.setText(Messages.Core_Name);
		tblclmntvcPatientLastName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(2);
				refresh();
			}
		});

		// patient firstname
		TableViewerColumn tvcPatientFirstName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientFirstName
				.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IStock) e).getOwner().getFirstName()));
		TableColumn tblclmntvcPatientFirstName = tvcPatientFirstName.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientFirstName, new ColumnWeightData(30, 200, true));
		tblclmntvcPatientFirstName.setText(Messages.Core_Firstname);
		tblclmntvcPatientFirstName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(3);
				refresh();
			}
		});

		// patient birthdate
		TableViewerColumn tvcPatientBirthdate = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientBirthdate.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				LocalDateTime birthdate = stock.getOwner().getDateOfBirth();
				return birthdate.format(dateFormatter);
			}
		});
		TableColumn tblclmntvcPatientBirthdate = tvcPatientBirthdate.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientBirthdate, new ColumnWeightData(10, 90, true));
		tblclmntvcPatientBirthdate.setText(Messages.Core_Enter_Birthdate);
		tblclmntvcPatientBirthdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(4);
				refresh();
			}
		});

		cStockTable.setData("tableViewer", tableViewer);

		IConfigurationElement[] configurationElementsFor = extensionRegistry
				.getConfigurationElementsFor(ExtensionPointConstantsUi.VIEWCONTRIBUTION);
		List<IConfigurationElement> filteredExtensions = Arrays.asList(configurationElementsFor).stream()
				.filter(p -> MediorderPart.class.getName()
						.equalsIgnoreCase(p.getAttribute(ExtensionPointConstantsUi.VIEWCONTRIBUTION_VIEWID)))
				.collect(Collectors.toList());
		filteredExtensions.forEach(e -> {
			try {
				IViewContribution contribution = (IViewContribution) e
						.createExecutableExtension(ExtensionPointConstantsUi.VIEWCONTRIBUTION_CLASS);
				contribution.initComposite(cStockTable);
			} catch (CoreException e1) {
				LoggerFactory.getLogger(getClass()).error("Error", e1);
			}
		});

	}

	private void createPatientorderDetailViewer(Composite parent) {
		// PatientDetails
		cDetails_table = new Composite(parent, SWT.NONE);
		cDetails_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cDetails_table.setLayout(new GridLayout(1, false));

		setCompositeTitle(cDetails_table, Messages.Mediorder_details);

		Composite tableComposite = new Composite(cDetails_table, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout_cDetails = new TableColumnLayout();
		tableComposite.setLayout(tcLayout_cDetails);

		tableViewerDetails = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.MULTI);
		Table tableDetails = tableViewerDetails.getTable();
		tableDetails.setHeaderVisible(true);
		tableViewerDetails.setContentProvider(ArrayContentProvider.getInstance());
		selectedDetailStock.addChangeListener(sel -> {
			IStock stock = selectedDetailStock.getValue();
			if (stock != null) {
				List<IStockEntry> lStocks = stock.getStockEntries();
				tableViewerDetails.setInput(lStocks);
				lStocks.forEach(entry -> MediorderPartUtil.automaticallyFromDefaultStock(entry, stockService,
						coreModelService, contextService));
			} else {
				tableViewerDetails.setInput(null);
			}
		});
		tableViewerDetails.setComparator(medicationComparator);
		tableViewerDetails.addDoubleClickListener((DoubleClickEvent event) -> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (!selection.isEmpty()) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof IStockEntry) {
					IStockEntry entry = (IStockEntry) selectedElement;
					IOrderEntry orderEntry = orderService.findOpenOrderEntryForStockEntry(entry);
					if (orderEntry != null) {
						MPart part = partService.findPart("ch.elexis.BestellenView");
						if (part == null) {
							part = partService.showPart("ch.elexis.BestellenView", EPartService.PartState.CREATE);
						}
						partService.showPart(part, EPartService.PartState.ACTIVATE);
						eventBroker.post("BestellenView/orderSelected", orderEntry.getOrder());
					}
				}
			}
		});

		// MediorderEntryState
		TableViewerColumn tvcMediorderEntryState = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		TableColumn tblclmntvcMedicationOrdered = tvcMediorderEntryState.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationOrdered, new ColumnWeightData(10, 120, true));
		tblclmntvcMedicationOrdered.setText(Messages.Mediorder_Order_status);
		tblclmntvcMedicationOrdered.setImage(Images.IMG_PERSPECTIVE_ORDERS.getImage());
		tblclmntvcMedicationOrdered.setToolTipText(Messages.Mediorder_Order_status_Tooltip);
		tvcMediorderEntryState.setLabelProvider(
				ColumnLabelProvider.createTextProvider(MediorderPartUtil::createMediorderEntryStateLabel));

		// medication details
		TableViewerColumn tvcMedication = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedication.setLabelProvider(
				ColumnLabelProvider.createTextProvider(e -> ((IStockEntry) e).getArticle().getLabel()));
		TableColumn tblclmntvcMedication = tvcMedication.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedication, new ColumnWeightData(30, 400, true));
		tblclmntvcMedication.setText(Messages.Core_Article);
		tblclmntvcMedication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				medicationComparator.setColumn(0);
				refresh();
			}
		});

		// medication dosage instruction
		TableViewerColumn tvcMedicationDosage = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationDosage.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			IStockEntry entry = (IStockEntry) element;
			IPatient patient = entry.getStock().getOwner().asIPatient();
			List<IPrescription> lMedication = patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
					EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));
			for (IPrescription prescription : lMedication) {
				if (prescription.getArticle().equals(entry.getArticle())) {
					return prescription.getDosageInstruction();
				}
			}
			return "";
		}));
		TableColumn tblclmntvcMedicationDosage = tvcMedicationDosage.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationDosage, new ColumnWeightData(10, 70, true));
		tblclmntvcMedicationDosage.setText(Messages.Core_Dosage);

		// medication no days consumption per dosage
		TableViewerColumn tvcMediorderEntryOutreach = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMediorderEntryOutreach.setLabelProvider(
				ColumnLabelProvider.createTextProvider(MediorderPartUtil::createMediorderEntryOutreachLabel));
		TableColumn tblclmntvcMedicationAmountDay = tvcMediorderEntryOutreach.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmountDay, new ColumnWeightData(10, 100, true));
		tblclmntvcMedicationAmountDay.setText(Messages.Mediorder_sufficient_for);

		// medication designated amount for ordering
		TableViewerColumn tvcMedicationAmount = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationAmount.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			IStockEntry entry = (IStockEntry) element;
			return String.valueOf(entry.getMinimumStock());
		}));
		tvcMedicationAmount.setEditingSupport(new EditingSupport(tableViewerDetails) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return createTextCellEditor();
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				IStockEntry entry = (IStockEntry) element;
				return String.valueOf(entry.getMinimumStock());
			}

			@Override
			protected void setValue(Object element, Object value) {
				String amount = (String) value;
				if (StringUtils.isNotBlank(amount)) {
					IStockEntry entry = (IStockEntry) element;
					entry.setMinimumStock(Integer.parseInt(amount));
					coreModelService.save(entry);
					tableViewerDetails.refresh(true);
					removeStockEntry(entry);
					MediorderPartUtil.updateStockImageState(imageStockStates, entry.getStock());
					refresh();
				}
			}

		});
		TableColumn tblclmntvcMedicationAmount = tvcMedicationAmount.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmount, new ColumnWeightData(10, 110, true));
		tblclmntvcMedicationAmount.setText(Messages.Mediorder_requested);
		tblclmntvcMedicationAmount.setImage(Images.IMG_ACHTUNG.getImage());
		tblclmntvcMedicationAmount.setToolTipText(Messages.Mediorder_requested_Tooltip);

		// medication allowed amount for ordering
		TableViewerColumn tvcMedicationClearance = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationClearance.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			IStockEntry entry = (IStockEntry) element;
			return String.valueOf(entry.getMaximumStock());
		}));
		tvcMedicationClearance.setEditingSupport(new EditingSupport(tableViewerDetails) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return createTextCellEditor();
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				IStockEntry entry = (IStockEntry) element;
				return String.valueOf(entry.getMaximumStock());
			}

			@Override
			protected void setValue(Object element, Object value) {
				String amount = (String) value;
				if (StringUtils.isNotBlank(amount)) {
					IStockEntry entry = (IStockEntry) element;
					entry.setMaximumStock(Integer.parseInt(amount));
					coreModelService.save(entry);
					tableViewerDetails.refresh(true);
					removeStockEntry(entry);
					MediorderPartUtil.updateStockImageState(imageStockStates, entry.getStock());
				}
			}

		});
		TableColumn tblclmntvcMedicationClearance = tvcMedicationClearance.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationClearance, new ColumnWeightData(10, 110, true));
		tblclmntvcMedicationClearance.setImage(Images.IMG_TICK.getImage());
		tblclmntvcMedicationClearance.setText(Messages.Mediorder_approved);
		tblclmntvcMedicationClearance.setToolTipText(Messages.Mediorder_approved_Tooltip);

		// use from default stock
		TableViewerColumn tvcArticleFromDefaultStock = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		TableColumn tblclmntvcArticleFromDefaultStock = tvcArticleFromDefaultStock.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcArticleFromDefaultStock, new ColumnWeightData(0, 70, true));
		tblclmntvcArticleFromDefaultStock.setText(Messages.Mediorder_from_stock);
		tblclmntvcArticleFromDefaultStock.setToolTipText(Messages.Mediorder_from_stock_Tooltip);
		tvcArticleFromDefaultStock.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			IStockEntry entry = (IStockEntry) element;
			return stockService.findStockEntryForArticleInStock(stockService.getDefaultStock(),
					entry.getArticle()) != null ? String.valueOf(entry.getCurrentStock()) : String.valueOf(0);
		}));
		tvcArticleFromDefaultStock.setEditingSupport(new EditingSupport(tableViewerDetails) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(tableViewerDetails.getTable(),
						MediorderPartUtil.createValuesArray(((IStockEntry) element), stockService), SWT.READ_ONLY);
			}

			@Override
			protected boolean canEdit(Object element) {
				return stockService.findStockEntryForArticleInStock(stockService.getDefaultStock(),
						((IStockEntry) element).getArticle()) != null;
			}

			@Override
			protected Object getValue(Object element) {
				return ((IStockEntry) element).getCurrentStock();
			}

			@Override
			protected void setValue(Object element, Object value) {
				IStockEntry entry = (IStockEntry) element;
				IStockEntry defaultStockEntry = stockService
						.findStockEntryForArticleInStock(stockService.getDefaultStock(), entry.getArticle());
				if (defaultStockEntry == null) {
					return;
				}
				MediorderPartUtil.useFromDefaultStock(entry, defaultStockEntry, (int) value, stockService,
						coreModelService, contextService);
				MediorderPartUtil.updateStockImageState(imageStockStates, entry.getStock());
				tableViewerDetails.refresh();
				tableViewer.refresh();
			}
		});

		TableViewerColumn tvcOrderDate = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		TableColumn tblclmntvcOrderDate = tvcOrderDate.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcOrderDate, new ColumnWeightData(10, 80, true));
		tblclmntvcOrderDate.setText(Messages.Mediorder_order_date);
		tblclmntvcOrderDate.setToolTipText(Messages.Mediorder_order_date_Tooltip);
		tvcOrderDate.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			IStockEntry entry = (IStockEntry) element;
			IOrderEntry orderEntry = orderService.findOpenOrderEntryForStockEntry(entry);
			if (orderEntry != null) {
				String regex = "\\d{2}\\.\\d{2}\\.\\d{4}";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(orderEntry.getOrder().getLabel());
				if (matcher.find()) {
					return matcher.group();
				}
			}
			return null;
		}));
	}

	private void createPatientorderHistory(Composite parent) {
		cHistory_table = new Composite(parent, SWT.NONE);
		cHistory_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cHistory_table.setLayout(new GridLayout(1, false));

		setCompositeTitle(cHistory_table, Messages.Mediorder_history);

		Composite searchComposite = new Composite(cHistory_table, SWT.NONE);
		searchComposite.setLayout(new GridLayout(2, false));
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Text txtSearch = new Text(searchComposite, SWT.BORDER | SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		txtSearch.setMessage(Messages.Core_DoSearch);
		txtSearch.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				orderHistoryFilter.setSearchTerm(txtSearch.getText());
				tableViewerHistory.refresh();
			}
		});

		Composite tableComposite = new Composite(cHistory_table, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout_cHistory = new TableColumnLayout();
		tableComposite.setLayout(tcLayout_cHistory);

		tableViewerHistory = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.MULTI);
		Table tableHistory = tableViewerHistory.getTable();
		tableHistory.setHeaderVisible(true);
		tableViewerHistory.setContentProvider(ArrayContentProvider.getInstance());
		tableViewerHistory.setComparator(medicationHistoryComparator);
		selectedDetailStock.addChangeListener(sel -> {
			tableViewerHistory.setInput(java.util.Optional.ofNullable(selectedDetailStock.getValue())
					.map(orderService::findOrderEntryForStock).orElse(null));
		});

		orderHistoryFilter = new MediorderHistoryFilter();
		tableViewerHistory.addFilter(orderHistoryFilter);

		TableViewerColumn tvcMediorderOrderDate = new TableViewerColumn(tableViewerHistory, SWT.NONE);
		tvcMediorderOrderDate.setLabelProvider(ColumnLabelProvider
				.createTextProvider(e -> ((IOrderEntry) e).getOrder().getTimestamp().format(dateFormatter)));
		TableColumn tblclmntvcMedicationDosage = tvcMediorderOrderDate.getColumn();
		tcLayout_cHistory.setColumnData(tblclmntvcMedicationDosage, new ColumnWeightData(10, 70, true));
		tblclmntvcMedicationDosage.setText(Messages.Core_Date);
		tblclmntvcMedicationDosage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				medicationHistoryComparator.setColumn(2);
				refresh();
			}
		});

		TableViewerColumn tvcMediorderArticle = new TableViewerColumn(tableViewerHistory, SWT.NONE);
		TableColumn tblclmntvcMedicationArticle = tvcMediorderArticle.getColumn();
		tcLayout_cHistory.setColumnData(tblclmntvcMedicationArticle, new ColumnWeightData(30, 400, true));
		tblclmntvcMedicationArticle.setText(Messages.Core_Article);
		tvcMediorderArticle.setLabelProvider(
				ColumnLabelProvider.createTextProvider(e -> ((IOrderEntry) e).getArticle().getLabel()));
		tblclmntvcMedicationArticle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				medicationHistoryComparator.setColumn(0);
				refresh();
			}
		});

		TableViewerColumn tvcMediorderAmount = new TableViewerColumn(tableViewerHistory, SWT.NONE);
		tvcMediorderAmount.setLabelProvider(
				ColumnLabelProvider.createTextProvider(e -> (String.valueOf(((IOrderEntry) e).getAmount()))));
		TableColumn tblclmntvcMediorderAmount = tvcMediorderAmount.getColumn();
		tcLayout_cHistory.setColumnData(tblclmntvcMediorderAmount, new ColumnWeightData(10, 50, true));
		tblclmntvcMediorderAmount.setText(Messages.Core_Count);
		tblclmntvcMediorderAmount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				medicationHistoryComparator.setColumn(1);
				refresh();
			}
		});
	}

	private TextCellEditor createTextCellEditor() {
		TextCellEditor editor = new TextCellEditor(tableViewerDetails.getTable());
		((Text) editor.getControl()).addVerifyListener(e -> e.doit = e.text.matches(ONLY_NUMBER_REGEX));
		return editor;
	}
	
	/**
	 * IArticle drag and drop<br>
	 * Drag to tableViewer - add to to patient currently in context<br>
	 * Drag to tableViewerDetails - add to selected patient
	 */
	private void addDragAndDrop() {

		new GenericObjectDropTarget(storeToStringService, tableViewer.getTable(), (list, event) -> {
			list.forEach(entry -> {
				if (entry instanceof IArticle article) {
					addMedicationOrderEntryToStock(null, article);
				}
			});
			refresh();
		});

		new GenericObjectDropTarget(storeToStringService, tableViewerDetails.getTable(), (list, event) -> {
			list.forEach(entry -> {
				if (entry instanceof IArticle article) {
					IStock selectedStock = (IStock) tableViewer.getStructuredSelection().getFirstElement();
					addMedicationOrderEntryToStock(selectedStock, article);
				}
			});
			refresh();
		});
	}

	public class StockComparator extends ViewerComparator {
		private int propertyIndex;
		private int direction;

		public StockComparator() {
			this.propertyIndex = 0;
			this.direction = -1;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				direction *= -1;
			}
			this.propertyIndex = column;
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			IStock ts1 = (IStock) o1;
			IStock ts2 = (IStock) o2;

			int number1 = MediorderPartUtil.getImageForStock(imageStockStates, ts1);
			int number2 = MediorderPartUtil.getImageForStock(imageStockStates, ts2);

			switch (propertyIndex) {
			case 0 -> {
				return Objects.compare(number1, number2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			}
			case 1 -> {
				Integer patientNr1 = Integer.valueOf(ts1.getId().substring(13));
				Integer patientNr2 = Integer.valueOf(ts2.getId().substring(13));
				return Objects.compare(patientNr1, patientNr2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			}
			case 2 -> {
				String patientName1 = ts1.getOwner().getLastName();
				String patientName2 = ts2.getOwner().getLastName();
				return Objects.compare(patientName1, patientName2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			}
			case 3 -> {
				String patientFirstName1 = ts1.getOwner().getFirstName();
				String patientFirstName2 = ts2.getOwner().getFirstName();
				return Objects.compare(patientFirstName1, patientFirstName2,
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			}
			case 4 -> {
				LocalDateTime birthDate1 = ts1.getOwner().getDateOfBirth();
				return birthDate1.compareTo(ts2.getOwner().getDateOfBirth()) * direction;
			}
			}

			return super.compare(viewer, o1, o2);

		}
	}

	public class MedicationComparator extends ViewerComparator {
		private int propertyIndex;
		private int direction;

		public MedicationComparator() {
			this.propertyIndex = 0;
			this.direction = -1;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				direction *= -1;
			}
			this.propertyIndex = column;
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			IStockEntry stockEntry1 = (IStockEntry) o1;
			IStockEntry stockEntry2 = (IStockEntry) o2;

			switch (propertyIndex) {
			case 0:
				String articleName1 = stockEntry1.getArticle().getName();
				String articleName2 = stockEntry2.getArticle().getName();
				return Objects.compare(articleName1, articleName2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			}

			return super.compare(viewer, o1, o2);
		}
	}

	private void addMedicationOrderEntryToStock(IStock stock, IArticle article) {
		if (StringUtils.isBlank(article.getGtin())) {
			// TODO inform user not possible
			return;
		}

		if (stock == null) {
			IPatient patient = contextService.getActivePatient().get();
			stockService.setEnablePatientStock(patient, true);
			stock = stockService.getPatientStock(patient).get();
		}

		IStockEntry stockEntry = stockService.findStockEntryForArticleInStock(stock, article);
		if (stockEntry != null) {
			int value = stockEntry.getMinimumStock() + 1;
			stockEntry.setMinimumStock(value);
			value = stockEntry.getMaximumStock() + 1;
			stockEntry.setMaximumStock(value);
		} else {
			stockEntry = stockService.storeArticleInStock(stock, article);
			stockEntry.setCurrentStock(0);
			stockEntry.setMinimumStock(1);
			stockEntry.setMaximumStock(1);
		}
		coreModelService.save(stockEntry);
		MediorderPartUtil.updateStockImageState(imageStockStates, stock);
	}

	/**
	 * Retrieves a list of patient stocks that do not only have stockEntries with
	 * the status {@link MediorderEntryState#AWAITING_REQUEST}
	 * 
	 * @return
	 */
	private List<IStock> getStocksExcludingAwaitingRequests() {
		return stockService.getAllPatientStock().stream().filter(stock -> !stock.getStockEntries().isEmpty())
				.filter(stock -> stock.getStockEntries().stream().anyMatch(
						entry -> !MediorderEntryState.AWAITING_REQUEST.equals(MediorderUtil.determineState(entry))))
				.toList();
	}

	@SuppressWarnings("unchecked")
	public List<IStockEntry> getSelectedStockEntries() {
		return tableViewerDetails.getStructuredSelection().toList();
	}

	public IStock getSelectedStock() {
		return selectedDetailStock.getValue();
	}

	public void removeStockEntry(IStockEntry entry) {
		if (entry.getMaximumStock() == 0 && entry.getMinimumStock() == 0) {
			MediorderPartUtil.removeStockEntry(entry, coreModelService, contextService, stockService);
			refresh();
		}
	}

	private void setCompositeTitle(Composite composite, String title) {
		Label titleLabel = new Label(composite, SWT.NONE);
		titleLabel.setText(title);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		FontData fontData = titleLabel.getFont().getFontData()[0];
		Font boldFont = new Font(composite.getDisplay(),
				new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		titleLabel.setFont(boldFont);
	}

	public void saveFilterStatus() {
		String filterValue = (currentFilterValue == null || currentFilterValue.isEmpty()) ? ""
				: currentFilterValue.stream().map(String::valueOf).collect(Collectors.joining(","));
		boolean isFilterActive = filterValue.isEmpty() ? false : filterActive;

		preferences.put(CURRENT_FILTER_VALUE, filterValue);
		preferences.putBoolean(IS_FILTER_ACTIVE, isFilterActive);
		preferences.putBoolean(LAST_ACTIVE_TABLEVIEWER, isDetailsViewActive);

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			LoggerFactory.getLogger(getClass()).error("Error saving filter values", e);
		}
	}

	public void applySavedFilter() {
		filterActive = preferences.getBoolean(IS_FILTER_ACTIVE, false);

		String filterValue = preferences.get(CURRENT_FILTER_VALUE, "");
		if (!filterValue.isEmpty() && filterActive) {
			currentFilterValue = Arrays.stream(filterValue.split(",")).map(Integer::parseInt)
					.collect(Collectors.toList());
		}

		stackLayout.topControl = preferences.getBoolean(LAST_ACTIVE_TABLEVIEWER, true) ? cDetails_table
				: cHistory_table;
		viewComposite.layout();
	}

	public void setFilterActive(boolean active) {
		this.filterActive = active;
		saveFilterStatus();
	}

	public boolean isFilterActive() {
		return filterActive;
	}

	public void setFilteredStocks(List<IStock> stocks) {
		this.filteredStocks = stocks;
	}

	public List<IStock> getFilteredStocks() {
		return filteredStocks;
	}

	public void setCurrentFilterValue(List<Integer> value) {
		this.currentFilterValue = value;
		saveFilterStatus();
	}

	public List<Integer> getCurrentFilterValue() {
		return this.currentFilterValue;
	}
}