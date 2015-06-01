package ch.elexis.core.ui.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.rgw.io.FileTool;

public class ExportTemplateCommand extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(ExportTemplateCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		// get selection
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				if (textTemplate.getTemplate() != null) {
					try {
						FileDialog fdl = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
						fdl.setFilterExtensions(new String[] {
							MimeTypeUtil.getExtensions(textTemplate.getMimeType()), "*.*"
						});
						fdl.setFilterNames(new String[] {
							textTemplate.getMimeTypePrintname(), "All files"
						});
						
						String filename = fdl.open();
						if (filename != null) {
							File file = new File(filename);
							byte[] contents = textTemplate.getTemplate().loadBinary();
							ByteArrayInputStream bais = new ByteArrayInputStream(contents);
							FileOutputStream fos = new FileOutputStream(file);
							FileTool.copyStreams(bais, fos);
							fos.close();
							bais.close();
						}
					} catch (IOException e) {
						logger.error("Error executing file dialog for text template export", e);
					}
				}
			}
		}
		return null;
	}
}
