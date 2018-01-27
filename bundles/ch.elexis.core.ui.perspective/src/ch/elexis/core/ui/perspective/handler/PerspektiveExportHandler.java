package ch.elexis.core.ui.perspective.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.perspective.service.IPerspectiveExportService;

@Component
public class PerspektiveExportHandler extends AbstractHandler {
	
	static IPerspectiveExportService perspectiveExportService;
	
	@Reference(unbind = "-")
	public void bind(IPerspectiveExportService service){
		perspectiveExportService = service;
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		FileDialog dialog = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] {
			"XMI"
		});
		dialog.setFilterExtensions(new String[] {
			"*.xmi"
		});
		
		dialog.setFilterPath(CoreHub.getWritableUserDir().getAbsolutePath());
		dialog.setFileName("perspective_export.xmi");
		
		String path = dialog.open();
		if (path != null) {
			try {
				perspectiveExportService.exportPerspective(path, null, null);
				MessageDialog.openInformation(UiDesk.getDisplay().getActiveShell(), "Export",
					"Die aktuelle Perspektive wurde erfolgreich exportiert.");
			} catch (IOException e) {
				MessageDialog.openError(UiDesk.getDisplay().getActiveShell(), "Export",
					"Die aktuelle Perspektive kann nicht exportiert werden.");
				LoggerFactory.getLogger(PerspektiveExportHandler.class).error("export error", e);
			}
		}
		return null;
	}
}
