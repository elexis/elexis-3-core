package ch.elexis.core.ui.mediorder.internal;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Barcode;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.mediorder.MediorderEntryState;
import ch.elexis.core.mediorder.MediorderLabelCode;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderHistoryService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.ui.mediorder.MediorderPartUtil;

/**
 * Hands an article out when the labels of all its packages are scanned, see
 * {@link MediorderLabelCode}. The barcode scanner resolves the label to the
 * patient stock entry, or to the order entry if it was already handed out.
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + MediorderLabelScanHandler.BARCODE_INPUT_TOPIC)
public class MediorderLabelScanHandler implements EventHandler {

	static final String BARCODE_INPUT_TOPIC = ElexisEventTopics.BASE_EVENT + "barcodeinput"; //$NON-NLS-1$

	private static final int NOTIFICATION_MILLIS = 5000;

	private static final Logger logger = LoggerFactory.getLogger(MediorderLabelScanHandler.class);

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IOrderService orderService;

	@Reference
	private IStickerService stickerService;

	@Reference
	private IStockService stockService;

	/**
	 * Synchronized, as the labels of an article may be scanned in quick succession.
	 */
	@Override
	public synchronized void handleEvent(Event event) {
		Object scanned = event.getProperty(ElexisEventTopics.PROPKEY_OBJ);
		try {
			if (scanned instanceof IStockEntry stockEntry) {
				String labelCode = getLabelCode(event.getProperty(Barcode.BARCODE_INPUT_PROPKEY), stockEntry);
				MediorderPartUtil.getPatient(stockEntry)
						.ifPresent(patient -> handOut(stockEntry, patient, labelCode));
			} else if (scanned instanceof IOrderEntry orderEntry) {
				MediorderPartUtil.getPatient(orderEntry.getStock())
						.ifPresent(patient -> showNotification(MessageFormat.format(
								Messages.Mediorder_scan_already_dispensed, getLabel(orderEntry.getArticle()),
								patient.getLabel()), SWT.ICON_WARNING));
			}
		} catch (RuntimeException e) {
			logger.error("Error handing out scanned mediorder label [{}]", scanned, e); //$NON-NLS-1$
		}
	}

	private void handOut(IStockEntry scanned, IPatient patient, String labelCode) {
		String article = getLabel(scanned.getArticle());
		Optional<IStockEntry> stockEntry = coreModelService.load(scanned.getId(), IStockEntry.class);
		if (stockEntry.isEmpty()) {
			showNotification(
					MessageFormat.format(Messages.Mediorder_scan_already_dispensed, article, patient.getLabel()),
					SWT.ICON_WARNING);
			return;
		}
		MediorderEntryState state = MediorderUtil.determineState(stockEntry.get());
		int packages = stockEntry.get().getCurrentStock();
		if (MediorderEntryState.IN_STOCK != state || packages <= 0) {
			showWarning(MessageFormat.format(Messages.Mediorder_scan_incomplete, article, patient.getLabel(),
					state.getLocaleText()));
			return;
		}
		IOrderHistoryService historyService = orderService.getHistoryService();
		Set<String> scannedLabels = MediorderLabelCode.getScannedLabels(historyService.getMediorderHistory(patient),
				stockEntry.get().getId());
		if (scannedLabels.contains(labelCode)) {
			showNotification(MessageFormat.format(Messages.Mediorder_scan_label_already_scanned, article,
					patient.getLabel(), Math.min(scannedLabels.size(), packages), packages), SWT.ICON_WARNING);
			return;
		}
		int scannedPackages = scannedLabels.size() + 1;
		historyService.logMediorderLabelScanned(patient, stockEntry.get().getArticle(), labelCode,
				Math.min(scannedPackages, packages), packages);
		if (scannedPackages < packages) {
			showNotification(MessageFormat.format(Messages.Mediorder_scan_partial, article, patient.getLabel(),
					scannedPackages, packages), SWT.ICON_INFORMATION);
			return;
		}
		MediorderPartUtil.dispense(List.of(stockEntry.get()), coreModelService, orderService, stickerService,
				stockService);
		contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, IStock.class);
		showNotification(MessageFormat.format(Messages.Mediorder_scan_dispensed, article, patient.getLabel()),
				SWT.ICON_INFORMATION);
	}

	/**
	 * If the barcode scanner does not provide the scanned text, all labels of the
	 * stock entry count as one.
	 */
	private String getLabelCode(Object barcode, IStockEntry stockEntry) {
		Optional<MediorderLabelCode> labelCode = MediorderLabelCode
				.decode(barcode instanceof String ? (String) barcode : null)
				.filter(code -> stockEntry.getId().equals(code.stockEntryId()));
		if (labelCode.isEmpty()) {
			logger.warn("Barcode of mediorder label for stock entry [{}] not available", stockEntry.getId()); //$NON-NLS-1$
			return MediorderLabelCode.PREFIX + stockEntry.getId() + "$"; //$NON-NLS-1$
		}
		return labelCode.get().toCode();
	}

	private String getLabel(IArticle article) {
		return article != null ? article.getLabel() : "?"; //$NON-NLS-1$
	}

	private void showNotification(String message, int icon) {
		Display.getDefault().asyncExec(() -> {
			Shell shell = getShell();
			if (shell == null) {
				return;
			}
			if (icon == SWT.ICON_WARNING) {
				shell.getDisplay().beep();
			}
			ToolTip toolTip = new ToolTip(shell, SWT.BALLOON | icon);
			toolTip.setText(Messages.Mediorder_scan_title);
			toolTip.setMessage(message);
			Rectangle bounds = shell.getBounds();
			toolTip.setLocation(bounds.x + bounds.width - 40, bounds.y + bounds.height - 40);
			toolTip.setAutoHide(true);
			toolTip.setVisible(true);
			shell.getDisplay().timerExec(NOTIFICATION_MILLIS, toolTip::dispose);
		});
	}

	private void showWarning(String message) {
		Display.getDefault()
				.asyncExec(() -> MessageDialog.openWarning(getShell(), Messages.Mediorder_scan_title, message));
	}

	private Shell getShell() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null && workbench.getWorkbenchWindowCount() > 0) {
			window = workbench.getWorkbenchWindows()[0];
		}
		return window != null ? window.getShell() : null;
	}
}
