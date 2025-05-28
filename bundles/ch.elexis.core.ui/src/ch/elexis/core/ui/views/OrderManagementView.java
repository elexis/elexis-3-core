package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.OrderManagementActionFactory;
import ch.elexis.core.ui.util.OrderManagementUtil;
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
	private TableViewer completedTable;

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
	private Label createdLabelState;
	private Composite completedContainer;
	private boolean isUIUpdating = false;

	@Inject
	private IOrderService orderService;

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
	private static final GridData FIXED_WIDTH_50 = new GridData(50, SWT.DEFAULT);
	private static final GridData FIXED_WIDTH_75 = new GridData(75, SWT.DEFAULT);
	private Map<Integer, Boolean> expandedStates = new HashMap<>();
	private final Map<IOrderEntry, Integer> pendingDeliveredValues = new HashMap<>();
	private static boolean barcodeScannerActivated = false;
	private boolean isDeliveryEditMode = false;
	private Image orderButtonCustomImage;

	public boolean isDeliveryEditMode() {
		return isDeliveryEditMode;
	}

	public void setDeliveryEditMode(boolean enabled) {
		this.isDeliveryEditMode = enabled;
	}

	public static void setBarcodeScannerActivated(boolean enabled) {
		barcodeScannerActivated = enabled;
	}

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
				String valueToSet = OrderManagementView.class.getName();
				ContextServiceHolder.get().getRootContext().setNamed("barcodeInputConsumer", valueToSet); //$NON-NLS-1$

			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				hasFocus = false;
				String valueToSet = OrderManagementView.class.getName();
				ContextServiceHolder.get().getRootContext().setNamed("barcodeInputConsumer", valueToSet); //$NON-NLS-1$
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
		if (event instanceof IArticle scannedArticle && StringUtils.equals(OrderManagementView.class.getName(),
				(String) contextService.getNamed("barcodeInputConsumer").orElse(null))) { //$NON-NLS-1$

			if (actOrder == null)
				return;
			Optional<IOrderEntry> matchingEntry = actOrder.getEntries().stream()
					.filter(entry -> entry.getArticle() != null
							&& entry.getArticle().getCode().equals(scannedArticle.getCode()))
					.findFirst();

			if (matchingEntry.isPresent()) {
				IOrderEntry entry = matchingEntry.get();
				int currentPending = pendingDeliveredValues.getOrDefault(entry, 0);
				int newPending = currentPending + 1;
				int ordered = entry.getAmount();
				int delivered = entry.getDelivered();
				int sumDelivered = delivered + newPending;
				if (sumDelivered > ordered) {
					boolean confirm = MessageDialog.openQuestion(getSite().getShell(),
							Messages.OrderManagement_Overdelivery_Title,
							MessageFormat.format(Messages.OrderManagement_Overdelivery_Message, delivered, newPending,
									sumDelivered, ordered));
					if (!confirm) {
						return;
					}
				}
				pendingDeliveredValues.put(entry, newPending);
				tableViewer.refresh();
				updateOrderStatus(actOrder);
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
		actionFactory.createContextMenu(tableViewer, orderTable);
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

		} else if (buttonText.equals(Messages.OrderManagement_Button_MissingSupplier)) {
			IContact selectedProvider = ContactSelectionDialog.showInSync(IContact.class,
					Messages.OrderManagement_SelectSupplier_Title, Messages.OrderManagement_SelectSupplier_Message
			);
			if (selectedProvider != null) {
				for (IOrderEntry entry : actOrder.getEntries()) {
					if (entry.getProvider() == null) {
						entry.setProvider(selectedProvider);
						orderService.getHistoryService().logSupplierAdded(actOrder, entry, selectedProvider.getLabel());
						CoreModelServiceHolder.get().save(entry);
					}
				}
				refreshTables();
			}
		} else if (buttonText.equals(Messages.OrderManagement_Button_Book)) {
			OrderManagementUtil.activateBarcodeScannerAndFocus();
			setDeliveryEditMode(true);
			tableViewer.refresh();
			for (IOrderEntry entry : actOrder.getEntries()) {
				if (entry.getState() == OrderEntryState.ORDERED
						|| entry.getState() == OrderEntryState.PARTIAL_DELIVER) {
					tableViewer.editElement(entry, 3);
					orderButton.setText(Messages.MedicationComposite_btnConfirm);
					orderButton.setImage(Images.IMG_TICK.getImage());
					break;
				}
			}
		}
		if (buttonText.equals(Messages.MedicationComposite_btnConfirm)) {
			for (Map.Entry<IOrderEntry, Integer> entry : pendingDeliveredValues.entrySet()) {
				IOrderEntry orderEntry = entry.getKey();
				int currentDelivered = orderEntry.getDelivered();
				int ordered = orderEntry.getAmount();
				int part = entry.getValue();
				int newTotal = currentDelivered + part;

				if (newTotal > ordered) {
					boolean confirm = MessageDialog.openQuestion(getSite().getShell(),
							Messages.OrderManagement_Overdelivery_Title,
							MessageFormat.format(Messages.OrderManagement_Overdelivery_Message, currentDelivered, part,
									newTotal, ordered));
					if (!confirm) {
						continue;
					}
				}
				if (newTotal < 0) {
					MessageDialog.openError(getSite().getShell(), Messages.Cst_Text_ungueltiger_Wert,
							Messages.OrderManagement_Error_NegativeDeliveredAmount);
					continue;
				}

				orderService.getHistoryService().logDelivery(orderEntry.getOrder(), orderEntry, part, ordered);
				OrderManagementUtil.saveSingleDelivery(orderEntry, part, orderService);
			}
			OrderManagementUtil.activateBarcodeScannerAndFocus();
			barcodeScannerActivated = false;
			pendingDeliveredValues.clear();
			setDeliveryEditMode(false);
			tableViewer.refresh();
			refreshTables();
			return;
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
			orderService.getHistoryService().logEdit(actOrder, entry, oldValue, newValue);
		} else {
			OrderManagementUtil.saveSingleDelivery(entry, newValue, orderService);
		}
		CoreModelServiceHolder.get().save(entry);
		tableViewer.refresh(entry);
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
		orderButton.setLayoutData(new GridData(120, 64));
		orderButton.setEnabled(false);
	}

	private void createTableUI(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(mainComposite, SWT.NONE | SWT.FULL_SELECTION);
		Table tableControl = tableViewer.getTable();
		tableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableControl.setHeaderVisible(true);
		tableControl.setLinesVisible(true);
		dropTarget = new GenericObjectDropTarget("ArtikelDropTarget", tableViewer.getControl(), //$NON-NLS-1$
				new OrderDropReceiver(this, orderService));
		CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);

		dropTarget.registered(false);
		String[] columnHeaders = { Messages.OrderManagement_Column_Status, Messages.OrderManagement_Column_Ordered,
				Messages.OrderManagement_Column_Delivered, Messages.Core_Add, Messages.OrderManagement_Column_Article,
				Messages.OrderManagement_Column_Supplier, Messages.OrderManagement_Column_Stock };
		int[] columnWidths = { 80, 50, 60, 80, 190, 160, 50 };

		createTableColumns(tableViewer, columnHeaders, columnWidths, showDeliveredColumn);
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
		orderTable = new TableViewer(rightListComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table orderTableControl = orderTable.getTable();
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 200;
		orderTableControl.setLayoutData(gridData);
		orderTableControl.setHeaderVisible(true);
		orderTableControl.setLinesVisible(true);
		orderTableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		orderTableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		String[] columnOrderHeaders = { StringUtils.EMPTY, Messages.Core_Date,
				Messages.Core_Title };
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

		completedTable = new TableViewer(completedContainer, SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table completedTableControl = completedTable.getTable();
		completedTableControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		completedTableControl.setHeaderVisible(true);
		completedTableControl.setLinesVisible(true);
		completedTableControl.setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		completedTableControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		String[] completedHeaders = { Messages.Core_Date, Messages.Core_Title };
		createTableColumns(completedTable, completedHeaders, columnOrderWidths, showDeliveredColumn);
		completedTable.setContentProvider(ArrayContentProvider.getInstance());

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
			col.setLabelProvider(new EntryTableLabelProvider(i, showDelivered, this));

		}
	}

	@Override
	public void refresh() {
		if (actOrder != null) {
			loadOrderDetails(actOrder);
			updateOrderDetails(actOrder);
		}
		refreshOrderTableWithIconUpdate();
		updateTableBackground(actOrder);
		updateUI();
	}

	private void refreshOrderTableWithIconUpdate() {
		if (orderTable.getLabelProvider() instanceof OrderTableLabelProvider labelProvider) {
			labelProvider.clearCache();
		}
		orderTable.refresh();
	}

	public void reload() {
		if (actOrder != null) {
			loadOpenOrders();
		}
		if (orderTable != null) {
			loadOpenOrders();
		}

		if (completedContainer != null) {
			loadCompletedOrders();
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
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_NoOrder);
		} else if (actOrder.isDone()) {
			checkInAction.setEnabled(false);
			checkInAction.setToolTipText(Messages.OrderManagement_CheckIn_Done);
		} else {
			checkInAction.setEnabled(true);
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
				}
			});
		}

		if (completedTable != null && !completedTable.getTable().isDisposed()) {
			completedTable.addSelectionChangedListener(event -> {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IOrder selectedOrder = (IOrder) selection.getFirstElement();
				if (selectedOrder != null) {
					actionFactory.handleCompletedOrderSelection(selectedOrder);
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

	private void registerMouseWheelListeners(Table tblYear) {
		completedContainer.addListener(SWT.MouseWheel,
				event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));

		if (orderTable != null && !orderTable.getTable().isDisposed()) {
			orderTable.getTable().addListener(SWT.MouseWheel,
					event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));
		}

		if (tblYear != null && !tblYear.isDisposed()) {
			tblYear.addListener(SWT.MouseWheel,
					event -> actionFactory.handleMouseWheelScroll(event, rightScrollComposite));
		}

		scrolledComposite.addListener(SWT.MouseWheel,
				event -> actionFactory.handleMouseWheelScroll(event, scrolledComposite));
	}

	private void loadOpenOrders() {
		List<IOrder> orders = OrderManagementUtil.getOpenOrders();
		orderTable.setInput(orders);
		registerMouseWheelListeners(null);
		refresh();
	}


	private void loadCompletedOrders() {
		loadCompletedOrders(completedContainer);
	}

	public void loadCompletedOrders(Composite completedContainer) {
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
					actOrder = selectedOrder;
					showDeliveredColumn = true;
					refresh();
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
		refresh();
	}

	private void loadOrderDetails(IOrder order) {
		if (order == null) {
			actOrder = null;
			tableViewer.setInput(Collections.emptyList());
			return;
		}
		boolean allOpen = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.OPEN);
		showDeliveredColumn = !allOpen;
		actOrder = order;
		List<IOrderEntry> alleEintraege = order.getEntries();
		if (tableViewer.getTable().getColumnCount() == 0) {
			String[] columnHeaders = { Messages.OrderManagement_Column_Status, Messages.OrderManagement_Column_Ordered,
					Messages.OrderManagement_Column_Delivered, Messages.Core_Add,
					Messages.OrderManagement_Column_Article,
					Messages.OrderManagement_Column_Supplier, Messages.OrderManagement_Column_Stock };
			int[] columnWidths = { 80, 50, 60,80, 190, 160, 50 };
			createTableColumns(tableViewer, columnHeaders, columnWidths, showDeliveredColumn);
		}

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(alleEintraege);

		addEditingSupportForSupplierColumn();
		addEditingSupportForAddColumn();
		addEditingSupportForDeliveredColumn();
		addEditingSupportForOrderColumn();

		updateTableBackground(order);
		tableViewer.refresh();
		tableViewer.getTable().layout();
	}

	private void updateTableBackground(IOrder order) {
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
		tableViewer.getTable().layout();
	}

	private void addEditingSupportForSupplierColumn() {
		TableColumn existingSupplierColumn = tableViewer.getTable().getColumn(5);
		TableViewerColumn supplierColumn = new TableViewerColumn(tableViewer, existingSupplierColumn);
		supplierColumn.setLabelProvider(new EntryTableLabelProvider(5, showDeliveredColumn, this));
		supplierColumn.setEditingSupport(
				new GenericOrderEditingSupport(this, tableViewer, EditingColumnType.SUPPLIER, actOrder,
						5, orderService));
	}

	private void addEditingSupportForAddColumn() {
		TableColumn existingDeliveredColumn = tableViewer.getTable().getColumn(3);
		TableViewerColumn deliveredColumn = new TableViewerColumn(tableViewer, existingDeliveredColumn);
		deliveredColumn.setLabelProvider(new EntryTableLabelProvider(3, showDeliveredColumn, this));
		deliveredColumn.setEditingSupport(new GenericOrderEditingSupport(this, tableViewer, EditingColumnType.DELIVERED,
				actOrder, 3, orderService));
	}

	private void addEditingSupportForDeliveredColumn() {
		TableColumn existingDeliveredColumn = tableViewer.getTable().getColumn(2);
		TableViewerColumn deliveredColumn = new TableViewerColumn(tableViewer, existingDeliveredColumn);
		deliveredColumn.setLabelProvider(new EntryTableLabelProvider(2, showDeliveredColumn, this));
	}

	private void addEditingSupportForOrderColumn() {
		TableColumn existingOrderColumn = tableViewer.getTable().getColumn(1);
		TableViewerColumn orderColumn = new TableViewerColumn(tableViewer, existingOrderColumn);
		orderColumn.setLabelProvider(new EntryTableLabelProvider(1, showDeliveredColumn, this));
		orderColumn.setEditingSupport(new GenericOrderEditingSupport(this, tableViewer, EditingColumnType.ORDERED, actOrder,
				1, orderService
		));
	}

	public void updateOrderDetails(IOrder order) {
		if (order == null) {
			return;
		}
		String createdStr = OrderManagementUtil.formatDate(order.getTimestamp());
		IOutputLog usierID = OrderManagementUtil.getOrderLogEntry(order);
		Image statusImage = OrderManagementUtil.getStatusIcon(order);
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

	private void updateOrderStatus(IOrder order) {
		if (order == null)
			return;

		IOutputLog logEntry = OrderManagementUtil.getOrderLogEntry(order);
		String jsonLog = (logEntry != null) ? logEntry.getOutputterStatus() : "[]"; //$NON-NLS-1$

		OrderHistoryEntry[] historyEntries;
		try {
			historyEntries = new Gson().fromJson(jsonLog, OrderHistoryEntry[].class);
		} catch (Exception e) {
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

		boolean hasEntries = !order.getEntries().isEmpty();
		boolean allDone = hasEntries && order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean anyOrdered = order.getEntries().stream().anyMatch(
				e -> e.getState() == OrderEntryState.ORDERED || e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allEntriesHaveSupplier = order.getEntries().stream().allMatch(e -> e.getProvider() != null);
		boolean hasOpenEntries = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.OPEN);

		dispatchedLabelIcon.setImage((allDone || anyOrdered) ? TICKIMAGE : IMGCLEAR);
		dispatchedLabelState.setText((allDone || anyOrdered)
				? defaultString(orderedDate) + ", (" + defaultString(orderedUser) + ")" //$NON-NLS-1$ //$NON-NLS-2$
				: Messages.Corr_No);

		bookedLabelIcon.setImage(allDone ? TICKIMAGE : IMGCLEAR);
		bookedLabelState.setText(allDone
				? defaultString(completedDate) + ", (" + defaultString(completedUser) + ")" //$NON-NLS-1$ //$NON-NLS-2$
				: Messages.Corr_No);

		if (orderButtonCustomImage != null && !orderButtonCustomImage.isDisposed()) {
			orderButtonCustomImage.dispose();
			orderButtonCustomImage = null;
		}

		if (allDone || order.getEntries().isEmpty()) {
			orderButton.setText(StringUtils.EMPTY);
			orderButton.setImage(TICKIMAGE);
			orderButton.setEnabled(false);
		} else {
			orderButton.setEnabled(true);
			if (!hasOpenEntries && anyOrdered) {

				orderButton.setText(Messages.OrderManagement_Button_Book);
				orderButton.setImage(EINBUCHEN);
			} else if (allEntriesHaveSupplier) {
				orderButton.setText(Messages.OrderManagement_Button_Order);
				orderButtonCustomImage = new Image(DELIVERY_TRUCK.getDevice(),
						DELIVERY_TRUCK.getImageData().scaledTo(32, 32));
				orderButton.setImage(orderButtonCustomImage);
			} else {

				orderButton.setText(Messages.OrderManagement_Button_MissingSupplier);
				orderButton.setImage(LIFERANT);
			}
		}

		if (hasOpenEntries) {
			actionFactory.createContextMenu(tableViewer, orderTable);
		} else {
			tableViewer.getControl().setMenu(null);
		}

		updateCheckIn();
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

	private void updateUI() {
		isDeliveryEditMode = false;
		if (isUIUpdating)
			return;
		isUIUpdating = true;
		Display.getDefault().syncExec(() -> {
			dispatchedComposite.layout(true, true);
			topComposite.layout();
			middleComposite.layout();
			leftComposite.layout();
			completedContainer.layout(true, true);
			scrolledComposite.setMinSize(completedContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			rightListComposite.layout(true, true);
			rightScrollComposite.setMinSize(rightListComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			orderTable.getTable().layout(true, true);
			isUIUpdating = false;
		});
	}

	public IOrder getOrder() {
		return actOrder;
	}

	public void setActOrder(IOrder selectedOrder) {
		actOrder = selectedOrder;
	}

	public void resetEditMode() {
		setDeliveryEditMode(false);
		pendingDeliveredValues.clear();
		if (tableViewer != null) {
			tableViewer.refresh();
		}
		if (barcodeScannerActivated) {
			OrderManagementUtil.activateBarcodeScannerAndFocus();
			this.setFocus();
			barcodeScannerActivated = false;
		}
	}

	private void makeActions() {
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
}
