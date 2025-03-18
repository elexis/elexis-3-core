package ch.elexis.core.ui.views;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.OrderHistoryManager;
import ch.elexis.core.ui.util.OrderManagementActionFactory;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.util.dnd.DropReceiver;
import jakarta.inject.Inject;

public class OrderManagementView extends ViewPart implements IRefreshable {

	public static final String ID = "ch.elexis.OrderManagementView"; //$NON-NLS-1$

	private Composite topComposite;
	private Composite middleComposite;
	private Composite leftComposite;
	private Label cartIcon;
	private Label titleLabel;
	private Label createdLabel;

	private Label statusLabel;
	private Label statusValue;

	public Table table;
	private Table orderTable;
	private Table completedTable;

	private Action checkInAction;
	private IOrder actOrder;
	private OrderManagementActionFactory actionFactory;
	private Button orderButton;
	private boolean showDeliveredColumn = false;

	private Composite dispatchedComposite;
	private Label dispatchedLabelTitle;
	private Label dispatchedLabelIcon;
	private Label dispatchedLabelState;

	private Composite bookedComposite;
	private Label bookedLabelTitle;
	private Label bookedLabelIcon;
	private Label bookedLabelState;
	private Composite completedContainer;
	private boolean isUIUpdating = false;

	public Composite getCompletedContainer() {
		return completedContainer;
	}

	public void setCompletedContainer(Composite completedContainer) {
		this.completedContainer = completedContainer;
	}


	private Composite rightListComposite;
	private ScrolledComposite scrolledComposite;
	private ScrolledComposite rightScrollComposite;

	private boolean hasFocus = false;
	private boolean showAllYears = false;

	private final Image IMGCLEAR = Images.IMG_CLEAR.getImage();
	private final Image TICKIMAGE = Images.IMG_TICK.getImage();
	private final Image DELIVERY_TRUCK = Images.IMG_DELIVERY_TRUCK.getImage(ImageSize._75x66_TitleDialogIconSize);
	private final Image EINBUCHEN = Images.IMG_IMPORT.getImage();
	private final Image LIFERANT = Images.IMG_ACHTUNG.getImage();

	private static final GridData FILL_HORIZONTAL = new GridData(SWT.FILL, SWT.CENTER, true, false);
	private static final GridData FILL_BOTH = new GridData(SWT.FILL, SWT.FILL, true, true);
	private static final GridData ALIGN_RIGHT = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
	private static final GridData FIXED_WIDTH_180 = new GridData(180, SWT.DEFAULT);
	private static final GridData FIXED_WIDTH_120 = new GridData(120, 25);
	private static final GridData FIXED_WIDTH_50 = new GridData(50, SWT.DEFAULT);

	private final OrderHistoryManager orderHistoryManager = new OrderHistoryManager();

