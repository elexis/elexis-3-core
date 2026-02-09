package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.serial.Connection;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.constants.OrderConstants;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.OrderEntryComparators;
import ch.elexis.core.ui.util.OrderManagementActionFactory;
import ch.elexis.core.ui.util.OrderManagementHelper;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.util.TableSortController;
import ch.elexis.core.ui.util.dnd.OrderDropReceiver;
import ch.elexis.core.ui.views.provider.CompletedOrderTableLabelProvider;
import ch.elexis.core.ui.views.provider.EntryTableLabelProvider;
import ch.elexis.core.ui.views.provider.GenericOrderEditingSupport;
import ch.elexis.core.ui.views.provider.GenericOrderEditingSupport.EditingColumnType;
import ch.elexis.core.ui.views.provider.OrderTableLabelProvider;
import jakarta.inject.Inject;

public class OrderManagementView extends ViewPart implements IRefreshable {

	public static final String ID = "ch.elexis.OrderManagementView"; //$NON-NLS-1$

	private Composite topComposite;
	private Composite middleComposite;
	private Composite leftComposite;
	private Composite createdComposite;
	private Label cartIcon;
	private Label titleLabel;
	private Label createdLabel;
	private TableViewerColumn[] mainColumns;
	private Label statusLabel;
	private Label statusValue;

	public TableViewer tableViewer;
	private TableViewer orderTable;

	private Action checkInAction;
	private IOrder actOrder;
	private OrderManagementActionFactory actionFactory;
	public Button orderButton;
	private boolean showDeliveredColumn = false;

	private Composite dispatchedComposite;
	private Label dispatchedLabelTitle;
	private Label dispatchedLabelIcon;
	private Label dispatchedLabelState;

	private Composite bookedComposite;
	private Label bookedLabelTitle;
	private Label bookedLabelIcon;
	private Label bookedLabelState;
	private Label createdLabelState;
	private Composite completedContainer;
	private boolean isUIUpdating = false;
	private boolean mouseListenersRegistered = false;
	private final List<TableViewer> completedYearViewers = new ArrayList<>();
	public Button selectAllChk;
	private TableViewer plainViewer;
	public CheckboxTableViewer checkboxViewer;
	private Composite tableArea;
	private StackLayout tableStack;

	private Composite mainComposite;
	private Composite headerBar;

	private TableSortController plainSorter;
	private TableSortController checkboxSorter;
	private Button addArticleButton;

	private Composite rightListComposite;
	private ScrolledComposite scrolledComposite;
	private ScrolledComposite rightScrollComposite;

	private boolean hasFocus = false;
	private boolean showAllYears = false;
	private static final Logger logger = LoggerFactory.getLogger(OrderManagementView.class);
	private static final String BARCODE_CONSUMER_KEY = "barcodeInputConsumer"; //$NON-NLS-1$

	public static final String BarcodeScanner_COMPORT = "barcode/Symbol/port"; //$NON-NLS-1$
	private static final GridData FILL_HORIZONTAL = new GridData(SWT.FILL, SWT.CENTER, true, false);
	private static final GridData FILL_BOTH = new GridData(SWT.FILL, SWT.FILL, true, true);
	private static final GridData FIXED_WIDTH_50 = new GridData(50, SWT.DEFAULT);
	private static final GridData FIXED_WIDTH_75 = new GridData(75, SWT.DEFAULT);
	private Map<Integer, Boolean> expandedStates = new HashMap<>();
	private final Map<IOrderEntry, Integer> pendingDeliveredValues = new HashMap<>();
	private static boolean barcodeScannerActivated = false;
	private boolean isDeliveryEditMode = false;
	private Image orderButtonCustomImage;
	private GenericObjectDropTarget plainDropTarget;
	private GenericObjectDropTarget checkboxDropTarget;
	public GenericObjectDropTarget dropTarget;
	private final Gson gson = new Gson();


	@Inject
	private IOrderService orderService;

	public Composite getCompletedContainer() {
		return completedContainer;
	}

	public void setCompletedContainer(Composite completedContainer) {
		this.completedContainer = completedContainer;
	}

	public boolean isDeliveryEditMode() {
		return isDeliveryEditMode;
	}

	public void setDeliveryEditMode(boolean enabled) {
		this.isDeliveryEditMode = enabled;
	}

