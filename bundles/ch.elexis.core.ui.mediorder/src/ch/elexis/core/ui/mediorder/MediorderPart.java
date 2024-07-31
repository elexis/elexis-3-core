package ch.elexis.core.ui.mediorder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
		parent.setLayout(new GridLayout(1, false));

		stockComparator = new StockComparator();
		medicationComparator = new MedicationComparator();

		createPatientorderListViewer(parent);
		createPatientorderDetailViewer(parent);
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
		tblclmntvcPatientNumber.setText("Patient-Nr");
		tblclmntvcPatientNumber.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(0);
				refresh();
			}
		});

		// patient lastname
		TableViewerColumn tvcPatientLastName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientLastName
				.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IStock) e).getOwner().getLastName()));
		TableColumn tblclmntvcPatientLastName = tvcPatientLastName.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientLastName, new ColumnPixelData(110, true, true));
		tblclmntvcPatientLastName.setText("Name");
		tblclmntvcPatientLastName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(1);
				refresh();
			}
		});

		// patient firstname
		TableViewerColumn tvcPatientFirstName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientFirstName
				.setLabelProvider(ColumnLabelProvider.createTextProvider(e -> ((IStock) e).getOwner().getFirstName()));
		TableColumn tblclmntvcPatientFirstName = tvcPatientFirstName.getColumn();
		tcLayout.setColumnData(tblclmntvcPatientFirstName, new ColumnPixelData(110, true, true));
		tblclmntvcPatientFirstName.setText("Vorname");
		tblclmntvcPatientFirstName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(2);
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
		tblclmntvcPatientBirthdate.setText("Geburtsdatum");
		tblclmntvcPatientBirthdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stockComparator.setColumn(3);
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

		// medication details
		TableViewerColumn tvcMedication = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedication.setLabelProvider(
				ColumnLabelProvider.createTextProvider(e -> ((IStockEntry) e).getArticle().getLabel()));
		TableColumn tblclmntvcMedication = tvcMedication.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedication, new ColumnPixelData(180, true, true));
		tblclmntvcMedication.setText("Medikament");
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
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationDosage, new ColumnPixelData(60, true, true));
		tblclmntvcMedicationDosage.setText("Dosis");

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
			}

		});
		TableColumn tblclmntvcMedicationClearance = tvcMedicationClearance.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationClearance, new ColumnPixelData(60, true, true));
		tblclmntvcMedicationClearance.setImage(Images.IMG_TICK.getImage());
		tblclmntvcMedicationClearance.setText("Freigabe");
		tblclmntvcMedicationClearance.setToolTipText("Anzahl zur Anforderung freigegeben");

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
			}

		});
		TableColumn tblclmntvcMedicationAmount = tvcMedicationAmount.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmount, new ColumnPixelData(70, true, true));
		tblclmntvcMedicationAmount.setText("Angefordert");
		tblclmntvcMedicationAmount.setImage(Images.IMG_ACHTUNG.getImage());
		tblclmntvcMedicationAmount.setToolTipText("Anzahl angefordert");

		// medication no days consumption per dosage
		TableViewerColumn tvcMediorderEntryOutreach = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMediorderEntryOutreach.setLabelProvider(
				ColumnLabelProvider.createTextProvider(MediorderPartUtil::createMediorderEntryOutreachLabel));
		TableColumn tblclmntvcMedicationAmountDay = tvcMediorderEntryOutreach.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmountDay, new ColumnPixelData(90, true, true));
		tblclmntvcMedicationAmountDay.setText("Verbrauchsdauer");

		// MediorderEntryState
		TableViewerColumn tvcMediorderEntryState = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		TableColumn tblclmntvcMedicationOrdered = tvcMediorderEntryState.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationOrdered, new ColumnPixelData(130, true, true));
		tblclmntvcMedicationOrdered.setText("Bestellt");
		tblclmntvcMedicationOrdered.setImage(Images.IMG_PERSPECTIVE_ORDERS.getImage());
		tblclmntvcMedicationOrdered.setToolTipText("Anzahl bestellt");
		tvcMediorderEntryState.setLabelProvider(
				ColumnLabelProvider.createTextProvider(MediorderPartUtil::createMediorderEntryStateLabel));
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

			switch (propertyIndex) {
			case 0 -> {
				Integer patientNr1 = Integer.valueOf(ts1.getId().substring(13));
				Integer patientNr2 = Integer.valueOf(ts2.getId().substring(13));
				return Objects.compare(patientNr1, patientNr2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			}
			case 1 -> {
				String patientName1 = ts1.getOwner().getLastName();
				String patientName2 = ts2.getOwner().getLastName();
				return Objects.compare(patientName1, patientName2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			}
			case 2 -> {
				String patientFirstName1 = ts1.getOwner().getFirstName();
				String patientFirstName2 = ts2.getOwner().getFirstName();
				return Objects.compare(patientFirstName1, patientFirstName2,
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			}
			case 3 -> {
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

}