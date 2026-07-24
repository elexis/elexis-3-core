package ch.elexis.core.ui.mediorder;

import java.time.LocalDate;
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
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContactService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMedicationService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.dialog.IContactSelectorDialog;
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
	IContactService contactService;

	@Inject
	ICodeElementService codeElementService;

	@Inject
	ITextReplacementService textReplacementService;

	public enum MediorderActiveView {
		DETAILS, HISTORY, // JSON_HISTORY
	}

	private MediorderActiveView mediorderActiveView = MediorderActiveView.DETAILS;

	private SashForm mainSashForm;

	private TableViewer tableViewer;
	private TableViewer tableViewerDetails;
	private TableViewer tableViewerHistory;
	private TableViewer tableViewerImportedPatients;
	private TableViewer tableViewerImportedArticles;

	private Composite cDetails_table;
	private Composite cHistory_table;
	private Composite cPatientError_table;
	private Composite cPatientList;
	private StackLayout stackLayout;
	private StackLayout topStackLayout;
	private Composite viewComposite;
	private Composite topViewComposite;

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

	private IPatient importedPatient, selectedImportedPatient;
	private Button btnUseSelected, btnSelectNewPatient, btnNewPatient, btnError;
	private Label txtImportName, txtImportFirstName, txtImportDob, txtImportSex, txtImportStreet, txtImportPostalCode,
			txtImportCity, txtImportEmail, txtImportMobile;
	private Label txtExistingName, txtExistingFirstName, txtExistingDob, txtExistingSex, txtExistingStreet,
			txtExistingPostalCode, txtExistingCity, txtExistingPostalEmail, txtExistingPostalMobile;

	private boolean activePatient = false;

	private List<IPatient> importedPatients;
	private TableViewer tableViewerPatientError;

	private static final String CURRENT_FILTER_VALUE = "currentFilterValues";
	private static final String IS_FILTER_ACTIVE = "isFilterActive";
	private static final String LAST_ACTIVE_TABLEVIEWER = "lastActiveView";
	private static final String ONLY_NUMBER_REGEX = "\\d*";

	public static class PatientImportData {
		public IPatient patient;
		public String street;
		public String postalCode;
		public String city;
		public String email;
		public String mobile;
		public String blobId;
		public Map<String, Integer> articleGtinsWithAmount = new HashMap<>();
	}

	public static class ImportedArticleRow {
		public final String gtin;
		public final int amount;
		public final IArticle article;

		public ImportedArticleRow(String gtin, int amount, IArticle article) {
			this.gtin = gtin;
			this.amount = amount;
			this.article = article;
		}
	}

	private List<PatientImportData> importedPatientDataList = new ArrayList<>();
	private PatientImportData selectedImportedPatientData;

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

	public void setActivePatient(boolean activePatient) {
		this.activePatient = activePatient;
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

		getImportedPatients();
		refreshImportedPatientsTable();
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService, IExtensionRegistry extensionRegistry) {
		parent.setLayout(new GridLayout(1, false));

		stockComparator = new StockComparator();
		medicationComparator = new MedicationComparator();
		medicationHistoryComparator = new MedicationHistoryComparator();

		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayout(new GridLayout(4, false));
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		getImportedPatients();

		btnError = new Button(filterComposite, SWT.TOGGLE);
		btnError.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		updateErrorButtonText();
		btnError.setText("Fehlerhaft (" + (importedPatients != null ? importedPatients.size() : 0) + ")");
		btnError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnError.getSelection()) {
					topStackLayout.topControl = cPatientError_table;
					getImportedPatients();
					tableViewerImportedPatients.setInput(importedPatients);
					tableViewerImportedPatients.refresh();
					mainSashForm.setWeights(new int[] { 100, 0 });
					viewComposite.setVisible(false);
				} else {
					topStackLayout.topControl = cPatientList;
					viewComposite.setVisible(true);
					mainSashForm.setWeights(new int[] { 50, 50 });
				}
				topViewComposite.layout();
			}
		});

		mainSashForm = new SashForm(parent, SWT.VERTICAL);
		mainSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainSashForm.setSashWidth(5);
		topViewComposite = new Composite(mainSashForm, SWT.NONE);
		topStackLayout = new StackLayout();
		topViewComposite.setLayout(topStackLayout);
		createPatientorderListViewer(extensionRegistry, topViewComposite);
		createPatientErrorViewer(topViewComposite);

		topStackLayout.topControl = cPatientList;
		topViewComposite.layout();

		viewComposite = new Composite(mainSashForm, SWT.NONE);
		stackLayout = new StackLayout();
		viewComposite.setLayout(stackLayout);

		createPatientorderDetailViewer(viewComposite);
		createPatientorderHistory(viewComposite);

		stackLayout.topControl = cDetails_table;
		mainSashForm.setWeights(new int[] { 50, 50 });
		viewComposite.layout();
		addDragAndDrop();

		menuService.registerContextMenu(tableViewer.getTable(), "ch.elexis.core.ui.mediorder.popupmenu.viewer"); //$NON-NLS-1$
		menuService.registerContextMenu(tableViewerDetails.getTable(),
				"ch.elexis.core.ui.mediorder.popupmenu.viewerdetails"); //$NON-NLS-1$
		menuService.registerContextMenu(tableViewerImportedPatients.getTable(),
				"ch.elexis.core.ui.mediorder.popupmenu.viewerimportedpatients"); //$NON-NLS-1$

		tableViewer.setInput(getStocksExcludingAwaitingRequests());
		applySavedFilter();
		refresh();

		selectedDetailStock.addChangeListener(ev -> selectionService.setSelection(selectedDetailStock.getValue()));
	}

	public MediorderActiveView toggleViews(MediorderActiveView view) {
		mediorderActiveView = (mediorderActiveView == view) ? MediorderActiveView.DETAILS : view;
		stackLayout.topControl = switch (mediorderActiveView) {
		case DETAILS -> cDetails_table;
		case HISTORY -> cHistory_table;
		};
		viewComposite.layout();
		saveFilterStatus();
		return mediorderActiveView;
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
		cPatientList = new Composite(parent, SWT.NONE);
		cPatientList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cPatientList.setLayout(new GridLayout(1, false));

		createSearchBar(cPatientList);

		Composite tableComposite = new Composite(cPatientList, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableComposite.setLayout(tcLayout);

		tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.NONE);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setComparator(stockComparator);

		searchFilter = new MediorderStockFilter();
		tableViewer.addFilter(searchFilter);

		tableViewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			IStock stock = (IStock) selection.getFirstElement();
			selectedDetailStock.setValue(stock);

			if (activePatient) {
				contextService.setActivePatient(stock.getOwner().asIPatient());
			}
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

		cPatientList.setData("tableViewer", tableViewer);

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
				contribution.initComposite(cPatientList);
			} catch (CoreException e1) {
				LoggerFactory.getLogger(getClass()).error("Error", e1);
			}
		});

	}

	private void getImportedPatients() {
		if (importedPatients == null) {
			importedPatients = new ArrayList<>();
		} else {
			importedPatients.clear();
		}
		if (importedPatientDataList == null) {
			importedPatientDataList = new ArrayList<>();
		} else {
			importedPatientDataList.clear();
		}

		IQuery<IBlob> query = coreModelService.getQuery(IBlob.class);
		query.and("id", COMPARATOR.LIKE, "MEDIORDER_UNDEFINDED_%");
		List<IBlob> results = query.execute();

		for (IBlob blob : results) {
			try {
				PatientImportData data = parsePatientFromQuestionnaireResponse(blob.getStringContent());
				if (data != null && data.patient != null) {
					data.blobId = blob.getId();
					importedPatients.add(data.patient);
					importedPatientDataList.add(data);
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Fehler beim Parsen von Blob: " + blob.getId(), e);
			}
		}
	}

	private void getDuplicatePatients(IPatient patient) {
		List<IPerson> duplicatePatients = contactService.findPersonFuzzy(patient.getDateOfBirth().toLocalDate(),
				patient.getGender(), patient.getLastName(), patient.getFirstName(), 6, false);
		List<IPatient> duplicatePatientsAsIPatient = duplicatePatients.stream().map(IPerson::asIPatient)
				.filter(Objects::nonNull).collect(Collectors.toList());

		tableViewerPatientError.setInput(duplicatePatientsAsIPatient);
		tableViewerPatientError.refresh();
		btnUseSelected.setEnabled(false);
	}

	private PatientImportData parsePatientFromQuestionnaireResponse(String json) throws Exception {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		JsonArray items = root.getAsJsonArray("item");

		JsonArray patientItems = items.get(0).getAsJsonObject().getAsJsonArray("item");
		Map<String, String> values = MediorderPartUtil.extractItemValues(patientItems);

		IPatient patient = coreModelService.create(IPatient.class);
		patient.setFirstName(values.get("Vorname"));
		patient.setLastName(values.get("Nachname"));
		patient.setGender("MALE".equals(values.get("Geschlecht")) ? Gender.MALE : Gender.FEMALE);
		if (values.get("Geburtsdatum") != null) {
			patient.setDateOfBirth(LocalDate.parse(values.get("Geburtsdatum")).atStartOfDay());
		}

		PatientImportData data = new PatientImportData();
		data.patient = patient;
		data.street = StringUtils.defaultString(values.get("Strasse"));
		data.postalCode = StringUtils.defaultString(values.get("Postleitzahl"));
		data.city = StringUtils.defaultString(values.get("Ort"));
		data.email = StringUtils.defaultString(values.get("E-Mail"));
		data.mobile = StringUtils.defaultString(values.get("Telefon"));

		if (items.size() > 2) {
			data.articleGtinsWithAmount.putAll(MediorderPartUtil.extractMedications(items));
		}
		return data;
	}

	private void createPatientErrorViewer(Composite parent) {
		cPatientError_table = new Composite(parent, SWT.NONE);
		cPatientError_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cPatientError_table.setLayout(new GridLayout(1, false));

		SashForm sashForm = new SashForm(cPatientError_table, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setSashWidth(1);

		Composite importComposite = new Composite(sashForm, SWT.NONE);
		importComposite.setLayout(new GridLayout(1, false));

		Label lblImportList = new Label(importComposite, SWT.NONE);
		lblImportList.setText("Importierte Patienten");
		lblImportList.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		FontData fdImp = lblImportList.getFont().getFontData()[0];
		lblImportList.setFont(
				new Font(importComposite.getDisplay(), new FontData(fdImp.getName(), fdImp.getHeight(), SWT.BOLD)));

		Composite importTableComposite = new Composite(importComposite, SWT.NONE);
		importTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tcLayoutImport = new TableColumnLayout();
		importTableComposite.setLayout(tcLayoutImport);

		tableViewerImportedPatients = new TableViewer(importTableComposite,
				SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		tableViewerImportedPatients.getTable().setHeaderVisible(true);
		tableViewerImportedPatients.getTable().setLinesVisible(true);
		tableViewerImportedPatients.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn tvcImpName = new TableViewerColumn(tableViewerImportedPatients, SWT.NONE);
		tvcImpName.getColumn().setText(Messages.Core_Name);
		tcLayoutImport.setColumnData(tvcImpName.getColumn(), new ColumnWeightData(40, 80, true));
		tvcImpName.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IPatient) e).getLastName()));

		TableViewerColumn tvcImpFirstName = new TableViewerColumn(tableViewerImportedPatients, SWT.NONE);
		tvcImpFirstName.getColumn().setText(Messages.Core_Firstname);
		tcLayoutImport.setColumnData(tvcImpFirstName.getColumn(), new ColumnWeightData(40, 80, true));
		tvcImpFirstName.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IPatient) e).getFirstName()));

		TableViewerColumn tvcImpDateOfBirth = new TableViewerColumn(tableViewerImportedPatients, SWT.NONE);
		tvcImpDateOfBirth.getColumn().setText(Messages.Core_Enter_Birthdate);
		tcLayoutImport.setColumnData(tvcImpDateOfBirth.getColumn(), new ColumnWeightData(40, 80, true));
		tvcImpDateOfBirth.setLabelProvider(ColumnLabelProvider.createTextProvider(
				e -> ((IPatient) e).getDateOfBirth().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

		TableViewerColumn tvcImpSex = new TableViewerColumn(tableViewerImportedPatients, SWT.NONE);
		tvcImpSex.getColumn().setText("Geschlecht");
		tcLayoutImport.setColumnData(tvcImpSex.getColumn(), new ColumnWeightData(40, 80, true));
		tvcImpSex.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> {
			return ((IPatient) e).getGender() == Gender.MALE ? "m" : "w";
		}));

		tableViewerImportedPatients.addSelectionChangedListener(event -> {
			IStructuredSelection sel = tableViewerImportedPatients.getStructuredSelection();
			if (!sel.isEmpty()) {
				IPatient selected = (IPatient) sel.getFirstElement();
				selectedImportedPatientData = importedPatientDataList.stream().filter(d -> d.patient == selected)
						.findFirst().orElse(null);
				importedPatient = selected;
				selectedImportedPatient = selected;
				btnNewPatient.setEnabled(true);
				btnSelectNewPatient.setEnabled(true);
				updateImportedPatientFields(selectedImportedPatientData);
				updateImportedArticlesTable(selectedImportedPatientData);
				getDuplicatePatients(selected);
			} else {
				btnNewPatient.setEnabled(true);
				btnSelectNewPatient.setEnabled(false);
				updateImportedArticlesTable(null);
			}
		});

		tableViewerImportedPatients.setInput(importedPatients);

		Composite similarComposite = new Composite(sashForm, SWT.NONE);
		similarComposite.setLayout(new GridLayout(1, false));

		Label lblSimilarList = new Label(similarComposite, SWT.NONE);
		lblSimilarList.setText("Ähnliche Patienten");
		lblSimilarList.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		FontData fdSim = lblSimilarList.getFont().getFontData()[0];
		lblSimilarList.setFont(
				new Font(similarComposite.getDisplay(), new FontData(fdSim.getName(), fdSim.getHeight(), SWT.BOLD)));

		Composite similarTableComposite = new Composite(similarComposite, SWT.NONE);
		similarTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tcLayoutSimilar = new TableColumnLayout();
		similarTableComposite.setLayout(tcLayoutSimilar);

		tableViewerPatientError = new TableViewer(similarTableComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		tableViewerPatientError.getTable().setHeaderVisible(true);
		tableViewerPatientError.getTable().setLinesVisible(true);
		// ColumnViewerToolTipSupport.enableFor(tableViewerPatientError);
		tableViewerPatientError.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn tvcName = new TableViewerColumn(tableViewerPatientError, SWT.NONE);
		tvcName.getColumn().setText(Messages.Core_Name);
		tcLayoutSimilar.setColumnData(tvcName.getColumn(), new ColumnWeightData(40, 80, true));
		tvcName.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IPatient) e).getLastName()));

		TableViewerColumn tvcFirstName = new TableViewerColumn(tableViewerPatientError, SWT.NONE);
		tvcFirstName.getColumn().setText(Messages.Core_Firstname);
		tcLayoutSimilar.setColumnData(tvcFirstName.getColumn(), new ColumnWeightData(40, 80, true));
		tvcFirstName.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IPatient) e).getFirstName()));

		TableViewerColumn tvcDob = new TableViewerColumn(tableViewerPatientError, SWT.NONE);
		tvcDob.getColumn().setText(Messages.Core_Enter_Birthdate);
		tcLayoutSimilar.setColumnData(tvcDob.getColumn(), new ColumnWeightData(40, 80, true));
		tvcDob.setLabelProvider(ColumnLabelProvider.createTextProvider(
				e -> ((IPatient) e).getDateOfBirth() != null ? ((IPatient) e).getDateOfBirth().format(dateFormatter)
						: ""));

		TableViewerColumn tvcSex = new TableViewerColumn(tableViewerPatientError, SWT.NONE);
		tvcSex.getColumn().setText("Geschlecht");
		tcLayoutSimilar.setColumnData(tvcSex.getColumn(), new ColumnWeightData(40, 80, true));
		tvcSex.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> {
			return ((IPatient) e).getGender() == Gender.MALE ? "m" : "w";
		}));

		tableViewerPatientError.addSelectionChangedListener(event -> {
			IStructuredSelection sel = tableViewerPatientError.getStructuredSelection();
			if (!sel.isEmpty()) {
				IPatient match = (IPatient) sel.getFirstElement();
				updateDetailComparison(match);
				btnNewPatient.setEnabled(false);
				btnUseSelected.setEnabled(true);
			} else {
				btnUseSelected.setEnabled(false);
			}
		});

		Composite detailComposite = new Composite(sashForm, SWT.NONE);
		detailComposite.setLayout(new GridLayout(1, false));

		Composite compareHeader = new Composite(detailComposite, SWT.NONE);
		compareHeader.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		compareHeader.setLayout(new GridLayout(2, true));
		FontData fdCol = compareHeader.getFont().getFontData()[0];
		Font boldFont = new Font(compareHeader.getDisplay(),
				new FontData(fdCol.getName(), fdCol.getHeight(), SWT.BOLD));

		Label lblColImport = new Label(compareHeader, SWT.NONE);
		lblColImport.setText("Importierter Patient");
		lblColImport.setFont(boldFont);
		lblColImport.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label lblColExisting = new Label(compareHeader, SWT.NONE);
		lblColExisting.setText("Bestehender Patient");
		lblColExisting.setFont(boldFont);
		lblColExisting.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Composite compareGrid = new Composite(detailComposite, SWT.NONE);
		compareGrid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compareGrid.setLayout(new GridLayout(2, true));

		Composite colImport = new Composite(compareGrid, SWT.BORDER);
		colImport.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		colImport.setLayout(new GridLayout(2, false));

		Composite colExisting = new Composite(compareGrid, SWT.BORDER);
		colExisting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		colExisting.setLayout(new GridLayout(2, false));

		txtImportName = createDetailField(colImport, Messages.Core_Name,
				selectedImportedPatient != null ? selectedImportedPatient.getLastName() : "");
		txtImportFirstName = createDetailField(colImport, Messages.Core_Firstname,
				selectedImportedPatient != null ? selectedImportedPatient.getLastName() : "");
		txtImportDob = createDetailField(colImport, Messages.Core_Enter_Birthdate, "");
		txtImportSex = createDetailField(colImport, "Geschlecht", "");
		txtImportStreet = createDetailField(colImport, Messages.AddressSearchView_Street, "");
		txtImportPostalCode = createDetailField(colImport, Messages.AddressSearchView_Zip, "");
		txtImportCity = createDetailField(colImport, Messages.AddressSearchView_City, "");
		txtImportEmail = createDetailField(colImport, Messages.KontaktErfassenDialog_email, "");
		txtImportMobile = createDetailField(colImport, Messages.Core_Phone, "");

		txtExistingName = createDetailField(colExisting, Messages.Core_Name, "");
		txtExistingFirstName = createDetailField(colExisting, Messages.Core_Firstname, "");
		txtExistingDob = createDetailField(colExisting, Messages.Core_Enter_Birthdate, "");
		txtExistingSex = createDetailField(colExisting, "Geschlecht", "");
		txtExistingStreet = createDetailField(colExisting, Messages.AddressSearchView_Street, "");
		txtExistingPostalCode = createDetailField(colExisting, Messages.AddressSearchView_Zip, "");
		txtExistingCity = createDetailField(colExisting, Messages.AddressSearchView_City, "");
		txtExistingPostalEmail = createDetailField(colExisting, Messages.KontaktErfassenDialog_email, "");
		txtExistingPostalMobile = createDetailField(colExisting, Messages.Core_Phone, "");

