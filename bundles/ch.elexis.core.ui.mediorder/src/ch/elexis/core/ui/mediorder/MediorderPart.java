package ch.elexis.core.ui.mediorder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMedicationService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.ui.e4.dnd.GenericObjectDropTarget;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.icons.Images;

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
	IMedicationService medicationService;

	private TableViewer tableViewer;
	private TableViewer tableViewerDetails;

	private StockComparator stockComparator;
	private MedicationComparator medicationComparator;
	private final DateTimeFormatter dateFormatter;

	private WritableValue<IStock> selectedDetailStock;
	
	private Map<IStock, Integer> imageStockStates = new HashMap<IStock, Integer>();

	public MediorderPart() {
		dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		selectedDetailStock = new WritableValue<>();
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Override
	public void refresh(Map<Object, Object> filterParameters) {
		Object firstElement = tableViewer.getStructuredSelection().getFirstElement();
		tableViewer.setInput(getPatientStocksWithStockEntry());
		tableViewer.refresh(true);
		if (tableViewer.contains(firstElement)) {
			tableViewer.setSelection(new StructuredSelection(firstElement));
		}
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		parent.setLayout(new FillLayout());

		stockComparator = new StockComparator();
		medicationComparator = new MedicationComparator();

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setSashWidth(5);
		createPatientorderListViewer(sashForm);
		createPatientorderDetailViewer(sashForm);
		addDragAndDrop();

		menuService.registerContextMenu(tableViewer.getTable(), "ch.elexis.core.ui.mediorder.popupmenu.viewer"); //$NON-NLS-1$
		menuService.registerContextMenu(tableViewerDetails.getTable(),
				"ch.elexis.core.ui.mediorder.popupmenu.viewerdetails"); //$NON-NLS-1$

		tableViewer.setInput(getPatientStocksWithStockEntry());
	}

	private void createPatientorderListViewer(Composite parent) {
		Composite cStockTable = new Composite(parent, SWT.NONE);
		cStockTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout = new TableColumnLayout();
		cStockTable.setLayout(tcLayout);

		tableViewer = new TableViewer(cStockTable, SWT.FULL_SELECTION | SWT.NONE);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setComparator(stockComparator);
		tableViewer.addSelectionChangedListener((SelectionChangedEvent event) -> {
			IStructuredSelection selection = event.getStructuredSelection();
			selectedDetailStock.setValue((IStock) selection.getFirstElement());
		});

		// order status
		TableViewerColumn tvcOrderState = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcOrderState.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public Image getImage(Object element) {
				IStock stock = (IStock) element;
				int number = getImageForStock(stock);
				return switch (number) {
				case 1 -> Images.IMG_BULLET_GREEN.getImage();
				case 2 -> Images.IMG_BULLET_YELLOW.getImage();
				case 3 -> Images.IMG_BULLET_BLUE.getImage();
				default -> null;
				};
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});

		TableColumn tblclmntvcOrderState = tvcOrderState.getColumn();
		tcLayout.setColumnData(tblclmntvcOrderState, new ColumnPixelData(20, true, true));
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
		tcLayout.setColumnData(tblclmntvcPatientNumber, new ColumnPixelData(70, true, true));
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
		tcLayout.setColumnData(tblclmntvcPatientLastName, new ColumnPixelData(110, true, true));
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
		tcLayout.setColumnData(tblclmntvcPatientFirstName, new ColumnPixelData(110, true, true));
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
		tcLayout.setColumnData(tblclmntvcPatientBirthdate, new ColumnPixelData(90, true, true));
		tblclmntvcPatientBirthdate.setText(Messages.Core_Enter_Birthdate);
		tblclmntvcPatientBirthdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(4);
				refresh();
			}
		});

	}

	private void createPatientorderDetailViewer(Composite parent) {
		// PatientDetails
		Composite cDetails_table = new Composite(parent, SWT.NONE);
		cDetails_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout_cDetails = new TableColumnLayout();
		cDetails_table.setLayout(tcLayout_cDetails);

		tableViewerDetails = new TableViewer(cDetails_table, SWT.FULL_SELECTION | SWT.MULTI);
		Table tableDetails = tableViewerDetails.getTable();
		tableDetails.setHeaderVisible(true);
		tableViewerDetails.setContentProvider(ArrayContentProvider.getInstance());
		selectedDetailStock.addChangeListener(sel -> {
			IStock stock = selectedDetailStock.getValue();
			//Represents inactive PEA order
			List<IStockEntry> lFilteredStocks = (stock != null)
					? stock.getStockEntries().stream().filter(s -> s.getMaximumStock() != 0 || s.getMinimumStock() != 0)
							.toList()
					: null;
			tableViewerDetails.setInput(lFilteredStocks);
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
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationOrdered, new ColumnPixelData(130, true, true));
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
		tcLayout_cDetails.setColumnData(tblclmntvcMedication, new ColumnPixelData(180, true, true));
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
			List<IPrescription> lMedication = patient.getMedication(null);
			for (IPrescription prescription : lMedication) {
				if (prescription.getArticle().equals(entry.getArticle())) {
					return prescription.getDosageInstruction();
				}
			}
			return "";
		}));
		TableColumn tblclmntvcMedicationDosage = tvcMedicationDosage.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationDosage, new ColumnPixelData(80, true, true));
		tblclmntvcMedicationDosage.setText(Messages.Core_Dosage);

		// medication no days consumption per dosage
		TableViewerColumn tvcMediorderEntryOutreach = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMediorderEntryOutreach.setLabelProvider(
				ColumnLabelProvider.createTextProvider(MediorderPartUtil::createMediorderEntryOutreachLabel));
		TableColumn tblclmntvcMedicationAmountDay = tvcMediorderEntryOutreach.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmountDay, new ColumnPixelData(120, true, true));
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
				return new TextCellEditor(tableViewerDetails.getTable());
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
				IStockEntry entry = (IStockEntry) element;
				String amount = (String) value;
				entry.setMinimumStock(Integer.parseInt(amount));
				coreModelService.save(entry);
				tableViewerDetails.refresh(true);
				updateStockImageState(entry.getStock());
			}

		});
		TableColumn tblclmntvcMedicationAmount = tvcMedicationAmount.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmount, new ColumnPixelData(110, true, true));
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
				return new TextCellEditor(tableViewerDetails.getTable());
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
				IStockEntry entry = (IStockEntry) element;
				String amount = (String) value;
				entry.setMaximumStock(Integer.parseInt(amount));
				coreModelService.save(entry);
				tableViewerDetails.refresh(true);
				updateStockImageState(entry.getStock());
			}

		});
		TableColumn tblclmntvcMedicationClearance = tvcMedicationClearance.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationClearance, new ColumnPixelData(110, true, true));
		tblclmntvcMedicationClearance.setImage(Images.IMG_TICK.getImage());
		tblclmntvcMedicationClearance.setText(Messages.Mediorder_approved);
		tblclmntvcMedicationClearance.setToolTipText(Messages.Mediorder_approved_Tooltip);
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

			int number1 = getImageForStock(ts1);
			int number2 = getImageForStock(ts2);

			switch (propertyIndex) {
			case 0 -> {
				return Objects.compare(number1, number2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
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
		updateStockImageState(stock);
	}

	private List<IStock> getPatientStocksWithStockEntry() {
		IQuery<IStock> query = coreModelService.getQuery(IStock.class);
		query.and("id", COMPARATOR.LIKE, "PatientStock-%");
		// Represents inactive PEA order
		return query.execute().stream().filter(stock -> !stock.getStockEntries().isEmpty())
				.filter(stock -> stock.getStockEntries().stream()
						.anyMatch(entry -> entry.getMaximumStock() != 0 || entry.getMinimumStock() != 0))
				.toList();
	}

	@SuppressWarnings("unchecked")
	public List<IStockEntry> getSelectedStockEntries() {
		return tableViewerDetails.getStructuredSelection().toList();
	}

	public IStock getSelectedStock() {
		return selectedDetailStock.getValue();
	}
	
	private void updateStockImageState(IStock stock) {
		int state = calculateStockState(stock);
		imageStockStates.put(stock, state);
		tableViewer.refresh();
	}

	private int calculateStockState(IStock stock) {
		int number = 0;
		for (IStockEntry entry : stock.getStockEntries()) {

			MediorderEntryState entryState = MediorderPartUtil.determineState(entry);
			number = switch (entryState) {
			case IN_STOCK -> 1;
			case ORDERED, PARTIALLY_ORDERED, PARTIALLY_IN_STOCK -> 2;
			case AWAITING_REQUEST, REQUESTED, PARTIALLY_REQUESTED, INVALID -> 3;
			default -> number;
			};
		}
		return number;
	}

	private int getImageForStock(IStock stock) {
		return imageStockStates.computeIfAbsent(stock, this::calculateStockState);
	}

}