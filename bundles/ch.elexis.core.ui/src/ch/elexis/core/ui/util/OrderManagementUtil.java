package ch.elexis.core.ui.util;


import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Barcode;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.builder.IOrderBuilder;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.constants.OrderConstants;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.ordermanagement.OrderManagementView;
import ch.elexis.core.utils.Extensions;
import ch.elexis.data.Bestellung;

public class OrderManagementUtil {

	private static final Logger logger = LoggerFactory.getLogger(OrderManagementUtil.class);
	public static final String BarcodeScanner_COMPORT = "barcode/Symbol/port"; //$NON-NLS-1$
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

	public static Image getStatusIcon(IOrder order, boolean forTable) {
		boolean isDone = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean isPartial = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.PARTIAL_DELIVER);
		boolean allOrdered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.ORDERED);
		boolean isShoping = order.getEntries().stream()
				.allMatch(e -> e.getState() == OrderEntryState.DONE && e.getOrder().getEntries().isEmpty());
		boolean anyDelivered = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.DONE);

		if (isShoping)
			return forTable ? OrderConstants.OrderImages.SHOPPING : OrderConstants.OrderImages.SHOPPING_64x64;
		if (isDone)
			return OrderConstants.OrderImages.THICK_CHECK;
		if (anyDelivered)
			return forTable ? OrderConstants.OrderImages.DELIVERY_TRUCK
					: OrderConstants.OrderImages.DELIVERY_TRUCK_64x64;
		if (isPartial || allOrdered)
			return forTable ? OrderConstants.OrderImages.DELIVERY_TRUCK
					: OrderConstants.OrderImages.DELIVERY_TRUCK_64x64;
		return forTable ? OrderConstants.OrderImages.SHOPPING_CART : OrderConstants.OrderImages.SHOPPING_CART_64x64;
	}

	public static String getStatusText(IOrder order) {
		if (order == null || order.getEntries().isEmpty()) {
			return Messages.OrderManagement_NoItems;
		}

		boolean allOrdered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.ORDERED);
		boolean allDelivered = order.getEntries().stream().allMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean anyDelivered = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.DONE);
		boolean isPartial = order.getEntries().stream().anyMatch(e -> e.getState() == OrderEntryState.PARTIAL_DELIVER);
		if (allDelivered)
			return Messages.OrderManagement_FullyDelivered;
		if (anyDelivered)
			return Messages.OrderManagement_PartiallyDelivered;
		if (isPartial)
			return Messages.OrderManagement_PartiallyDelivered;
		if (allOrdered)
			return Messages.OrderManagement_Ordered;
		return Messages.OrderManagement_NotOrdered;
	}

	public static IOrder addItemsToOrder(IOrder actOrder, List<IArticle> articlesToOrder, Shell shell,
			IOrderService orderService) {
		if (actOrder == null) {
			NeueBestellungDialog nbDlg = new NeueBestellungDialog(shell,
					ch.elexis.core.ui.views.Messages.BestellView_CreateNewOrder,
					ch.elexis.core.ui.views.Messages.BestellView_EnterOrderTitle);
			if (nbDlg.open() == Dialog.OK) {
				actOrder = new IOrderBuilder(CoreModelServiceHolder.get(), nbDlg.getTitle()).buildAndSave();
			} else {
				return null;
			}
		}

		IMandator mandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		return orderService.addItemsToExistingOrder(actOrder, articlesToOrder, mandator);
	}

	public static Image getEntryStatusIcon(IOrderEntry entry) {
		if (entry == null)
			return null;

		int delivered = entry.getDelivered();
		int ordered = entry.getAmount();

		if (delivered == 0) {
			return Images.IMG_BULLET_RED.getImage();
		} else if (delivered < ordered) {
			return Images.IMG_BULLET_YELLOW.getImage();
		} else {
			return Images.IMG_BULLET_GREEN.getImage();
		}
	}

	public static void activateBarcodeScannerAndFocus() {
		String COMMAND_ID = "ch.elexis.base.barcode.scanner.ListenerProcess"; //$NON-NLS-1$
		try {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
					.getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);

			Command scannerCommand = commandService.getCommand(COMMAND_ID);
			Boolean isActive = false;
			if (scannerCommand.getState("org.eclipse.jface.commands.ToggleState") != null) { //$NON-NLS-1$
				isActive = (Boolean) scannerCommand.getState("org.eclipse.jface.commands.ToggleState").getValue(); //$NON-NLS-1$
			}

			if (!isActive) {
				if (scannerCommand.isEnabled()) {
					try {
						handlerService.executeCommand(COMMAND_ID, null);
					} catch (Exception e) {
						logger.warn("Scanner could not be activated (possibly changed too quickly): " //$NON-NLS-1$
								+ e.getMessage());
					}
				} else {
					logger.debug("Scanner Command ist disabled (busy). Skip activation."); //$NON-NLS-1$
				}
			}
			OrderManagementView.setBarcodeScannerActivated(true);
			ContextServiceHolder.get().getRootContext().setNamed(Barcode.BARCODE_CONSUMER_KEY,
					OrderManagementView.class.getName());

		} catch (Exception e) {
			logger.error("General error in barcode setup", e); //$NON-NLS-1$
		}
	}

	public static void deactivateBarcodeScanner() {
		String COMMAND_ID = "ch.elexis.base.barcode.scanner.ListenerProcess"; //$NON-NLS-1$
		try {
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
					.getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			Command scannerCommand = commandService.getCommand(COMMAND_ID);
			if (scannerCommand == null) {
				return;
			}
			State state = scannerCommand.getState("org.eclipse.jface.commands.ToggleState"); //$NON-NLS-1$
			if (state == null) {
				state = scannerCommand.getState(IMenuStateIds.STYLE);
			}
			Boolean isActive = false;
			if (state != null) {
				isActive = (Boolean) state.getValue();
			}
			if (Boolean.TRUE.equals(isActive)) {
				handlerService.executeCommand(COMMAND_ID, null);
			}
			OrderManagementView.setBarcodeScannerActivated(false);
			ContextServiceHolder.get().getRootContext().setNamed(Barcode.BARCODE_CONSUMER_KEY, null);

		} catch (Exception e) {
			logger.error("Error when deactivating the barcode scanner", e); //$NON-NLS-1$
		}
	}

	public static void handleOrderButtonClick(OrderManagementView view, IOrderService orderService,
			Map<IOrderEntry, Integer> pendingDeliveredValues, IOrder actOrder) {

		Button orderButton = view.orderButton;
		String buttonText = orderButton.getText();

		if (buttonText.equals(Messages.OrderManagement_Button_Order)) {
			boolean confirm = MessageDialog.openQuestion(view.getSite().getShell(),
					ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Title,
					ch.elexis.core.ui.dialogs.Messages.OrderMethodDialog_Message);
			if (confirm) {
				view.getActionFactory().sendOrder();
				view.loadOpenOrders();
			}
			view.refresh();
			return;
		}

		if (buttonText.equals(Messages.OrderManagement_Button_MissingSupplier)) {
			List<IContact> allowedSuppliers = loadConfiguredSuppliers();
			ContactSelectionDialog dialog = new ContactSelectionDialog(view.getSite().getShell(), IContact.class,
					Messages.OrderManagement_SelectSupplier_Title, Messages.OrderManagement_SelectSupplier_Message);
			if (dialog.open() == Dialog.OK) {
				IContact selectedProvider = (IContact) dialog.getSelection();
				if (selectedProvider != null && actOrder != null) {
					for (IOrderEntry entry : actOrder.getEntries()) {
						if (entry.getProvider() == null) {
							entry.setProvider(selectedProvider);
							orderService.getHistoryService().logSupplierAdded(actOrder, entry,
									selectedProvider.getLabel());
							CoreModelServiceHolder.get().save(entry);
						}
					}
					view.refreshTables();
				}
			}
			return;
		}

		if (buttonText.equals(Messages.OrderManagement_Button_Book)
				|| buttonText.equals(ch.elexis.core.ui.views.Messages.OmnivoreView_editActionCaption)) {
			view.setDeliveryEditMode(true);
			setCheckboxColumnVisible(view, true);

			view.tableViewer.refresh();
			IOrderEntry first = view.findFirstEditableInViewerOrder();
			if (first != null) {
				view.tableViewer.setSelection(new StructuredSelection(first), true);
				view.tableViewer.reveal(first);
				view.tableViewer.editElement(first, OrderConstants.OrderTable.DELIVERED);
				orderButton.setText(Messages.MedicationComposite_btnConfirm);
				orderButton.setImage(Images.IMG_TICK.getImage());
			}
			enableLastColumnFill(view.tableViewer.getTable());
			return;
		}

		if (buttonText.equals(Messages.MedicationComposite_btnConfirm)) {
			for (Map.Entry<IOrderEntry, Integer> entry : pendingDeliveredValues.entrySet()) {
				IOrderEntry orderEntry = entry.getKey();
				int currentDelivered = orderEntry.getDelivered();
				int ordered = orderEntry.getAmount();
				int part = entry.getValue();
				int newTotal = currentDelivered + part;
				if (newTotal > ordered) {
					String articleName = orderEntry.getArticle() != null ? orderEntry.getArticle().getLabel()
							: "Unbekannter Artikel"; //$NON-NLS-1$
					boolean confirm = MessageDialog.openQuestion(view.getSite().getShell(),
							Messages.OrderManagement_Overdelivery_Title,
							MessageFormat.format(Messages.OrderManagement_Overdelivery_Message,
									currentDelivered, part, newTotal, ordered, articleName));
					if (!confirm) {
						continue;
					}
				}
				if (newTotal < 0) {
					MessageDialog.openError(view.getSite().getShell(),
							Messages.Cst_Text_ungueltiger_Wert, Messages.OrderManagement_Error_NegativeDeliveredAmount);
					continue;
				}

				orderService.saveSingleDelivery(orderEntry, part);
			}

			pendingDeliveredValues.clear();
			view.setDeliveryEditMode(false);
			setCheckboxColumnVisible(view, false);

			view.selectAllChk.setVisible(false);
			view.selectAllChk.getParent().layout(true, true);

			view.tableViewer.refresh();

			final boolean isCompletelyDelivered = (actOrder != null) ? orderService.isOrderCompletelyDelivered(actOrder)
					: false;
			final String finishedOrderId = (actOrder != null) ? actOrder.getId() : null;
			Display.getDefault().asyncExec(() -> {
				view.loadOpenOrders();
				view.loadCompletedOrders(view.getCompletedContainer());
				if (finishedOrderId != null) {
					IOrder reloaded = CoreModelServiceHolder.get().load(finishedOrderId, IOrder.class).orElse(null);
					if (reloaded != null) {
						boolean isNowCompleted = orderService.isOrderCompletelyDelivered(reloaded);
						if (isNowCompleted == isCompletelyDelivered || reloaded.getEntries().isEmpty()) {
							view.setActOrder(reloaded);
							view.selectOrderInHistory(reloaded);
							view.refresh();
						}
					}
				}
				view.updateUI();
			});

			return;
		}
	}

	public static List<IContact> loadConfiguredSuppliers() {
		Set<IContact> result = new LinkedHashSet<>();
		List<IConfigurationElement> list = Extensions.getExtensions(ExtensionPointConstantsUi.TRANSPORTER);
		for (IConfigurationElement ic : list) {
			String handlerType = ic.getAttribute("type"); //$NON-NLS-1$
			if (handlerType != null && handlerType.contains(Bestellung.class.getName())) {
				try {
					Object executable = ic.createExecutableExtension(ExtensionPointConstantsUi.TRANSPORTER_EXPC);
					if (executable instanceof IDataSender) {
						IDataSender sender = (IDataSender) executable;
						List<IContact> suppliers = sender.getSupplier();
						if (suppliers != null) {
							result.addAll(suppliers);
						}
					}
				} catch (CoreException e) {
					LoggerFactory.getLogger(OrderManagementUtil.class)
							.error("Error loading supplier from plugin: " + ic.getContributor().getName(), e); //$NON-NLS-1$
				}
			}
		}
		return new ArrayList<>(result);
	}

	public static void setCheckboxColumnVisible(OrderManagementView view, boolean visible) {
		Table table = view.checkboxViewer.getTable();
		if (table.isDisposed() || table.getColumnCount() == 0)
			return;
		view.selectAllChk.setVisible(visible);
		TableColumn checkboxCol = table.getColumn(OrderConstants.OrderTable.CHECKBOX);
		checkboxCol.setResizable(visible);
		checkboxCol.setMoveable(visible);
		checkboxCol.setWidth(visible ? 30 : 0);
	}

	public static void enableLastColumnFill(Table table) {
		table.addListener(SWT.Resize, e -> {
			Table t = (Table) e.widget;
			adjustLastColumnWidth(t);
		});
	}

	public static void adjustLastColumnWidth(Table table) {
		if (table == null || table.isDisposed() || table.getColumnCount() == 0) {
			return;
		}
		int clientWidth = table.getClientArea().width;
		if (clientWidth <= 0) {
			return;
		}
		int totalFixedWidth = 0;
		for (int i = 0; i < table.getColumnCount() - 1; i++) {
			totalFixedWidth += table.getColumn(i).getWidth();
		}
		int minLastWidth = 50;
		int newLastWidth = clientWidth - totalFixedWidth;
		if (newLastWidth < minLastWidth) {
			newLastWidth = minLastWidth;
		}
		table.getColumn(table.getColumnCount() - 1).setWidth(newLastWidth);
	}

	public static String formatDate(LocalDateTime dateTime) {
		if (dateTime == null)
			return "";
		return dateTime.format(FORMATTER);
	}
}