	public GenericObjectDropTarget dropTarget;

	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IStock.class.equals(clazz)) {
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
				String valueToSet = hasFocus ? OrderManagementView.class.getName() : null;
				ContextServiceHolder.get().getRootContext().setNamed("barcodeInputConsumer", valueToSet); //$NON-NLS-1$

			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				hasFocus = false;
				String valueToSet = hasFocus ? OrderManagementView.class.getName() : null;
				ContextServiceHolder.get().getRootContext().setNamed("barcodeInputConsumer", valueToSet); //$NON-NLS-1$
			}
		}
	};

	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	public void barcodeEvent(@UIEventTopic(ElexisEventTopics.BASE_EVENT + "barcodeinput") Object event,
			IContextService contextService) {
		  if (!hasFocus) {
		        return;
		    }
		if (event instanceof IArticle && StringUtils.equals(OrderManagementView.class.getName(),
				(String) contextService.getNamed("barcodeInputConsumer").orElse(null))) { //$NON-NLS-1$
			IArticle scannedArticle = (IArticle) event;
			if (actOrder == null) {
				return;
			}
			boolean isOrderOpen = actOrder.getEntries().stream()
					.allMatch(entry -> entry.getState() == OrderEntryState.OPEN);

			if (isOrderOpen) {
				actOrder = OrderManagementUtil.addItemsToOrder(actOrder, List.of(scannedArticle), getSite().getShell());
			} else {
				Optional<IOrderEntry> matchingEntry = actOrder.getEntries().stream()
						.filter(entry -> entry.getArticle() != null
								&& entry.getArticle().getCode().equals(scannedArticle.getCode()))
						.findFirst();
				if (matchingEntry.isPresent()) {
					IOrderEntry entry = matchingEntry.get();

					int newDeliveredAmount = entry.getDelivered() + 1;
					IStock stock = entry.getStock();

					if (stock != null) {
						OrderManagementUtil.updateStockEntry(stock, entry);
					}
					int newDelivered = entry.getDelivered() + 1;
					int orderAmount = entry.getAmount();
					orderHistoryManager.logDelivery(entry.getOrder(), entry, newDelivered, orderAmount);
					entry.setDelivered(newDeliveredAmount);
					entry.setState(newDeliveredAmount >= entry.getAmount() ? OrderEntryState.DONE
							: OrderEntryState.PARTIAL_DELIVER);
					CoreModelServiceHolder.get().save(entry);
					boolean allDelivered = entry.getOrder().getEntries().stream()
							.allMatch(e -> e.getState() == OrderEntryState.DONE);

					if (allDelivered) {
						orderHistoryManager.logCompleteDelivery(entry.getOrder());
					}
				}

			}
			refreshTables();
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
		actionFactory.createContextMenu(table, orderTable);
		loadCompletedOrders(completedContainer);
		updateUI();
		sashForm.setWeights(new int[] { 2, 1 });
	}

	public boolean isShowDeliveredColumn() {
		return showDeliveredColumn;
	}

	public void setShowDeliveredColumn(boolean showDeliveredColumn) {
		this.showDeliveredColumn = showDeliveredColumn;
	}

	private void handleOrderButtonClick() {
		String buttonText = orderButton.getText();
		if (buttonText.equals(Messages.OrderManagement_Button_Order)) {
			if (buttonText.equals(Messages.OrderManagement_Button_Order)) {
				boolean confirm = MessageDialog.openQuestion(this.getSite().getShell(),
						ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Title,
						ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Message);
				if (confirm) {
					actionFactory.sendOrder();
					loadOpenOrders();
				}
			}
			refresh();

		} else if (buttonText.equals(Messages.OrderManagement_Button_Book)) {
			verbucheBestellung();

		} else if (buttonText.equals(Messages.OrderManagement_Button_MissingSupplier)) {
			IContact selectedProvider = ContactSelectionDialog.showInSync(IContact.class,
					Messages.OrderManagement_SelectSupplier_Title, Messages.OrderManagement_SelectSupplier_Message
			);
			if (selectedProvider != null) {
				for (IOrderEntry entry : actOrder.getEntries()) {
					if (entry.getProvider() == null) {
						entry.setProvider(selectedProvider);
						orderHistoryManager.logSupplierAdded(actOrder, entry, selectedProvider.getLabel());
						CoreModelServiceHolder.get().save(entry);
					}
				}
				refreshTables();
			}
		}
	}

	public void refreshTables() {
		loadOpenOrders();
		loadCompletedOrders(completedContainer);
		refresh();
	}

	public int determineEditableColumn(IOrderEntry entry) {
		if (entry.getState() == OrderEntryState.OPEN)
			return 1;
		if (entry.getState() == OrderEntryState.ORDERED || entry.getState() == OrderEntryState.PARTIAL_DELIVER)
			return 2;
		return -1;
	}

	public void updateOrderEntry(IOrderEntry entry, int newValue) {
		if (entry.getState() == OrderEntryState.OPEN) {
			int oldValue = entry.getAmount();
			entry.setAmount(newValue);
			orderHistoryManager.logEdit(actOrder, entry, oldValue, newValue);
		} else {
			OrderManagementUtil.saveSingleDelivery(table.getSelection()[0]);
		}
		CoreModelServiceHolder.get().save(entry);
	}

	/**
	 * Erstellt die Kopfzeile der UI
	 */
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
		cartIcon.setLayoutData(new GridData(50, 60));
	}

	private void createLeftComposite(Composite parent) {

		leftComposite = new Composite(topComposite, SWT.NONE);
		leftComposite.setLayoutData(FILL_BOTH);
		leftComposite.setLayout(new GridLayout(1, false));

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

		createdLabel = new Label(lowerLeftComposite, SWT.NONE);
		createdLabel.setText(Messages.OrderManagement_CreatedLabel);
		createdLabel.setLayoutData(FIXED_WIDTH_180);

		dispatchedComposite = new Composite(lowerLeftComposite, SWT.NONE);
		dispatchedComposite.setLayoutData(ALIGN_RIGHT);
		dispatchedComposite.setLayout(new GridLayout(3, false));

		dispatchedLabelTitle = new Label(dispatchedComposite, SWT.NONE);
		dispatchedLabelTitle.setText(Messages.OrderManagement_DispatchedLabel);
		dispatchedLabelTitle.setLayoutData(FIXED_WIDTH_50);
		dispatchedLabelIcon = new Label(dispatchedComposite, SWT.NONE);

		dispatchedLabelState = new Label(dispatchedComposite, SWT.NONE);
		dispatchedLabelState.setText(StringUtils.EMPTY);
		dispatchedLabelState.setLayoutData(FIXED_WIDTH_50);

	}

	private void createMiddleComposite(Composite parent) {

		middleComposite = new Composite(topComposite, SWT.NONE);
		middleComposite.setLayoutData(FILL_HORIZONTAL);
		middleComposite.setLayout(new GridLayout(1, false));

		Composite upperComposite = new Composite(middleComposite, SWT.NONE);
		upperComposite.setLayoutData(FILL_HORIZONTAL);
		upperComposite.setLayout(new GridLayout(3, false));

		statusLabel = new Label(upperComposite, SWT.NONE);
		statusLabel.setText(Messages.OrderManagement_StatusLabel);
		statusLabel.setLayoutData(FIXED_WIDTH_120);

		statusValue = new Label(upperComposite, SWT.NONE);
		statusValue.setText(StringUtils.EMPTY);
		statusValue.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		statusValue.setLayoutData(FIXED_WIDTH_120);
		statusValue.setFont(createBoldFont(statusValue));

		Composite lowerComposite = new Composite(middleComposite, SWT.NONE);
		lowerComposite.setLayoutData(FILL_HORIZONTAL);
		lowerComposite.setLayout(new GridLayout(2, false));

		bookedComposite = new Composite(lowerComposite, SWT.NONE);
		bookedComposite.setLayoutData(FILL_HORIZONTAL);
		bookedComposite.setLayout(new GridLayout(3, false));

		bookedLabelTitle = new Label(bookedComposite, SWT.NONE);
		GridData bookedLabelTitleData = new GridData(100, SWT.DEFAULT);
		bookedLabelTitleData.horizontalIndent = -5;

		bookedLabelTitle.setLayoutData(bookedLabelTitleData);
		bookedLabelTitle.setText(Messages.OrderManagement_BookedLabel);
		bookedLabelIcon = new Label(bookedComposite, SWT.NONE);
		bookedLabelState = new Label(bookedComposite, SWT.NONE);
		bookedLabelState.setText(StringUtils.EMPTY);
		bookedLabelState.setLayoutData(FIXED_WIDTH_50);

	}

	private void createRightComposite(Composite parent) {
		Composite rightComposite = new Composite(topComposite, SWT.NONE);
		rightComposite.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		rightComposite.setLayout(new GridLayout());
		orderButton = new Button(rightComposite, SWT.PUSH);
		orderButton.setText(Messages.OrderManagement_Button_Order);
		orderButton.setLayoutData(new GridData(120, 30));
		orderButton.setEnabled(false);
	}

	private void createTableUI(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout(1, false));

		table = new Table(mainComposite, SWT.NONE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		dropTarget = new GenericObjectDropTarget("ArtikelDropTarget", table, new DropReceiver(this)); //$NON-NLS-1$
		CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);

		dropTarget.registered(false);
		String[] columnHeaders = { Messages.OrderManagement_Column_Status, Messages.OrderManagement_Column_Ordered,
				Messages.OrderManagement_Column_Delivered, Messages.OrderManagement_Column_Article,
				Messages.OrderManagement_Column_Supplier, Messages.OrderManagement_Column_Stock };
		int[] columnWidths = { 80, 50, 60, 190, 160, 50 };

		createTableColumns(table, columnHeaders, columnWidths);
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
		orderTable = new Table(rightListComposite, SWT.NONE | SWT.FULL_SELECTION);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 200;
		orderTable.setLayoutData(gridData);
		orderTable.setHeaderVisible(true);
		orderTable.setLinesVisible(true);

		String[] columnOrderHeaders = { Messages.OrderManagement_Column_Abbreviation, Messages.Core_Date,
				Messages.Core_Title };
		int[] columnOrderWidths = { 40, 90, 210 };

		createTableColumns(orderTable, columnOrderHeaders, columnOrderWidths);

		Label completedTitle = new Label(rightListComposite, SWT.NONE);
		completedTitle.setText(Messages.OrderManagement_CompletedTitle);

		scrolledComposite = new ScrolledComposite(rightListComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite scrollContent = new Composite(scrolledComposite, SWT.NONE);
		scrollContent.setLayout(new GridLayout(1, false));

		scrolledComposite.setContent(scrollContent);

		completedContainer = new Composite(rightListComposite, SWT.NONE);
		GridData completedGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		completedGridData.heightHint = 100;
		completedContainer.setLayoutData(completedGridData);

		completedContainer.setLayout(new GridLayout(1, false));
		completedContainer = scrollContent;
		completedTable = new Table(completedContainer, SWT.NONE | SWT.FULL_SELECTION);
		completedTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		completedTable.setHeaderVisible(true);
		completedTable.setLinesVisible(true);

		String[] completedHeaders = { Messages.Core_Date, Messages.Core_Title };
		createTableColumns(completedTable, completedHeaders, columnOrderWidths);

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

	private void createTableColumn(Table table, String title, int width) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(title);
		column.setWidth(width);
	}

	private void createTableColumns(Table table, String[] titles, int[] widths) {
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(widths[i]);
		}
	}

	@Override
	public void refresh() {
		if (actOrder != null) {
			loadOrderDetails(actOrder);
			updateOrderDetails(actOrder);
		}
		updateUI();
	}
	
	@Override
	public void setFocus() {

	}

	public void updateCheckIn() {
		if (checkInAction == null) {
			return;
		}
		if (actOrder == null) {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_NoOrder);
		} else if (actOrder.isDone()) {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_Done);
		} else {
			checkInAction.setEnabled(true);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_Confirm);
		}
	}

	private void registerTableListeners(Table tblYear) {
		if (orderTable != null && !orderTable.isDisposed()) {
			orderTable.addListener(SWT.Selection, event -> actionFactory.handleOrderSelection((TableItem) event.item));
		}

		if (completedTable != null && !completedTable.isDisposed()) {
			completedTable.addListener(SWT.Selection,
					event -> actionFactory.handleCompletedOrderSelection((TableItem) event.item));
		}
		if (tblYear != null && !tblYear.isDisposed()) {
			tblYear.addListener(SWT.Selection,
					event -> actionFactory.handleCompletedOrderSelection((TableItem) event.item));
		}

		if (table != null && !table.isDisposed()) {
			table.addListener(SWT.MouseDoubleClick, event -> actionFactory.handleTableDoubleClick());
		}
	}

	private void registerButtonListeners() {
		orderButton.addListener(SWT.Selection, event -> handleOrderButtonClick());
	}

	private void registerMouseWheelListeners(Table tblYear) {
		completedContainer.addListener(SWT.MouseWheel,
				event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));
		orderTable.addListener(SWT.MouseWheel,
				event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));
		if (tblYear != null && !tblYear.isDisposed()) {
			tblYear.addListener(SWT.MouseWheel,
					event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));
		}

		scrolledComposite.addListener(SWT.MouseWheel,
				event -> actionFactory.handleMouseWheelScroll(event, scrolledComposite));
	}

	public void loadOpenOrders() {
		List<IOrder> orders = OrderManagementUtil.getOpenOrders();
		orderTable.removeAll();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

		for (IOrder order : orders) {
			Image statusIcon = OrderManagementUtil.getStatusIcon(order);
			TableItem item = new TableItem(orderTable, SWT.NONE);
			item.setImage(statusIcon);
			item.setText(new String[] { StringUtils.EMPTY, order.getTimestamp().format(formatter), order.getName() });
			item.setData("orderId", order.getId()); //$NON-NLS-1$
		}
		registerMouseWheelListeners(null);
		refresh();
	}

	public void loadCompletedOrders() {

		loadCompletedOrders(completedContainer);

	}

	public void loadCompletedOrders(Composite completedContainer) {
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

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$
	    for (Integer year : sortedYears) {
	        ExpandableComposite yearSection = new ExpandableComposite(
	            completedContainer,
	            SWT.NONE,
	            ExpandableComposite.TREE_NODE 
	                | ExpandableComposite.COMPACT 
	                | ExpandableComposite.FOCUS_TITLE
	        );
	        yearSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			yearSection.setFont(createBoldFont(titleLabel));
			yearSection.setText(Messages.OrderManagement_YearSection + year);
	        yearSection.setExpanded(false);

	        Composite client = new Composite(yearSection, SWT.NONE);
	        client.setLayout(new GridLayout(1, false));

			Table tblYear = new Table(client, SWT.FULL_SELECTION | SWT.NONE);
	        tblYear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        tblYear.setHeaderVisible(true);
	        tblYear.setLinesVisible(true);

			createTableColumn(tblYear, Messages.Core_Date, 90);
			createTableColumn(tblYear, Messages.Core_Title, 210);

	        for (IOrder order : ordersByYear.get(year)) {
	            TableItem item = new TableItem(tblYear, SWT.NONE);
	            item.setData("orderId", order.getId()); //$NON-NLS-1$
				if (!order.getEntries().isEmpty()) {
					item.setData("orderEntry", order.getEntries().get(0)); //$NON-NLS-1$
				}
				item.setText(0, order.getTimestamp().format(formatter));
				item.setText(1, order.getName());
	        }

	        yearSection.setClient(client);
	        yearSection.addExpansionListener(new ExpansionAdapter() {
	            @Override
	            public void expansionStateChanged(ExpansionEvent e) {
					completedContainer.layout(true, true);
					updateUI();
	            }
	        });
			tblYear.addListener(SWT.Selection, event -> {
				TableItem selectedItem = (TableItem) event.item;
				if (selectedItem != null) {
					IOrder selectedOrder = OrderManagementUtil
							.getSelectedOrder((String) selectedItem.getData("orderId"), true); //$NON-NLS-1$
					if (selectedOrder != null) {
						actOrder = selectedOrder;
						showDeliveredColumn = true;
						refresh();

					}
				}
			});
			registerMouseWheelListeners(tblYear);
			actionFactory.createOrderHistoryMenu(tblYear);
		}

		if (ordersByYear.size() > 2) {
			Button toggleButton = new Button(completedContainer, SWT.PUSH);
			toggleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			toggleButton.setText(showAllYears ? Messages.OrderManagement_Toggle_Less : Messages.OrderManagement_Toggle_More);
			toggleButton.addListener(SWT.Selection, e -> {
				showAllYears = !showAllYears;
				loadCompletedOrders(completedContainer);
				completedContainer.layout(true, true);
				scrolledComposite.setMinSize(completedContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			});
		}
		refresh();
	}

	public void loadOrderDetails(IOrder order) {
		for (Control child : table.getChildren()) {
			child.dispose();
		}
		if (order == null) {
			actOrder = null;
			return;
		}
		boolean allOpen = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.OPEN);
		showDeliveredColumn = !allOpen;
		table.removeAll();
		actOrder = order;
		boolean isOrderDone = order.isDone();
		List<IOrderEntry> alleEintraege = isOrderDone ? order.getEntries()
				: order.getEntries().stream()
						.collect(Collectors.toList());
		for (IOrderEntry entry : alleEintraege) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData("orderEntry", entry); //$NON-NLS-1$
			if (showDeliveredColumn) {

			}
			int ordered = entry.getAmount();
			int delivered = entry.getDelivered();
			int missing = Math.max(0, ordered - delivered);
			String deliveredText = showDeliveredColumn ? String.valueOf(delivered) : StringUtils.EMPTY;
			String missingText = showDeliveredColumn ? String.valueOf(missing) : StringUtils.EMPTY;
			if (entry.getState() == OrderEntryState.DONE && delivered == 0) {
				deliveredText = StringUtils.EMPTY;
				missingText = StringUtils.EMPTY;
			}

			String articleName = (entry.getArticle() != null) ? entry.getArticle().getName() : StringUtils.EMPTY;
			String providerLabel = (entry.getProvider() != null) ? entry.getProvider().getLabel() : Messages.UNKNOWN;
			String stockCode = (entry.getStock() != null) ? entry.getStock().getCode() : Messages.OrderManagement_Stock_Unknown;

			item.setText(new String[] { String.format("%12s", missingText), String.valueOf(ordered), deliveredText, //$NON-NLS-1$
					articleName, providerLabel, stockCode });

			if (showDeliveredColumn) {
				item.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_GRAY));
				item.setImage(0, OrderManagementUtil.getEntryStatusIcon(entry));
			}
		}

		if (!isOrderDone) {
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem tableItem = table.getItem(i);
				IOrderEntry orderEntry = (IOrderEntry) tableItem.getData("orderEntry"); //$NON-NLS-1$
				if (orderEntry == null)
					continue;

				if (Messages.UNKNOWN.equals(tableItem.getText(4))) {
					TableEditor editor = new TableEditor(table);
					editor.grabHorizontal = true;
					editor.grabVertical = true;

					ContactSelectionDialogCellEditor providerEditor = new ContactSelectionDialogCellEditor(table,
							Messages.OrderManagement_SelectSupplier_Title,
							Messages.OrderManagement_SelectSupplier_Message);

					providerEditor.setValue(orderEntry.getProvider());
					Control control = providerEditor.getControl();
					editor.setEditor(control, tableItem, 4);

					providerEditor.addListener(new ICellEditorListener() {
						@Override
						public void applyEditorValue() {
							Object newValue = providerEditor.getValue();
							if (newValue instanceof IContact) {
								IContact newProvider = (IContact) newValue;
								orderEntry.setProvider(newProvider);
								orderHistoryManager.logSupplierAdded(order, orderEntry, newProvider.getLabel());
								CoreModelServiceHolder.get().save(orderEntry);
								tableItem.setText(4, newProvider.getLabel());
								updateOrderDetails(order);
							}
						}

						@Override
						public void cancelEditor() {
							/* kann ignoriert werden */ }

						@Override
						public void editorValueChanged(boolean oldValidState, boolean newValidState) {
						}
					});
				}
			}
		}
	}

	public void verbucheBestellung() {
		if (actOrder == null) {
			return;
		}

		AtomicReference<IOrderEntry> nextOrderEntryRef = new AtomicReference<>(null);
		for (TableItem item : table.getItems()) {
			IOrderEntry orderEntry = (IOrderEntry) item.getData("orderEntry"); //$NON-NLS-1$
			if (orderEntry != null && (orderEntry.getState() == OrderEntryState.ORDERED
					|| orderEntry.getState() == OrderEntryState.PARTIAL_DELIVER)) {

				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal = true;
				editor.grabVertical = true;

				final Text deliveredText = new Text(table, SWT.NONE);
				deliveredText.setText(StringUtils.EMPTY);
				deliveredText.selectAll();
				deliveredText.setFocus();

				deliveredText.addModifyListener(e -> {
					item.setText(2, deliveredText.getText());
				});

				deliveredText.addTraverseListener(e -> {
					if (e.detail == SWT.TRAVERSE_RETURN) {
						e.doit = false;
						OrderManagementUtil.saveAllDeliveries(table, actOrder);

						IOrderEntry nextOrderEntry = OrderManagementUtil.getNextOpenEntryAsOrderEntry(table,
								orderEntry);
						nextOrderEntryRef.set(nextOrderEntry);
						refresh();
						Display.getDefault().asyncExec(() -> {
							if (nextOrderEntryRef.get() != null) {
								OrderManagementUtil.focusEntryInTable(table, nextOrderEntryRef.get(), 2);
							}
						});
						refreshTables();
					}
				});
				editor.setEditor(deliveredText, item, 2);
			}
		}
	}

	public void updateOrderDetails(IOrder order) {
		if (order == null) {
			return;
		}
		String createdStr = OrderManagementUtil.formatDate(order.getTimestamp());
		IOutputLog usierID = OrderManagementUtil.getOrderLogEntry(order);
		Image statusImage = OrderManagementUtil.getStatusIcon(order);
		Image scaledStatusImage = new Image(statusImage.getDevice(), statusImage.getImageData().scaledTo(32, 32));
		cartIcon.setImage(scaledStatusImage);
		titleLabel.setText(order.getName());
		String creatorId = (usierID != null && usierID.getCreatorId() != null) ? usierID.getCreatorId()
				: Messages.UNKNOWN;
		createdLabel.setText(Messages.OrderManagement_CreatedLabel + StringUtils.SPACE + createdStr + ", " + creatorId); //$NON-NLS-1$
		statusValue.setText(OrderManagementUtil.getStatusText(order));
		updateOrderStatus(order);
		actionFactory.setOrder(actOrder);
		updateUI();
	}

	private void updateOrderStatus(IOrder order) {
		if (order == null)
			return;
		boolean hasEntries = !order.getEntries().isEmpty();
		boolean allDone = hasEntries && order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean anyOrdered = order.getEntries().stream().anyMatch(
				e -> e.getState() == OrderEntryState.ORDERED || e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allEntriesHaveSupplier = order.getEntries().stream().allMatch(e -> e.getProvider() != null);
		dispatchedLabelIcon.setImage((allDone || anyOrdered) ? TICKIMAGE : IMGCLEAR);
		dispatchedLabelState.setText((allDone || anyOrdered) ? Messages.Core_Yes : Messages.Corr_No);
		bookedLabelIcon.setImage(allDone ? TICKIMAGE : IMGCLEAR);
		bookedLabelState.setText(allDone ? Messages.Core_Yes : Messages.Corr_No);
		if (allDone || order.getEntries().isEmpty()) {
			orderButton.setText(StringUtils.EMPTY);
			orderButton.setImage(TICKIMAGE);
			orderButton.setEnabled(false);
		} else {
			orderButton.setEnabled(true);
			if (anyOrdered) {
				orderButton.setText(Messages.OrderManagement_Button_Book);
				orderButton.setImage(EINBUCHEN);
			} else if (allEntriesHaveSupplier) {
				orderButton.setText(Messages.OrderManagement_Button_Order);
				orderButton.setImage(DELIVERY_TRUCK);
			} else {
				orderButton.setText(Messages.OrderManagement_Button_MissingSupplier);
				orderButton.setImage(LIFERANT);
			}
		}
		if (anyOrdered || allDone) {
			table.setMenu(null);
		} else {
			actionFactory.createContextMenu(table, orderTable);
		}
		updateCheckIn();
	}

	private void updateUI() {
		if (isUIUpdating)
			return;
		isUIUpdating = true;
		Display.getDefault().asyncExec(() -> {
			dispatchedComposite.layout(true, true);
			topComposite.layout();
			middleComposite.layout();
			leftComposite.layout();
			completedContainer.layout(true, true);
			scrolledComposite.setMinSize(completedContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			rightListComposite.layout(true, true);
			rightScrollComposite.setMinSize(rightListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			orderTable.layout(true, true);
			isUIUpdating = false;
		});
	}

	public IOrder getOrder() {
		return actOrder;
	}

	public void setActOrder(IOrder selectedOrder) {
		actOrder = selectedOrder;
	}

	private void makeActions() {
		actionFactory = new OrderManagementActionFactory(this, actOrder);
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
		getSite().getPage().removePartListener(focusListener);
		super.dispose();
	}
}
