package ch.elexis.core.ui.mediorder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
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
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import at.medevit.ch.artikelstamm.IArtikelstammItem;
import at.medevit.ch.artikelstamm.model.service.ArtikelstammModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;

public class MediorderView implements IRefreshablePart {
	
	private TableViewer tableViewer, tableViewerDetails;
	private Table table, tableDetails;
	private StockComparator stockComparator;
	private MedicationComparator medicationComparator;
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	@Inject
	private EPartService partService;
	
	@Inject
	IEventBroker eventBroker;

	@Inject
	IContextService contextService;

	@Override
	public void refresh(Map<Object, Object> filterParameters) {
		tableViewer.setInput(getPatientStocksWithStockEntry());
		tableViewer.refresh(true);
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(1, false));
		
		Composite cStockTable = new Composite(composite, SWT.NONE);
		cStockTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout = new TableColumnLayout();
		cStockTable.setLayout(tcLayout);

		tableViewer = new TableViewer(cStockTable, SWT.FULL_SELECTION | SWT.NONE);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		stockComparator = new StockComparator();
		tableViewer.setComparator(stockComparator);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = event.getStructuredSelection();
				Object sel = selection.getFirstElement();
				if (sel instanceof IStock) {
					IStock stock = (IStock) sel;
					List<IStockEntry> lStocks = StockServiceHolder.get().findAllStockEntriesForStock(stock);
					tableViewerDetails.setInput(lStocks);
					refresh();
				}

			}
		});

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

		TableViewerColumn tvcPatientLastName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientLastName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				return stock.getOwner().getLastName();
			}
		});
		
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

		TableViewerColumn tvcPatientFirstName = new TableViewerColumn(tableViewer, SWT.NONE);
		tvcPatientFirstName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStock stock = (IStock) element;
				return stock.getOwner().getFirstName();
			}
		});
		
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
		tableViewer.setInput(getPatientStocksWithStockEntry());

		Composite cMedicationTable = new Composite(parent, SWT.BORDER);
		cMedicationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cMedicationTable.setLayout(new GridLayout(1, false));

		Composite cDetails_table = new Composite(cMedicationTable, SWT.NONE);
		cDetails_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout_cDetails = new TableColumnLayout();
		cDetails_table.setLayout(tcLayout_cDetails);

		tableViewerDetails = new TableViewer(cDetails_table, SWT.FULL_SELECTION | SWT.NONE);
		tableDetails = tableViewerDetails.getTable();
		tableDetails.setHeaderVisible(true);
		tableDetails.setLinesVisible(true);
		tableViewerDetails.setContentProvider(ArrayContentProvider.getInstance());
		medicationComparator = new MedicationComparator();
		tableViewerDetails.setComparator(medicationComparator);
		tableViewerDetails.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (!selection.isEmpty()) {
					Object selectedElement = selection.getFirstElement();
					if (selectedElement instanceof IStockEntry) {
						IStockEntry entry = (IStockEntry) selectedElement;
						IOrderEntry orderEntry = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(entry);
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
			}
		});

		TableViewerColumn tvcMedication = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedication.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStockEntry entry = (IStockEntry) element;
				return entry.getArticle().getLabel();
			}
		});

		TableColumn tblclmntvcMedication = tvcMedication.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedication, new ColumnPixelData(110, true, true));
		tblclmntvcMedication.setText("Medikament");
		tblclmntvcMedication.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				medicationComparator.setColumn(0);
				refresh();
			}
		});

		TableViewerColumn tvcMedicationPos = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationPos.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String dosage = "";

				IStockEntry entry = (IStockEntry) element;
				IPatient patient = entry.getStock().getOwner().asIPatient();
				List<IPrescription> lMedication = patient.getMedication(null);
				for (IPrescription prescription : lMedication) {
					if (prescription.getArticle().equals(entry.getArticle())) {
						dosage = prescription.getDosageInstruction();
					}
				}
				return dosage;
			}
		});

		TableColumn tblclmntvcMedicationPos = tvcMedicationPos.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationPos, new ColumnPixelData(110, true, true));
		tblclmntvcMedicationPos.setText("Dosis");

		TableViewerColumn tvcMedicationAmountDay = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationAmountDay.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String dosage = "";
				int amountOfOne = 0;
				int resultDays = 0;

				IStockEntry entry = (IStockEntry) element;
				IPatient patient = entry.getStock().getOwner().asIPatient();
				List<IPrescription> lMedication = patient.getMedication(null);
				for (IPrescription prescription : lMedication) {
					if (prescription.getArticle().equals(entry.getArticle())) {
						dosage = prescription.getDosageInstruction();
						for (int i = 0; i < dosage.length(); i++) {
							if (String.valueOf(dosage.charAt(i)).equals("1")) {
								amountOfOne++;
							}
						}
						resultDays = entry.getArticle().getPackageSize() / amountOfOne;
					}
				}
				return String.valueOf(resultDays) + " Tage";
			}
		});

		TableColumn tblclmntvcMedicationAmountDay = tvcMedicationAmountDay.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmountDay, new ColumnPixelData(110, true, true));
		tblclmntvcMedicationAmountDay.setText("Verbrauchsdauer");

		TableViewerColumn tvcMedicationAmount = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationAmount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IStockEntry entry = (IStockEntry) element;
				return String.valueOf(entry.getMinimumStock());
			}
		});
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
				CoreModelServiceHolder.get().save(entry);
				tableViewerDetails.refresh(true);
			}

		});

		TableColumn tblclmntvcMedicationAmount = tvcMedicationAmount.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationAmount, new ColumnPixelData(110, true, true));
		tblclmntvcMedicationAmount.setText("Vorgesehen");

		TableViewerColumn tvcMedicationOrdered = new TableViewerColumn(tableViewerDetails, SWT.NONE);
		tvcMedicationOrdered.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				    if (!(element instanceof IStockEntry)) {
					return "";
				    }

				    IStockEntry entry = (IStockEntry) element;
				    return Optional.ofNullable(OrderServiceHolder.get().findOpenOrderEntryForStockEntry(entry))
				            .map(orderEntry -> String.valueOf(orderEntry.getAmount()))
				            .orElse("");
			}
		});

		TableColumn tblclmntvcMedicationOrdered = tvcMedicationOrdered.getColumn();
		tcLayout_cDetails.setColumnData(tblclmntvcMedicationOrdered, new ColumnPixelData(110, true, true));
		tblclmntvcMedicationOrdered.setText("Bestellt");
		tableViewerDetails.setInput(null);

		Composite cButtons = new Composite(cMedicationTable, SWT.NONE);
		cButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		cButtons.setLayout(new GridLayout(2, false));

		Button btnFinischOrder = new Button(cButtons, SWT.NONE);
		btnFinischOrder.setText("Abschliessen und verrechnen");

		Button btnPartlyFinischOrder = new Button(cButtons, SWT.NONE);
		btnPartlyFinischOrder.setText("Teilweise abschliessen");

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(types);

		source.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {

			}
		});

		DropTarget target = new DropTarget(tableDetails, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					String data = (String) event.data;

					IPatient patient = ContextServiceHolder.get().getActivePatient().get();
					Optional<IStock> stock = StockServiceHolder.get().getPatientStock(patient);

					IQuery<IArtikelstammItem> query = ArtikelstammModelServiceHolder.get()
							.getQuery(IArtikelstammItem.class);
					query.and("dscr", COMPARATOR.EQUALS, data);
					query.and("gtin", COMPARATOR.NOT_EQUALS, null);

					IArticle article = query.execute().get(0);
					if (!stock.isPresent()) {
						StockServiceHolder.get().setEnablePatientStock(patient, true);
						stock = StockServiceHolder.get().getPatientStock(patient);
					}

					IStockEntry stockentry = StockServiceHolder.get().findStockEntryForArticleInStock(stock.get(),
							article);
					if (stockentry != null) {
						int value = stockentry.getMinimumStock() + 1;
						stockentry.setMinimumStock(value);
						CoreModelServiceHolder.get().save(stockentry);
					} else {
						IStockEntry entry = StockServiceHolder.get().storeArticleInStock(stock.get(), article);
						entry.setCurrentStock(0);
						entry.setMinimumStock(1);
						CoreModelServiceHolder.get().save(entry);
					}
					refresh();

				}
			}
		});
	}

	private List<IStock> getPatientStocksWithStockEntry() {
		List<IStock> lStockWithEntry = new ArrayList<IStock>();
		IQuery<IStock> query = CoreModelServiceHolder.get().getQuery(IStock.class);
		query.and("id", COMPARATOR.LIKE, "PatientStock-%");

		List<IStock> lStock = query.execute();
		for (IStock stock : lStock) {
			if (!StockServiceHolder.get().findAllStockEntriesForStock(stock).isEmpty())
				lStockWithEntry.add(stock);
		}
		return lStockWithEntry;
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
			case 0:
				String patientNr1 = ts1.getCode();
				String patientNr2 = ts2.getCode();
				return Objects.compare(patientNr1, patientNr2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			case 1:
				String patientName1 = ts1.getOwner().getLastName();
				String patientName2 = ts2.getOwner().getLastName();
				return Objects.compare(patientName1, patientName2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			case 2:
				String patientFirstName1 = ts1.getOwner().getFirstName();
				String patientFirstName2 = ts2.getOwner().getFirstName();
				return Objects.compare(patientFirstName1, patientFirstName2,
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			case 3:
				LocalDateTime birthDate1 = ts1.getOwner().getDateOfBirth();
				return birthDate1.compareTo(ts2.getOwner().getDateOfBirth()) * direction;
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
				return Objects.compare(articleName1, articleName2,
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			}

			return super.compare(viewer, o1, o2);
		}
	}

}
