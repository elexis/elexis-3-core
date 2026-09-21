package ch.elexis.core.ui.mediorder.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.ui.mediorder.internal.MediorderHistoryBuilder.JsonExport;
import ch.elexis.core.ui.views.ordermanagement.OrderManagementView;

public class MediorderHistoryLinkListener {

	private final MediorderHistoryBuilder historyBuilder;

	public MediorderHistoryLinkListener(MediorderHistoryBuilder historyBuilder) {
		this.historyBuilder = historyBuilder;
	}

	public void handleHistoryLink(LocationEvent event) {
		String location = event.location;
		if (MediorderHistoryBuilder.isOrderLink(location)) {
			event.doit = false;
			Display.getDefault().asyncExec(() -> historyBuilder.resolveOrder(location).ifPresent(this::showOrder));
			return;
		}
		if (!MediorderHistoryBuilder.isJsonLink(location)) {
			return;
		}
		event.doit = false;
		Shell shell = event.widget instanceof Control control ? control.getShell() : null;
		Display.getDefault().asyncExec(() -> historyBuilder.resolveJsonExport(location).ifPresent(export -> {
			if (export.download()) {
				downloadJsonData(shell, export);
			} else {
				copyJsonData(export.json());
			}
		}));
	}

	private void showOrder(IOrder order) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window != null ? window.getActivePage() : null;
		if (page == null) {
			return;
		}
		try {
			OrderManagementView view = (OrderManagementView) page.showView(OrderManagementView.ID);
			view.selectOrderInHistory(order);
		} catch (PartInitException e) {
			LoggerFactory.getLogger(getClass()).error("Failed to show the order {}.", order.getId(), e);
		}
	}

	private void downloadJsonData(Shell shell, JsonExport export) {
		if (shell == null || shell.isDisposed()) {
			return;
		}
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("JSON speichern");
		dialog.setFilterExtensions(new String[] { "*.json", "*.*" });
		dialog.setFileName(export.fileName());
		dialog.setOverwrite(true);
		String selected = dialog.open();
		if (selected == null) {
			return;
		}
		try {
			Files.writeString(Path.of(selected), export.json(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Failed to save the JSON for blob {}.", export.blobId(), e);
			MessageDialog.openError(shell, "JSON speichern", "Die Datei konnte nicht geschrieben werden.");
		}
	}

	private void copyJsonData(String json) {
		if (StringUtils.isBlank(json)) {
			return;
		}
		Clipboard clipboard = new Clipboard(Display.getDefault());
		try {
			clipboard.setContents(new Object[] { json }, new Transfer[] { TextTransfer.getInstance() });
		} finally {
			clipboard.dispose();
		}
	}
}