	public static void setBarcodeScannerActivated(boolean enabled) {
		barcodeScannerActivated = enabled;
	}

	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IStock.class.equals(clazz) || IOrder.class.equals(clazz) || IArticle.class.equals(clazz)) {
			if (actOrder != null) {
				refresh();
				loadOpenOrders();
			}
			if (orderTable != null) {
				loadOpenOrders();
			}

		}
	}
	
	private RefreshingPartListener focusListener = new RefreshingPartListener(this) {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				hasFocus = true;
				String valueToSet = OrderManagementView.class.getName();
				ContextServiceHolder.get().getRootContext().setNamed(BARCODE_CONSUMER_KEY, valueToSet);
				CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				hasFocus = false;
				ContextServiceHolder.get().getRootContext().setNamed(BARCODE_CONSUMER_KEY, null);
				OrderManagementUtil.deactivateBarcodeScanner();
			}
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				CodeSelectorHandler handler = CodeSelectorHandler.getInstance();
				if (handler.getCodeSelectorTarget() == dropTarget) {
					handler.removeCodeSelectorTarget();
				}
			}
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				CodeSelectorHandler handler = CodeSelectorHandler.getInstance();
				if (handler.getCodeSelectorTarget() == dropTarget) {
					handler.removeCodeSelectorTarget();
				}
			}
		}
	};

	@Inject
	@org.eclipse.e4.core.di.annotations.Optional
	public void barcodeEvent(@UIEventTopic(ElexisEventTopics.BASE_EVENT + "barcodeinput") Object event,
			IContextService contextService) {
		if (!hasFocus) {
			return;
		}
		if (event instanceof IArticle scannedArticle && OrderManagementView.class.getName()
				.equals(contextService.getNamed(BARCODE_CONSUMER_KEY).orElse(null))) {
			if (actOrder == null) {

				return;
			}

			boolean activateDeliveryMode = false;

			Optional<IOrderEntry> matchingEntry = actOrder.getEntries().stream()
					.filter(entry -> entry.getArticle() != null
							&& entry.getArticle().getCode().equals(scannedArticle.getCode()))
					.findFirst();
			if (matchingEntry.isPresent()) {
				IOrderEntry entry = matchingEntry.get();
				if (entry.getState() == OrderEntryState.OPEN) {
					int oldAmount = entry.getAmount();
					int newAmount = oldAmount + 1;
					updateOrderEntry(entry, newAmount);
				} else {
					activateDeliveryMode = true;
					int currentPending = pendingDeliveredValues.getOrDefault(entry, 0);
					int newPending = currentPending + 1;
					int ordered = entry.getAmount();
					int delivered = entry.getDelivered();
					int sumDelivered = delivered + newPending;
					if (sumDelivered > ordered) {
						String articleName = entry.getArticle() != null ? entry.getArticle().getLabel()
								: "Unbekannter Artikel"; //$NON-NLS-1$
						boolean confirm = MessageDialog.openQuestion(getSite().getShell(),
								Messages.OrderManagement_Overdelivery_Title,
								MessageFormat.format(Messages.OrderManagement_Overdelivery_Message, delivered,
										newPending, sumDelivered, ordered, articleName));
						if (!confirm) {
							return;
						}
					}
					pendingDeliveredValues.put(entry, newPending);
				}

			}else {

				boolean isOrderOpen = actOrder.getEntries().stream()
						.allMatch(e -> e.getState() == OrderEntryState.OPEN);

				if (isOrderOpen) {
					IStock stock = StockServiceHolder.get()
							.getMandatorDefaultStock(ContextServiceHolder.get().getActiveMandator().get().getId());
					IOrderEntry newEntry = actOrder.addEntry(scannedArticle, stock, null, 1);
					newEntry.setOrder(actOrder);
					newEntry.setArticle(scannedArticle);
					newEntry.setAmount(1);
					newEntry.setState(OrderEntryState.OPEN);
					actOrder.getEntries().add(newEntry);
					CoreModelServiceHolder.get().save(newEntry);
					orderService.getHistoryService().logEdit(actOrder, newEntry, 0, 1);
				}
			}
			loadOrderDetails(actOrder);
			updateOrderDetails(actOrder);
			if (orderTable != null && !orderTable.getControl().isDisposed()) {
				orderTable.refresh();
			}
			OrderManagementHelper.updateSelectAllCheckbox(this, pendingDeliveredValues);
			if (activateDeliveryMode) {
				setDeliveryEditMode(true);
				OrderManagementUtil.setCheckboxColumnVisible(this, true);
			}
		}
	}

	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	void activeOrder(IOrder order) {
		if (order != null) {
			actOrder = order;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
	    parent.setLayout(new GridLayout(2, false));
	    getSite().getPage().addPartListener(focusListener);
	    createHeaderUI(parent);
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createTableUI(sashForm);
		createRightUI(sashForm);
		registerTableListeners(null);
		registerButtonListeners();
		registerMouseWheelListeners(null);
		loadOpenOrders();
		makeActions();
		loadCompletedOrders(completedContainer);
		actionFactory.createContextMenu(tableViewer, orderTable);
		updateUI();

		sashForm.setWeights(new int[] { 2, 1 });
	}

	public boolean isShowDeliveredColumn() {
		return showDeliveredColumn;
	}

	public void setShowDeliveredColumn(boolean showDeliveredColumn) {
		this.showDeliveredColumn = showDeliveredColumn;
	}

	public boolean isBarcodePortAvailable() {
		String port = LocalConfigService.get(BarcodeScanner_COMPORT, StringUtils.EMPTY);
		if (StringUtils.isBlank(port)) {
			return false;
		}
		String[] available = Connection.getComPorts();
		return Arrays.asList(available).contains(port);
	}

	public void refreshTables() {
		loadOpenOrders();
		loadCompletedOrders(completedContainer);
		refresh();
	}

	public int determineEditableColumn(IOrderEntry entry) {
		if (entry.getState() == OrderEntryState.OPEN)
			return OrderConstants.OrderTable.ORDERED;
		if (entry.getState() == OrderEntryState.ORDERED || entry.getState() == OrderEntryState.PARTIAL_DELIVER)
			return OrderConstants.OrderTable.DELIVERED;
		return -1;
	}

	public void updateOrderEntry(IOrderEntry entry, int newValue) {
	    if (entry.getState() == OrderEntryState.OPEN) {
	        int oldValue = entry.getAmount();
	        entry.setAmount(newValue);
	        orderService.getHistoryService().logEdit(actOrder, entry, oldValue, newValue);
	    } else {
	        OrderManagementUtil.saveSingleDelivery(entry, newValue, orderService);
	    }
	    CoreModelServiceHolder.get().save(entry);
	    if (actOrder != null && actOrder.getId() != null) {
	        historyCache.remove(actOrder.getId());
	    }
	    tableViewer.refresh(entry);
	}

	private void createHeaderUI(Composite parent) {
		topComposite = new Composite(parent, SWT.BORDER);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		topComposite.setLayout(new GridLayout(4, false));

		createImageComposite(topComposite);
		createLeftComposite(topComposite);
		createMiddleComposite(topComposite);
		createRightComposite(topComposite);
	}

	private void createImageComposite(Composite parent) {
		Composite imageComposite = new Composite(topComposite, SWT.NONE);
		GridData imageCompositeData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		imageComposite.setLayoutData(imageCompositeData);
		imageComposite.setLayout(new GridLayout(1, false));
		cartIcon = new Label(imageComposite, SWT.NONE);
		GridData cartIconData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		cartIcon.setLayoutData(cartIconData);
		cartIcon.setLayoutData(new GridData(80, 70));
	}

	private void createLeftComposite(Composite parent) {

		leftComposite = new Composite(topComposite, SWT.NONE);
		leftComposite.setLayoutData(FILL_BOTH);
		GridLayout leftLayout = new GridLayout(1, false);
		leftLayout.marginHeight = 0;
		leftLayout.marginTop = 0;
		leftLayout.marginBottom = 0;
		leftLayout.verticalSpacing = -5;
		leftComposite.setLayout(leftLayout);

		Composite upperLeftComposite = new Composite(leftComposite, SWT.NONE);
		upperLeftComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		upperLeftComposite.setLayout(new GridLayout(1, false));

		titleLabel = new Label(upperLeftComposite, SWT.NONE);
		titleLabel.setLayoutData(new GridData(180, 25));
		titleLabel.setText(StringUtils.EMPTY);
		titleLabel.setFont(createBoldFont(titleLabel));

		Composite lowerLeftComposite = new Composite(leftComposite, SWT.NONE);
		lowerLeftComposite.setLayoutData(FILL_BOTH);
		lowerLeftComposite.setLayout(new GridLayout(2, false));

		statusLabel = new Label(lowerLeftComposite, SWT.NONE);
		statusLabel.setText(Messages.OrderManagement_StatusLabel);
		statusLabel.setLayoutData(FIXED_WIDTH_50);

		statusValue = new Label(lowerLeftComposite, SWT.NONE);
		statusValue.setText(StringUtils.EMPTY);
		statusValue.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		statusValue.setFont(createBoldFont(statusValue));
		GridData statusValueData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		statusValue.setLayoutData(statusValueData);

	}

	private void createMiddleComposite(Composite parent) {
		middleComposite = new Composite(topComposite, SWT.NONE);
		middleComposite.setLayoutData(FILL_HORIZONTAL);
		GridLayout middleLayout = new GridLayout(1, false);
		middleLayout.marginHeight = 0;
		middleLayout.marginTop = 0;
		middleLayout.marginBottom = 0;
		middleLayout.verticalSpacing = -5;
		middleComposite.setLayout(middleLayout);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		createdComposite = new Composite(middleComposite, SWT.NONE);
		createdComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createdComposite.setLayout(new GridLayout(2, false));

		createdLabel = new Label(createdComposite, SWT.NONE);
		createdLabel.setText(Messages.OrderManagement_CreatedLabel);
		createdLabel.setLayoutData(FIXED_WIDTH_75);

		createdLabelState = new Label(createdComposite, SWT.NONE);
		createdLabelState.setText(StringUtils.EMPTY);
		createdLabelState.setLayoutData(gridData);

		dispatchedComposite = new Composite(middleComposite, SWT.NONE);
		dispatchedComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		dispatchedComposite.setLayout(new GridLayout(3, false));

		dispatchedLabelTitle = new Label(dispatchedComposite, SWT.NONE);
		dispatchedLabelTitle.setText(Messages.OrderManagement_DispatchedLabel);
		dispatchedLabelTitle.setLayoutData(FIXED_WIDTH_75);

		dispatchedLabelIcon = new Label(dispatchedComposite, SWT.NONE);
		dispatchedLabelIcon.setLayoutData(new GridData(20, 20));

		dispatchedLabelState = new Label(dispatchedComposite, SWT.NONE);
		dispatchedLabelState.setText(StringUtils.EMPTY);
		dispatchedLabelState.setLayoutData(gridData);

		bookedComposite = new Composite(middleComposite, SWT.NONE);
		bookedComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		bookedComposite.setLayout(new GridLayout(3, false));

		bookedLabelTitle = new Label(bookedComposite, SWT.NONE);
		bookedLabelTitle.setText(Messages.OrderManagement_BookedLabel);
		bookedLabelTitle.setLayoutData(FIXED_WIDTH_75);

		bookedLabelIcon = new Label(bookedComposite, SWT.NONE);
		bookedLabelIcon.setLayoutData(new GridData(20, 20));

		bookedLabelState = new Label(bookedComposite, SWT.NONE);
		bookedLabelState.setText(StringUtils.EMPTY);
		bookedLabelState.setLayoutData(gridData);
	}

	private void createRightComposite(Composite parent) {
		Composite rightComposite = new Composite(topComposite, SWT.NONE);
		rightComposite.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		rightComposite.setLayout(new GridLayout());
		orderButton = new Button(rightComposite, SWT.PUSH);
		orderButton.setText(Messages.OrderManagement_Button_Order);
		orderButton.setLayoutData(new GridData(140, 64));
		orderButton.setEnabled(false);
	}

	private void createTableUI(Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout(1, false));

		headerBar = new Composite(mainComposite, SWT.NONE);
		GridData headerGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		headerGD.heightHint = 30;
		headerGD.verticalIndent = 2;
		headerBar.setLayoutData(headerGD);
		headerBar.setLayout(new org.eclipse.swt.layout.FormLayout());

		selectAllChk = new Button(headerBar, SWT.CHECK);
		selectAllChk.setText(Messages.OrderManagement_FullyDelivered);
		selectAllChk.addListener(SWT.Selection,
				e -> OrderManagementHelper.applySelectAll(this, selectAllChk.getSelection(), pendingDeliveredValues));

		FormData fdChk = new FormData();
		fdChk.left = new FormAttachment(0, 5);

		int chkHeight = selectAllChk.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		fdChk.top = new FormAttachment(50, -chkHeight / 2);

		selectAllChk.setLayoutData(fdChk);

		addArticleButton = new Button(headerBar, SWT.NONE);
		Image plusImg = Images.IMG_NEW.getImage();
		addArticleButton.setImage(plusImg);
		addArticleButton.setToolTipText(Messages.OrderManagement_AddItem);

		FormData fdPlus = new FormData();
		fdPlus.right = new FormAttachment(100, -5);
		fdPlus.top = new FormAttachment(30, -plusImg.getBounds().height / 2);
		addArticleButton.setLayoutData(fdPlus);

		addArticleButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1 && actionFactory != null && actOrder != null) {
					actionFactory.handleAddItem();
				}
			}
		});

		addArticleButton.setVisible(false);

		tableArea = new Composite(mainComposite, SWT.NONE);
		tableArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableStack = new StackLayout();
		tableArea.setLayout(tableStack);

		createPlainTable();
		createCheckboxTable();

		tableViewer = plainViewer;
		tableStack.topControl = plainViewer.getControl();
		tableArea.layout(true, true);
	}

	private void createPlainTable() {
		int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		plainViewer = new TableViewer(tableArea, style);
		plainViewer.setUseHashlookup(true);

		Table tableControl = plainViewer.getTable();
		tableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableControl.setHeaderVisible(true);
		tableControl.setLinesVisible(true);

		String[] columnHeaders = { StringUtils.EMPTY, Messages.OrderManagement_Column_Status,
				Messages.OrderManagement_Column_Ordered,
				Messages.OrderManagement_Column_Delivered, Messages.Core_Add, Messages.OrderManagement_Column_Article,
				Messages.OrderManagement_Column_Supplier, Messages.OrderManagement_Column_Stock };
		int[] columnWidths = { 0, 0, 50, 60, 0, 190, 160, 50 };

		createTableColumns(plainViewer, columnHeaders, columnWidths, showDeliveredColumn);
		OrderManagementUtil.enableLastColumnFill(tableControl);
		plainSorter = new TableSortController(plainViewer);
		plainSorter.setDefaultSort(OrderConstants.OrderTable.ARTICLE, SWT.UP);
		TableColumn addCol = plainViewer.getTable().getColumn(OrderConstants.OrderTable.ADD);
		addCol.setResizable(false);
		addCol.setMoveable(false);
		addCol.setWidth(0);

		addEditingSupportForSupplierColumn(plainViewer);
		addEditingSupportForDeliveredColumn(plainViewer);
		addEditingSupportForOrderColumn(plainViewer);

		plainDropTarget = new GenericObjectDropTarget("ArtikelDropTarget", plainViewer.getControl(), //$NON-NLS-1$
				new OrderDropReceiver(this, orderService), false);
		plainDropTarget.registered(false);
		dropTarget = plainDropTarget;
		CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
	}

	private void createCheckboxTable() {
		int style = SWT.MULTI | SWT.CHECK | SWT.FULL_SELECTION;
		checkboxViewer = CheckboxTableViewer.newCheckList(tableArea, style);
		checkboxViewer.setUseHashlookup(true);

		Table tableControl = checkboxViewer.getTable();
		tableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableControl.setHeaderVisible(true);
		tableControl.setLinesVisible(true);

		String[] columnHeaders = { StringUtils.EMPTY, Messages.OrderManagement_Column_Status,
				Messages.OrderManagement_Column_Ordered, Messages.OrderManagement_Column_Delivered, Messages.Core_Add,
				Messages.OrderManagement_Column_Article, Messages.OrderManagement_Column_Supplier,
				Messages.OrderManagement_Column_Stock };

		int[] columnWidths = { 30, 50, 50, 60, 0, 190, 160, 50 };

		createTableColumns(checkboxViewer, columnHeaders, columnWidths, showDeliveredColumn);
		OrderManagementUtil.enableLastColumnFill(tableControl);
		checkboxSorter = new TableSortController(checkboxViewer);
		checkboxSorter.setDefaultSort(OrderConstants.OrderTable.ARTICLE, SWT.UP);
		TableColumn addCol = checkboxViewer.getTable().getColumn(OrderConstants.OrderTable.ADD);
		addCol.setResizable(false);
		addCol.setMoveable(false);
		addCol.setWidth(0);

		addEditingSupportForSupplierColumn(checkboxViewer);
		addEditingSupportForDeliveredColumn(checkboxViewer);
		addEditingSupportForOrderColumn(checkboxViewer);

		checkboxDropTarget = new GenericObjectDropTarget("ArtikelDropTarget", checkboxViewer.getControl(), //$NON-NLS-1$
				new OrderDropReceiver(this, orderService), false);
		checkboxDropTarget.registered(false);
		checkboxViewer.addCheckStateListener(e -> {
			IOrderEntry entry = (IOrderEntry) e.getElement();
			if (!isDeliveryEditMode) {
				boolean shouldBeChecked = pendingDeliveredValues.getOrDefault(entry, 0) > 0;
				if (checkboxViewer.getChecked(entry) != shouldBeChecked) {
					checkboxViewer.setChecked(entry, shouldBeChecked);
				}
				return;
			}
			if (!isEligibleForBooking(entry)) {
				checkboxViewer.setChecked(entry, false);
				return;
			}
			if (e.getChecked()) {
				int rest = Math.max(0, entry.getAmount() - entry.getDelivered());
				if (rest > 0) {
					pendingDeliveredValues.put(entry, rest);
				}
			} else {
				pendingDeliveredValues.remove(entry);
			}
			checkboxViewer.update(entry, null);
			if (!Messages.MedicationComposite_btnConfirm.equals(orderButton.getText())) {
				updateOrderStatus(actOrder);
			}
			OrderManagementHelper.updateSelectAllCheckbox(this, pendingDeliveredValues);

		});

		checkboxViewer.setCheckStateProvider(new org.eclipse.jface.viewers.ICheckStateProvider() {
			@Override
			public boolean isChecked(Object element) {
				IOrderEntry e = (IOrderEntry) element;
				return pendingDeliveredValues.getOrDefault(e, 0) > 0;
			}

			@Override
			public boolean isGrayed(Object element) {
				IOrderEntry e = (IOrderEntry) element;
				return !isEligibleForBooking(e);
			}
		});

		OrderManagementUtil.setCheckboxColumnVisible(this, false);
	}

	private void createRightUI(Composite parent) {
		rightScrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		rightScrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		rightScrollComposite.setExpandHorizontal(true);
		rightScrollComposite.setExpandVertical(true);

		rightListComposite = new Composite(rightScrollComposite, SWT.NONE);
		rightListComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		rightListComposite.setLayout(new GridLayout(1, false));
		rightScrollComposite.setContent(rightListComposite);

		Label rightTitle = new Label(rightListComposite, SWT.NONE);
		rightTitle.setText(Messages.OrderManagement_RightTitle);
		orderTable = new TableViewer(rightListComposite,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.HIDE_SELECTION);
		orderTable.setUseHashlookup(true);
		Table orderTableControl = orderTable.getTable();
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 200;
		orderTableControl.setLayoutData(gridData);
		orderTableControl.setHeaderVisible(true);
		orderTableControl.setLinesVisible(true);
		orderTableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		orderTableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		String[] columnOrderHeaders = { StringUtils.EMPTY, Messages.Core_Date, Messages.Core_Title };
		int[] columnOrderWidths = { 40, 90, 210 };
		createTableColumns(orderTable, columnOrderHeaders, columnOrderWidths, showDeliveredColumn);

		orderTable.setContentProvider(ArrayContentProvider.getInstance());
		orderTable.setLabelProvider(new OrderTableLabelProvider());
		Label completedTitle = new Label(rightListComposite, SWT.NONE);
		completedTitle.setText(Messages.OrderManagement_CompletedTitle);

		scrolledComposite = new ScrolledComposite(rightListComposite, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite scrollContent = new Composite(scrolledComposite, SWT.NONE);
		scrollContent.setLayout(new GridLayout(1, false));
		scrolledComposite.setContent(scrollContent);

		completedContainer = scrollContent;
		completedContainer.setLayout(new GridLayout(1, false));
		completedContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

	}

	private Font createBoldFont(Control control) {
		FontData[] fontData = control.getFont().getFontData();
	    for (FontData fd : fontData) {
	        fd.setStyle(SWT.BOLD);
	    }
		Font newFont = new Font(control.getDisplay(), fontData);
		control.addDisposeListener(e -> newFont.dispose());
		return newFont;
	}

	private void createTableColumns(TableViewer viewer, String[] titles, int[] widths, boolean showDelivered) {
		mainColumns = new TableViewerColumn[titles.length];
		for (int i = 0; i < titles.length; i++) {
			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
			mainColumns[i] = col;
			TableColumn column = col.getColumn();
			column.setText(titles[i]);
			column.setWidth(widths[i]);
			column.setResizable(true);
			column.setMoveable(true);
			if (i == OrderConstants.OrderTable.STATUS) {
				col.setLabelProvider(new OwnerDrawLabelProvider() {
					@Override
					protected void measure(Event event, Object element) {
						event.height = 25;
					}

					@Override
					protected void paint(Event event, Object element) {
						if (element instanceof IOrderEntry entry) {
							Image img = OrderManagementUtil.getEntryStatusIcon(entry);
							if (img != null && !img.isDisposed()) {
								int imgWidth = img.getBounds().width;
								int imgHeight = img.getBounds().height;
								int cellWidth = event.width;
								int style = event.widget.getStyle();
								int offset = (style & SWT.CHECK) != 0 ? 20 : 0;
								int x = event.x + offset + (cellWidth - imgWidth) / 2;
								int y = event.y + (event.height - imgHeight) / 2;
								event.gc.drawImage(img, x, y);
							}
						}
					}
				});
			} else {
				col.setLabelProvider(new EntryTableLabelProvider(i, showDelivered, this));
			}
		}
	}

	@Override
	public void refresh() {
		if (actOrder != null) {
			loadOrderDetails(actOrder);
			updateOrderDetails(actOrder);
		}
		updateCheckIn();
		updateUI();
	}

	public void reload() {
		loadOpenOrders();
		if (actOrder != null) {
			refresh();
		}
		if (completedContainer != null) {
			loadCompletedOrders();
			refresh();
		}
	}

	public void clearOrderDetailsView() {
		if (tableViewer != null && tableViewer.getContentProvider() != null && tableViewer.getControl() != null
				&& !tableViewer.getControl().isDisposed()) {
			tableViewer.setInput(java.util.Collections.emptyList());
			tableViewer.refresh();
		}

		titleLabel.setText(StringUtils.EMPTY);
		createdLabelState.setText(StringUtils.EMPTY);
		statusValue.setText(StringUtils.EMPTY);

		cartIcon.setImage(null);
		dispatchedLabelIcon.setImage(null);
		dispatchedLabelState.setText(StringUtils.EMPTY);
		bookedLabelIcon.setImage(null);
		bookedLabelState.setText(StringUtils.EMPTY);

		orderButton.setImage(null);
		orderButton.setText(StringUtils.EMPTY);
		orderButton.setEnabled(false);

	}

	public void selectOrderInHistory(IOrder order) {
		if (order == null) {
			return;
		}
		String id = order.getId();
		if (orderTable != null && !orderTable.getTable().isDisposed()) {
			@SuppressWarnings("unchecked")
			List<IOrder> openOrders = (List<IOrder>) orderTable.getInput();
			if (openOrders != null) {
				for (IOrder o : openOrders) {
					if (id.equals(o.getId())) {
						orderTable.setSelection(new StructuredSelection(o), true);
						orderTable.reveal(o);
						clearOtherSelections(orderTable);
						return;
					}
				}
			}
		}
		for (TableViewer tv : completedYearViewers) {
			if (tv == null || tv.getTable().isDisposed()) {
				continue;
			}
			@SuppressWarnings("unchecked")
			List<IOrder> yearOrders = (List<IOrder>) tv.getInput();
			if (yearOrders == null) {
				continue;
			}
			for (IOrder o : yearOrders) {
				if (id.equals(o.getId())) {
					tv.setSelection(new StructuredSelection(o), true);
					tv.reveal(o);
					clearOtherSelections(tv);
					return;
				}
			}
		}
	}

	@Override
	public void setFocus() {
		Control controlToFocus = (tableViewer != null) ? tableViewer.getControl() : topComposite;
		if (controlToFocus != null && !controlToFocus.isDisposed()) {
			controlToFocus.setFocus();
		}
	}

	public void updateCheckIn() {
		if (checkInAction == null) {
			return;
		}
		if (actOrder == null) {
			checkInAction.setEnabled(false);
			selectAllChk.setVisible(false);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_NoOrder);
		} else if (actOrder.isDone()) {
			checkInAction.setEnabled(false);
			selectAllChk.setVisible(false);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_Done);
		} else {
			checkInAction.setEnabled(true);
			selectAllChk.setVisible(true);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_Confirm);
		}
	}

	private void registerTableListeners(List<TableViewer> tableViewers) {
		if (orderTable != null && !orderTable.getTable().isDisposed()) {
			orderTable.addSelectionChangedListener(event -> {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IOrder selectedOrder = (IOrder) selection.getFirstElement();
				if (selectedOrder != null) {
					resetEditMode();
					setActOrder(selectedOrder);
					refresh();
					clearOtherSelections(orderTable);
				}
			});
		}

		if (tableViewers != null) {
			for (TableViewer yearTableViewer : tableViewers) {
				yearTableViewer.addSelectionChangedListener(event -> {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					IOrder selectedOrder = (IOrder) selection.getFirstElement();
					if (selectedOrder != null) {
						actionFactory.handleCompletedOrderSelection(selectedOrder);
					}
				});
			}
		}
	}

	private void registerButtonListeners() {
		orderButton.addListener(SWT.Selection, event -> handleOrderButtonClick());
	}

	public void handleOrderButtonClick() {
		OrderManagementUtil.handleOrderButtonClick(this, orderService, pendingDeliveredValues, actOrder);
	}

	private void registerMouseWheelListeners(Table tblYear) {
		if (!mouseListenersRegistered) {
			completedContainer.addListener(SWT.MouseWheel,
					e -> actionFactory.handleMouseWheelScroll(e, rightScrollComposite));
			scrolledComposite.addListener(SWT.MouseWheel,
					e -> actionFactory.handleMouseWheelScroll(e, scrolledComposite));
			if (orderTable != null) {
				orderTable.getTable().addListener(SWT.MouseWheel,
						e -> actionFactory.handleMouseWheelScroll(e, rightScrollComposite));
			}
			mouseListenersRegistered = true;
		}
		if (tblYear != null) {
			tblYear.addListener(SWT.MouseWheel, e -> actionFactory.handleMouseWheelScroll(e, rightScrollComposite));
		}
	}

	public void loadOpenOrders() {
		List<IOrder> orders = OrderManagementUtil.getOpenOrders();
		orderTable.setInput(orders);

		if (actOrder != null && orders.contains(actOrder)) {
			orderTable.setSelection(new StructuredSelection(actOrder), true);
			orderTable.reveal(actOrder);
			clearOtherSelections(orderTable);
		}

		registerMouseWheelListeners(null);
	}

	private void loadCompletedOrders() {
		loadCompletedOrders(completedContainer);
	}

	public void loadCompletedOrders(Composite completedContainer) {
		completedYearViewers.clear();
		for (Control child : completedContainer.getChildren()) {
			if (child instanceof ExpandableComposite oldSection) {

				Object yearObj = oldSection.getData("year"); //$NON-NLS-1$
				if (yearObj instanceof Integer oldYear) {
					expandedStates.put(oldYear, oldSection.isExpanded());
				}
			}
		}

		for (Control child : completedContainer.getChildren()) {
	        child.dispose();
	    }
		List<IOrder> orders = OrderManagementUtil.getCompletedOrders(showAllYears);
	    if (orders.isEmpty()) {
	        return;
	    }

	    Map<Integer, List<IOrder>> ordersByYear = orders.stream()
	        .collect(Collectors.groupingBy(o -> o.getTimestamp().getYear()));
	    List<Integer> sortedYears = new ArrayList<>(ordersByYear.keySet());
		sortedYears.sort(Collections.reverseOrder());

		if (!showAllYears && sortedYears.size() > 2) {
			sortedYears = sortedYears.subList(0, 2);
		}

	    for (Integer year : sortedYears) {
			ExpandableComposite completedSection = new ExpandableComposite(
	            completedContainer,
	            SWT.NONE,
					ExpandableComposite.TREE_NODE | ExpandableComposite.COMPACT | ExpandableComposite.FOCUS_TITLE
	        );
			completedSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			completedSection.setFont(createBoldFont(titleLabel));
			completedSection.setText(Messages.OrderManagement_YearSection + year);

			completedSection.setData("year", year); //$NON-NLS-1$

			completedSection.setExpanded(false);

			Composite client = new Composite(completedSection, SWT.NONE);
	        client.setLayout(new GridLayout(1, false));

			TableViewer completedTableViewer = new TableViewer(client, SWT.FULL_SELECTION | SWT.BORDER);
			completedTableViewer.setUseHashlookup(true);
			Table tableControl = completedTableViewer.getTable();
			tableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tableControl.setHeaderVisible(true);
			tableControl.setLinesVisible(true);
			tableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			tableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			createTableColumns(completedTableViewer, new String[] { Messages.Core_Date, Messages.Core_Title },
					new int[] { 90, 210 }, showDeliveredColumn);

			completedTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			completedTableViewer.setLabelProvider(new CompletedOrderTableLabelProvider());

			completedTableViewer.setInput(ordersByYear.get(year));

			completedTableViewer.addSelectionChangedListener(event -> {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IOrder selectedOrder = (IOrder) selection.getFirstElement();
				if (selectedOrder != null) {
					resetEditMode();
					setActOrder(selectedOrder);
					refresh();
					clearOtherSelections(completedTableViewer);
				}
			});

			completedSection.setClient(client);
			Boolean wasExpanded = expandedStates.get(year);
			if (wasExpanded != null) {
				completedSection.setExpanded(wasExpanded);
			}

			completedSection.addExpansionListener(new ExpansionAdapter() {
	            @Override
	            public void expansionStateChanged(ExpansionEvent e) {
					expandedStates.put(year, completedSection.isExpanded());
					completedContainer.layout(true, true);
					updateUI();
	            }
	        });

			registerMouseWheelListeners(tableControl);
			actionFactory.createOrderHistoryMenu(completedTableViewer);
			completedYearViewers.add(completedTableViewer);

		}

		if (ordersByYear.size() > 2) {
			Button toggleButton = new Button(completedContainer, SWT.PUSH);
			toggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			toggleButton.setText(
					showAllYears ? Messages.OrderManagement_Toggle_Less : Messages.OrderManagement_Toggle_More);
			toggleButton.addListener(SWT.Selection, e -> {
				showAllYears = !showAllYears;
				loadCompletedOrders(completedContainer);
				completedContainer.layout(true, true);
				scrolledComposite.setMinSize(completedContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			});
		}
	}

	private void loadOrderDetails(IOrder order) {
		if (order == null) {
			actOrder = null;
			if (tableViewer != null) {
				tableViewer.setInput(Collections.emptyList());
			}
			return;
		}
		boolean allOpen = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.OPEN);
		showDeliveredColumn = !allOpen;
		actOrder = order;
		switchViewerFor(order);

		Table table = tableViewer.getTable();
		if (table == null || table.isDisposed()) {
			return;
		}

		table.setRedraw(false);
		try {
			boolean hasEntries = !order.getEntries().isEmpty();
			boolean anyOrdered = order.getEntries().stream().anyMatch(e -> e.getState() != OrderEntryState.OPEN);
			updateAddArticleButtonVisibility(hasEntries, anyOrdered);

			List<IOrderEntry> alleEintraege = order.getEntries();
			int sortColumn = OrderConstants.OrderTable.ARTICLE;
			int sortDirection = SWT.UP;

			if (table.getSortColumn() != null) {
				TableSortController controller = (tableViewer == plainViewer) ? plainSorter : checkboxSorter;
				sortColumn = controller.getCurrentColumn();
				sortDirection = controller.getCurrentDirection();
			}

			List<IOrderEntry> initial = new ArrayList<>(alleEintraege.size());
			initial.addAll(alleEintraege);
			initial.sort(OrderEntryComparators.forColumn(sortColumn, sortDirection));

			if (tableViewer.getContentProvider() == null) {
				tableViewer.setContentProvider(ArrayContentProvider.getInstance());
			}
			tableViewer.setInput(initial);

			if (sortColumn >= 0 && sortColumn < table.getColumnCount()) {
				table.setSortColumn(table.getColumn(sortColumn));
			}
			OrderManagementHelper.updateSelectAllCheckbox(this, pendingDeliveredValues);
			updateTableBackground(order);
			pendingDeliveredValues.keySet().removeIf(e -> !isEligibleForBooking(e));
		} finally {
			table.setRedraw(true);
		}
	}

	private boolean isEligibleForBooking(IOrderEntry e) {
		return e.getState() != OrderEntryState.OPEN && e.getDelivered() < e.getAmount();
	}

	public void updateTableBackground(IOrder order) {
		if (tableViewer == null || tableViewer.getTable().isDisposed()) {
			return;
		}
		Table tableControl = tableViewer.getTable();
		boolean isBestellt = order != null
				&& order.getEntries().stream().anyMatch(e -> e.getState() != OrderEntryState.OPEN);

		if (isBestellt) {
			tableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			tableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		} else {
			tableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			tableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		}
	}

	private void addEditingSupportForSupplierColumn(TableViewer v) {
		TableColumn col = v.getTable().getColumn(OrderConstants.OrderTable.SUPPLIER);
		TableViewerColumn tvc = new TableViewerColumn(v, col);
		tvc.setLabelProvider(
				new EntryTableLabelProvider(OrderConstants.OrderTable.SUPPLIER, showDeliveredColumn, this));
		tvc.setEditingSupport(new GenericOrderEditingSupport(this, v, EditingColumnType.SUPPLIER, actOrder,
				OrderConstants.OrderTable.SUPPLIER, orderService));
	}

	private void addEditingSupportForDeliveredColumn(TableViewer v) {
		TableColumn col = v.getTable().getColumn(OrderConstants.OrderTable.DELIVERED);
		TableViewerColumn tvc = new TableViewerColumn(v, col);
		tvc.setLabelProvider(
				new EntryTableLabelProvider(OrderConstants.OrderTable.DELIVERED, showDeliveredColumn, this));
		tvc.setEditingSupport(new GenericOrderEditingSupport(this, v, EditingColumnType.DELIVERED, actOrder,
				OrderConstants.OrderTable.DELIVERED, orderService));
	}

	private void addEditingSupportForOrderColumn(TableViewer v) {
		TableColumn col = v.getTable().getColumn(OrderConstants.OrderTable.ORDERED);
		TableViewerColumn tvc = new TableViewerColumn(v, col);
		tvc.setLabelProvider(new EntryTableLabelProvider(OrderConstants.OrderTable.ORDERED, showDeliveredColumn, this));
		tvc.setEditingSupport(new GenericOrderEditingSupport(this, v, EditingColumnType.ORDERED, actOrder,
				OrderConstants.OrderTable.ORDERED, orderService));
	}

	private void switchViewerFor(IOrder order) {
		boolean anyOrdered = order != null
				&& order.getEntries().stream().anyMatch(e -> e.getState() != OrderEntryState.OPEN);

		TableViewer targetViewer = anyOrdered ? checkboxViewer : plainViewer;

		if (tableViewer == targetViewer && tableStack.topControl == targetViewer.getControl()) {
			return;
		}

		tableViewer = targetViewer;
		tableStack.topControl = targetViewer.getControl();

		if (anyOrdered && checkboxDropTarget != null) {
			dropTarget = checkboxDropTarget;
			CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
		} else if (!anyOrdered && plainDropTarget != null) {
			dropTarget = plainDropTarget;
			CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
		}

		tableArea.layout(true, true);
		if (headerBar != null && !headerBar.isDisposed()) {
			headerBar.layout(true, true);
		}
		if (mainComposite != null && !mainComposite.isDisposed()) {
			mainComposite.layout(true, true);
		}
	}

	public void updateOrderDetails(IOrder order) {
		if (order == null) {
			return;
		}
		String createdStr = OrderManagementUtil.formatDate(order.getTimestamp());
		IOutputLog usierID = OrderManagementUtil.getOrderLogEntry(order);
		Image statusImage = OrderManagementUtil.getStatusIcon(order, false);
		cartIcon.setImage(statusImage);
		titleLabel.setText(order.getName());
		String creatorId = (usierID != null && usierID.getCreatorId() != null) ? usierID.getCreatorId()
				: Messages.UNKNOWN;
		createdLabelState.setText(createdStr + ", " + "(" + creatorId + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		statusValue.setText(OrderManagementUtil.getStatusText(order));
		updateOrderStatus(order);
		actionFactory.setOrder(actOrder);
		updateUI();
	}

	private OrderHistorySummary getHistorySummary(IOrder order) {
		if (order == null || order.getId() == null) {
			return new OrderHistorySummary(null, null, null, null);
		}
		String id = order.getId();
		OrderHistorySummary cached = historyCache.get(id);
		if (cached != null) {
			return cached;
		}

		IOutputLog logEntry = OrderManagementUtil.getOrderLogEntry(order);
		String jsonLog = (logEntry != null) ? logEntry.getOutputterStatus() : "[]"; //$NON-NLS-1$

		OrderHistoryEntry[] historyEntries;
		try {
			historyEntries = gson.fromJson(jsonLog, OrderHistoryEntry[].class);
		} catch (Exception e) {
			historyEntries = new OrderHistoryEntry[0];
		}
		if (historyEntries == null) {
			historyEntries = new OrderHistoryEntry[0];
		}

		String orderedUser = null;
		String orderedDate = null;
		String completedUser = null;
		String completedDate = null;

		DateTimeFormatter dateOnlyFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

		for (int i = historyEntries.length - 1; i >= 0; i--) {
			OrderHistoryEntry entry = historyEntries[i];

			if (entry.getAction() == OrderHistoryAction.ORDERED && orderedUser == null) {
				orderedUser = StringUtils.isNotBlank(entry.getUserId()) ? entry.getUserId() : Messages.UNKNOWN;
				orderedDate = parseDate(entry.getTimestamp(), dateOnlyFormat);
			}
			if (entry.getAction() == OrderHistoryAction.COMPLETEDELIVERY && completedUser == null) {
				completedUser = StringUtils.isNotBlank(entry.getUserId()) ? entry.getUserId() : Messages.UNKNOWN;
				completedDate = parseDate(entry.getTimestamp(), dateOnlyFormat);
			}
		}
		OrderHistorySummary summary = new OrderHistorySummary(orderedUser, orderedDate, completedUser, completedDate);
		historyCache.put(id, summary);
		return summary;
	}

	public void updateOrderStatus(IOrder order) {
		if (order == null) {
			return;
		}
		Table table = tableViewer.getTable();

		OrderHistorySummary history = getHistorySummary(order);

		String orderedUser = history.orderedUser;
		String orderedDate = history.orderedDate;
		String completedUser = history.completedUser;
		String completedDate = history.completedDate;

		boolean hasEntries = !order.getEntries().isEmpty();
		boolean allDone = hasEntries && order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean anyOrdered = order.getEntries().stream().anyMatch(
				e -> e.getState() == OrderEntryState.ORDERED || e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allEntriesHaveSupplier = order.getEntries().stream().allMatch(e -> e.getProvider() != null);
		boolean hasOpenEntries = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.OPEN);

		dispatchedLabelIcon.setImage((allDone || anyOrdered) ? OrderConstants.OrderImages.TICK : OrderConstants.OrderImages.CLEAR);
		dispatchedLabelState.setText((allDone || anyOrdered)
				? defaultString(orderedDate) + ", (" + defaultString(orderedUser) + ")" //$NON-NLS-1$ //$NON-NLS-2$
				: Messages.Corr_No);

		bookedLabelIcon.setImage(allDone ? OrderConstants.OrderImages.TICK : OrderConstants.OrderImages.CLEAR);
		bookedLabelState.setText(allDone
				? defaultString(completedDate) + ", (" + defaultString(completedUser) + ")" //$NON-NLS-1$ //$NON-NLS-2$
				: Messages.Corr_No);

		if (orderButtonCustomImage != null && !orderButtonCustomImage.isDisposed()) {
			orderButtonCustomImage.dispose();
			orderButtonCustomImage = null;
		}

		if (order.getEntries().isEmpty()) {
			orderButton.setText(StringUtils.EMPTY);
			orderButton.setImage(OrderConstants.OrderImages.TICK);
			orderButton.setEnabled(false);
		} else {
			orderButton.setEnabled(true);
			if (!hasOpenEntries && anyOrdered) {
				orderButton.setText(Messages.OrderManagement_Button_Book);
				orderButton.setImage(OrderConstants.OrderImages.IMPORT);
				OrderManagementUtil.adjustLastColumnWidth(table);
			} else if (allDone) {
				orderButton.setText(Messages.OmnivoreView_editActionCaption);
				orderButton.setImage(OrderConstants.OrderImages.EDIT);
			} else if (allEntriesHaveSupplier) {
				orderButton.setText(Messages.OrderManagement_Button_Order);
				orderButton.setImage(OrderConstants.OrderImages.DELIVERY_TRUCK_64x64);
			} else {
				orderButton.setText(Messages.OrderManagement_Button_MissingSupplier);
				orderButton.setImage(OrderConstants.OrderImages.WARNING);
			}
		}

		updateCheckIn();
		if (!pendingDeliveredValues.isEmpty()) {
			orderButton.setText(Messages.MedicationComposite_btnConfirm);
			orderButton.setImage(Images.IMG_TICK.getImage());
		}
	}

	private String parseDate(String isoDateString, DateTimeFormatter formatter) {
		try {
			LocalDateTime dateTime = LocalDateTime.parse(isoDateString);
			return dateTime.format(formatter);
		} catch (Exception e) {
			return Messages.UNKNOWN;
		}
	}

	private String defaultString(String str) {
		return StringUtils.isNotBlank(str) ? str : Messages.UNKNOWN;
	}

	private void updateAddArticleButtonVisibility(boolean hasEntries, boolean anyOrdered) {
		if (addArticleButton == null || addArticleButton.isDisposed()) {
			return;
		}
		boolean show = !anyOrdered;
		addArticleButton.setVisible(show);
		if (headerBar != null && !headerBar.isDisposed()) {
			headerBar.layout();
		}
	}

	public void updateUI() {
		if (isUIUpdating)
			return;
		isUIUpdating = true;

		Display.getDefault().asyncExec(() -> {
			try {
				topComposite.layout(true, true);
				rightListComposite.layout(true, true);
				scrolledComposite.setMinSize(completedContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				rightScrollComposite.setMinSize(rightListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} finally {
				isUIUpdating = false;
			}
		});
	}

	public IOrder getOrder() {
		return actOrder;
	}

	public void setActOrder(IOrder selectedOrder) {
		actOrder = selectedOrder;
		Display.getDefault().asyncExec(() -> {
			restBarCode();
			if (tableViewer == null || tableViewer.getControl().isDisposed()) {
				return;
			}
			boolean isDelivered = (actOrder != null) && OrderManagementUtil.isOrderCompletelyDelivered(actOrder);
			boolean isOpenOrder = (actOrder != null) && !isDelivered;

			if (isOpenOrder) {
				this.setFocus();
				if (!barcodeScannerActivated && isBarcodePortAvailable()) {
					OrderManagementUtil.activateBarcodeScannerAndFocus();
				}
			} else {
				if (barcodeScannerActivated) {
					OrderManagementUtil.deactivateBarcodeScanner();
				}
			}
		});
	}

	public void resetEditMode() {
		setDeliveryEditMode(false);
		OrderManagementUtil.setCheckboxColumnVisible(this, false);
		pendingDeliveredValues.clear();
		OrderManagementHelper.updateSelectAllCheckbox(this, pendingDeliveredValues);

		if (tableViewer != null) {
			tableViewer.refresh();
		}
	}

	public void restBarCode() {
		String COMMAND_ID = "ch.elexis.base.barcode.scanner.ListenerProcess"; //$NON-NLS-1$
		try {
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			Command scannerCommand = commandService.getCommand(COMMAND_ID);
			if (scannerCommand == null) {
				return;
			}
			org.eclipse.core.commands.State state = scannerCommand.getState("org.eclipse.jface.commands.ToggleState"); //$NON-NLS-1$

			if (state == null) {
				state = scannerCommand.getState(IMenuStateIds.STYLE); // $NON-NLS-1$
			}
			if (state != null) {
				barcodeScannerActivated = (Boolean) state.getValue();
			}

		} catch (Exception e) {
			logger.error("Error when deactivating the barcode scanner", e); //$NON-NLS-1$
		}
	}

	void makeActions() {
		actionFactory = new OrderManagementActionFactory(this, actOrder, orderService);
		actionFactory.initActions();
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager tbm = actionBars.getToolBarManager();
		if (tbm != null) {
			tbm.add(actionFactory.getNewAction());
			tbm.add(actionFactory.getDailyWizardAction());
			tbm.add(actionFactory.getWizardAction());
			tbm.add(actionFactory.getPrintAction());
			tbm.add(actionFactory.getExportClipboardAction());
			actionBars.updateActionBars();
		}
	}

	public OrderManagementActionFactory getActionFactory() {
		return actionFactory;
	}

	@Override
	public void dispose() {
		if (orderButtonCustomImage != null && !orderButtonCustomImage.isDisposed()) {
			orderButtonCustomImage.dispose();
		}
		getSite().getPage().removePartListener(focusListener);
		super.dispose();
	}

	public Map<IOrderEntry, Integer> getPendingDeliveredValues() {
		return pendingDeliveredValues;
	}

	private void clearOtherSelections(TableViewer... except) {
		Set<TableViewer> skip = new HashSet<>(Arrays.asList(except));

		if (tableViewer != null && !skip.contains(tableViewer) && !tableViewer.getTable().isDisposed()) {
			tableViewer.setSelection(StructuredSelection.EMPTY);
			tableViewer.getTable().deselectAll();
		}

		if (orderTable != null && !skip.contains(orderTable) && !orderTable.getTable().isDisposed()) {
			orderTable.setSelection(StructuredSelection.EMPTY);
			orderTable.getTable().deselectAll();
		}

		for (TableViewer tv : completedYearViewers) {
			if (tv != null && !skip.contains(tv) && !tv.getTable().isDisposed()) {
				tv.setSelection(StructuredSelection.EMPTY);
				tv.getTable().deselectAll();
			}
		}
	}

	public IOrderEntry findFirstEditableInViewerOrder() {
		Table t = tableViewer.getTable();
		for (int i = 0; i < t.getItemCount(); i++) {
			Object o = t.getItem(i).getData();
			if (o instanceof IOrderEntry e && isEligibleForBooking(e)) {
				return e;
			}
		}
		if (t.getItemCount() > 0) {
			Object top = t.getItem(0).getData();
			if (top instanceof IOrderEntry e) {
				return e;
			}
		}
		return null;
	}

	public IOrderService getOrderService() {
		return orderService;
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	private static final class OrderHistorySummary {
		final String orderedUser;
		final String orderedDate;
		final String completedUser;
		final String completedDate;

		OrderHistorySummary(String orderedUser, String orderedDate, String completedUser, String completedDate) {
			this.orderedUser = orderedUser;
			this.orderedDate = orderedDate;
			this.completedUser = completedUser;
			this.completedDate = completedDate;
		}
	}

	private final Map<String, OrderHistorySummary> historyCache = new HashMap<>();
}