//		sashForm.setWeights(new int[] { 25, 25, 49 });

		Composite actionBar = new Composite(detailComposite, SWT.BORDER);
		actionBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		actionBar.setLayout(new GridLayout(4, false));

		Label lblHint = new Label(actionBar, SWT.NONE);
		lblHint.setText("Ähnlichen Patienten wählen oder neuen erstellen.");
		lblHint.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		btnNewPatient = new Button(actionBar, SWT.PUSH);
		btnNewPatient.setText("Neuen Patient erstellen");
		btnNewPatient.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		btnNewPatient.setEnabled(false);
		btnNewPatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedImportedPatient == null || selectedImportedPatientData == null)
					return;

				IPatient patient = new IContactBuilder.PatientBuilder(coreModelService,
						selectedImportedPatient.getFirstName(), selectedImportedPatient.getLastName(),
						selectedImportedPatient.getDateOfBirth().toLocalDate(), selectedImportedPatient.getGender())
						.build();
				patient.setStreet(selectedImportedPatientData.street);
				patient.setZip(selectedImportedPatientData.postalCode);
				patient.setCity(selectedImportedPatientData.city);
				patient.setEmail(selectedImportedPatientData.email);
				patient.setMobile(selectedImportedPatientData.mobile);
				coreModelService.save(patient);

				relinkBlobToPatient(selectedImportedPatientData.blobId, patient);
				IStock stock = storeArticlesInPatientStock(patient, selectedImportedPatientData.articleGtinsWithAmount);

				importedPatient = null;
				getImportedPatients();
				finishPatientAssignment(stock);
			}
		});

		btnUseSelected = new Button(actionBar, SWT.PUSH);
		btnUseSelected.setText("Ausgewählten verwenden");
		btnUseSelected.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		btnUseSelected.setEnabled(false);
		btnUseSelected.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = tableViewerPatientError.getStructuredSelection();
				if (!sel.isEmpty()) {
					IPatient patient = (IPatient) sel.getFirstElement();

					relinkBlobToPatient(selectedImportedPatientData.blobId, patient);
					IStock stock = storeArticlesInPatientStock(patient,
							selectedImportedPatientData.articleGtinsWithAmount);

					importedPatients.remove(selectedImportedPatient);
					importedPatientDataList.remove(selectedImportedPatientData);

					finishPatientAssignment(stock);
				}
			}
		});

		btnSelectNewPatient = new Button(actionBar, SWT.PUSH);
		btnSelectNewPatient.setText("Patient suchen");
		btnSelectNewPatient.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		btnSelectNewPatient.setEnabled(false);
		btnSelectNewPatient.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IContactSelectorDialog csd = new IContactSelectorDialog(parent.getShell(), coreModelService,
						IPatient.class);
				csd.setTitle("Kontakt auswählen");
				csd.setMessage(String.format("Wählen Sie einen Patienten aus, welchem %s %s zugeordnet werden soll.",
						selectedImportedPatient.getFirstName(), selectedImportedPatient.getLastName()));
				int retVal = csd.open();
				if (Dialog.OK == retVal) {
					IPatient patient = csd.getSelectedContact().asIPatient();

					relinkBlobToPatient(selectedImportedPatientData.blobId, patient);
					IStock stock = storeArticlesInPatientStock(patient,
							selectedImportedPatientData.articleGtinsWithAmount);

					importedPatients.remove(selectedImportedPatient);
					importedPatientDataList.remove(selectedImportedPatientData);

					finishPatientAssignment(stock);
				}
			}
		});
		createImportedArticleViewer(sashForm);
		sashForm.setWeights(new int[] { 20, 20, 25, 35 });
	}

	private void finishPatientAssignment(IStock stock) {
		refreshImportedPatientsTable();
		resetErrorSelection();
		selectStockInDetailView(stock);
	}

	private void resetErrorSelection() {
		selectedImportedPatient = null;
		selectedImportedPatientData = null;
		tableViewerPatientError.setInput(null);
		tableViewerPatientError.refresh();
		clearComparisonFields();
		btnNewPatient.setEnabled(false);
		btnUseSelected.setEnabled(false);
		btnSelectNewPatient.setEnabled(false);
	}

	private void refreshImportedPatientsTable() {
		tableViewerImportedPatients.setInput(importedPatients);
		tableViewerImportedPatients.refresh();
		updateErrorButtonText();
	}

	private void selectStockInDetailView(IStock stock) {
		refresh();
		selectedDetailStock.setValue(stock);
		tableViewer.setSelection(new StructuredSelection(stock));
	}

	private void relinkBlobToPatient(String blobId, IPatient patient) {
		if (blobId == null) {
			return;
		}
		coreModelService.load(blobId, IBlob.class).ifPresent(oldBlob -> {
			String originalContent = oldBlob.getStringContent();
			coreModelService.delete(oldBlob);

			IBlob newBlob = coreModelService.create(IBlob.class);
			newBlob.setId("MEDIORDER_" + patient.getId());
			newBlob.setStringContent(originalContent);
			coreModelService.save(newBlob);
		});
	}

	private IStock storeArticlesInPatientStock(IPatient patient, Map<String, Integer> articleGtinsWithAmount) {
		IStock stock = stockService.getOrCreatePatientStock(patient);
		for (Map.Entry<String, Integer> entry : articleGtinsWithAmount.entrySet()) {
			String gtin = entry.getKey();
			int amount = entry.getValue();
			try {
				java.util.Optional<IArticle> article = codeElementService.findArticleByGtin(gtin);
				if (article.isPresent()) {
					IStockEntry stockEntry = stockService.storeArticleInStock(stock, article.get());
					stockEntry.setCurrentStock(0);
					stockEntry.setMinimumStock(amount);
					stockEntry.setMaximumStock(amount);
					coreModelService.save(stockEntry);
				} else {
					LoggerFactory.getLogger(getClass()).warn("Artikel mit GTIN {} nicht gefunden", gtin);
				}
			} catch (IllegalStateException e) {
				LoggerFactory.getLogger(getClass()).error("Fehler beim Suchen des Artikels mit GTIN {}", gtin, e);
			}
		}
		return stock;
	}

	private Label createDetailField(Composite parent, String labelText, String value) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(labelText + ":");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		Label val = new Label(parent, SWT.WRAP);
		val.setText(value);
		val.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return val;
	}

	private String formatDob(IPatient patient) {
		return patient != null && patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(dateFormatter)
				: "";
	}

	private String formatGender(IPatient patient) {
		return patient != null && patient.getGender() != null ? patient.getGender().toString() : "";
	}

	private void updateImportedPatientFields(PatientImportData data) {
		if (data == null)
			return;
		IPatient patient = data.patient;

		txtImportName.setText(StringUtils.defaultString(patient.getLastName()));
		txtImportFirstName.setText(StringUtils.defaultString(patient.getFirstName()));
		txtImportDob.setText(formatDob(patient));
		txtImportSex.setText(formatGender(patient));
		txtImportStreet.setText(data.street);
		txtImportPostalCode.setText(data.postalCode);
		txtImportCity.setText(data.city);
		txtImportEmail.setText(data.email);
		txtImportMobile.setText(data.mobile);

		txtExistingName.setText("");
		txtExistingFirstName.setText("");
		txtExistingDob.setText("");
		txtExistingSex.setText("");
		txtExistingStreet.setText("");
		txtExistingPostalCode.setText("");
		txtExistingCity.setText("");
		txtExistingPostalEmail.setText("");
		txtExistingPostalMobile.setText("");

		btnUseSelected.setEnabled(false);
	}

	private void updateDetailComparison(IPatient existing) {
		if (importedPatient == null || existing == null)
			return;

		Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

		String importLastName = StringUtils.defaultString(importedPatient.getLastName());
		String importFirstName = StringUtils.defaultString(importedPatient.getFirstName());
		String importDob = formatDob(importedPatient);
		String importSex = formatGender(importedPatient);
		String importStreet = selectedImportedPatientData != null ? selectedImportedPatientData.street : "";
		String importPostal = selectedImportedPatientData != null ? selectedImportedPatientData.postalCode : "";
		String importCity = selectedImportedPatientData != null ? selectedImportedPatientData.city : "";
		String importEmail = selectedImportedPatientData != null ? selectedImportedPatientData.email : "";
		String importMobile = selectedImportedPatientData != null ? selectedImportedPatientData.mobile : "";

		txtImportName.setText(importLastName);
		txtImportFirstName.setText(importFirstName);
		txtImportDob.setText(importDob);
		txtImportSex.setText(importSex);

		String existingLastName = StringUtils.defaultString(existing.getLastName());
		String existingFirstName = StringUtils.defaultString(existing.getFirstName());
		String existingDob = formatDob(existing);
		String existingSex = formatGender(existing);
		String existingStreet = StringUtils.defaultString(existing.getStreet());
		String existingPostal = StringUtils.defaultString(existing.getZip());
		String existingCity = StringUtils.defaultString(existing.getCity());
		String existingEmail = StringUtils.defaultString(existing.getEmail());
		String existingMobile = StringUtils.defaultString(existing.getMobile());

		setCompareField(txtImportName, txtExistingName, importLastName, existingLastName, black, red);
		setCompareField(txtImportFirstName, txtExistingFirstName, importFirstName, existingFirstName, black, red);
		setCompareField(txtImportDob, txtExistingDob, importDob, existingDob, black, red);
		setCompareField(txtImportSex, txtExistingSex, importSex, existingSex, black, red);
		setCompareField(txtImportStreet, txtExistingStreet, importStreet, existingStreet, black, red);
		setCompareField(txtImportPostalCode, txtExistingPostalCode, importPostal, existingPostal, black, red);
		setCompareField(txtImportCity, txtExistingCity, importCity, existingCity, black, red);
		setCompareField(txtImportEmail, txtExistingPostalEmail, importEmail, existingEmail, black, red);
		setComparePhoneField(txtImportMobile, txtExistingPostalMobile, importMobile, existingMobile, black, red);
	}

	private void clearComparisonFields() {
		txtImportName.setText("");
		txtImportFirstName.setText("");
		txtImportDob.setText("");
		txtImportSex.setText("");
		txtImportStreet.setText("");
		txtImportPostalCode.setText("");
		txtImportCity.setText("");
		txtImportEmail.setText("");
		txtImportMobile.setText("");

		txtExistingName.setText("");
		txtExistingFirstName.setText("");
		txtExistingDob.setText("");
		txtExistingSex.setText("");
		txtExistingStreet.setText("");
		txtExistingPostalCode.setText("");
		txtExistingCity.setText("");
		txtExistingPostalEmail.setText("");
		txtExistingPostalMobile.setText("");

		Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		txtImportName.setForeground(black);
		txtImportFirstName.setForeground(black);
		txtImportDob.setForeground(black);
		txtImportSex.setForeground(black);
		txtImportStreet.setForeground(black);
		txtImportPostalCode.setForeground(black);
		txtImportCity.setForeground(black);
		txtImportEmail.setForeground(black);
		txtImportMobile.setForeground(black);
	}

	private void updateErrorButtonText() {
		if (btnError != null) {
			btnError.setText("Fehlerhaft (" + (importedPatients != null ? importedPatients.size() : 0) + ")");
		}
	}

	private void setCompareField(Label importLbl, Label existingLbl, String importVal, String existingVal, Color match,
			Color diff) {
		existingLbl.setText(existingVal != null ? existingVal : "");
		boolean equal = Objects.equals(importVal, existingVal);
		importLbl.setForeground(equal ? match : diff);
		existingLbl.setForeground(equal ? match : diff);
	}

	private void setComparePhoneField(Label importLbl, Label existingLbl, String importVal, String existingVal,
			Color match, Color diff) {
		existingLbl.setText(existingVal != null ? existingVal : "");

		String normalizedImport = normalizePhoneNumber(importVal);
		String normalizedExisting = normalizePhoneNumber(existingVal);
		boolean equal = Objects.equals(normalizedImport, normalizedExisting);

		importLbl.setForeground(equal ? match : diff);
		existingLbl.setForeground(equal ? match : diff);
	}

	private String normalizePhoneNumber(String value) {
		if (value == null) {
			return "";
		}
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber number = phoneUtil.parse(value, "CH");

			return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL).replaceAll("[^0-9]", "");
		} catch (NumberParseException e) {
			LoggerFactory.getLogger(getClass()).warn("Konnte Telefonnummer nicht parsen: {}", value, e);
			return value.replaceAll("[^+0-9]", "");
		}
	}

	private void createImportedArticleViewer(Composite parent) {
		Composite cImportedArticles = new Composite(parent, SWT.NONE);
		cImportedArticles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cImportedArticles.setLayout(new GridLayout(1, false));

		setCompositeTitle(cImportedArticles, "Importierte Artikel");

		Composite tableComposite = new Composite(cImportedArticles, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableComposite.setLayout(tcLayout);

		tableViewerImportedArticles = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.MULTI);
		Table table = tableViewerImportedArticles.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewerImportedArticles.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn tvcArticle = new TableViewerColumn(tableViewerImportedArticles, SWT.NONE);
		TableColumn tblclmnArticle = tvcArticle.getColumn();
		tcLayout.setColumnData(tblclmnArticle, new ColumnWeightData(30, 400, true));
		tblclmnArticle.setText(Messages.Core_Article);
		tvcArticle.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
			ImportedArticleRow row = (ImportedArticleRow) element;
			if (row.article != null) {
				return row.article.getLabel();
			}
			return "GTIN " + row.gtin + " (nicht gefunden)"; //$NON-NLS-1$
		}));

		TableViewerColumn tvcAmount = new TableViewerColumn(tableViewerImportedArticles, SWT.NONE);
		TableColumn tblclmnAmount = tvcAmount.getColumn();
		tcLayout.setColumnData(tblclmnAmount, new ColumnWeightData(10, 80, true));
		tblclmnAmount.setText(Messages.Core_Count);
		tvcAmount.setLabelProvider(ColumnLabelProvider
				.createTextProvider(element -> String.valueOf(((ImportedArticleRow) element).amount)));
	}

	private void updateImportedArticlesTable(PatientImportData data) {
		if (tableViewerImportedArticles == null) {
			return;
		}
		if (data == null) {
			tableViewerImportedArticles.setInput(null);
			return;
		}
		List<ImportedArticleRow> rows = data.articleGtinsWithAmount.entrySet().stream().map(entry -> {
			IArticle article = codeElementService.findArticleByGtin(entry.getKey()).orElse(null);
			return new ImportedArticleRow(entry.getKey(), entry.getValue(), article);
		}).collect(Collectors.toList());
		tableViewerImportedArticles.setInput(rows);
		tableViewerImportedArticles.refresh();
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

		// Medication type symbol
		TableViewerColumn tvcMedicationTypeSymbol = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		TableColumn tblclmntvcMedicationTypeSymbol = tvcMedicationTypeSymbol.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationTypeSymbol, new ColumnWeightData(2, 2, false));
		tvcMedicationTypeSymbol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				if (!(element instanceof IStockEntry entry)) {
					return Images.IMG_EMPTY_TRANSPARENT.getImage();
				}
				IArticle stockArticle = entry.getArticle();
				if (stockArticle == null || entry.getStock() == null || entry.getStock().getOwner() == null) {
					return Images.IMG_EMPTY_TRANSPARENT.getImage();
				}
				IPatient patient = entry.getStock().getOwner().asIPatient();
				if (patient == null) {
					return Images.IMG_EMPTY_TRANSPARENT.getImage();
				}

				List<IPrescription> lMedication = patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
						EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));

				for (IPrescription prescription : lMedication) {
					IArticle mediArticle = prescription.getArticle();
					if (mediArticle == null) {
						continue;
					}

					if (stockArticle.getId().equals(mediArticle.getId())) {
						switch (prescription.getEntryType()) {
						case FIXED_MEDICATION:
							return Images.IMG_FIX_MEDI.getImage();
						case RESERVE_MEDICATION:
							return Images.IMG_RESERVE_MEDI.getImage();
						case SYMPTOMATIC_MEDICATION:
							return Images.IMG_SYMPTOM_MEDI.getImage();
						default:
							return Images.IMG_EMPTY_TRANSPARENT.getImage();
						}
					}
				}
				return Images.IMG_EMPTY_TRANSPARENT.getImage();
			}
		});

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
	public List<IPatient> getSelectedImportedPatients() {
		return tableViewerImportedPatients.getStructuredSelection().toList();
	}

	public void deleteImportedPatient(IPatient patient) {
		PatientImportData data = importedPatientDataList.stream().filter(d -> d.patient == patient).findFirst()
				.orElse(null);
		if (data == null) {
			return;
		}
		if (data.blobId != null) {
			coreModelService.load(data.blobId, IBlob.class).ifPresent(coreModelService::delete);
		}
		importedPatients.remove(patient);
		importedPatientDataList.remove(data);
		if (selectedImportedPatient == patient) {
			resetErrorSelection();
		}
		refreshImportedPatientsTable();
	}

	@SuppressWarnings("unchecked")
	public List<IStockEntry> getSelectedStockEntries() {
		return tableViewerDetails.getStructuredSelection().toList();
	}

	@SuppressWarnings("unchecked")
	public List<IStock> getSelectedStocks() {
		return tableViewer.getStructuredSelection().toList();
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