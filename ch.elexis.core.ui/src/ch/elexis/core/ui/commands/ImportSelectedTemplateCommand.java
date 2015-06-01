package ch.elexis.core.ui.commands;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.MimeTypeUtil;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;
import ch.rgw.tools.ExHandler;

public class ImportSelectedTemplateCommand extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(ImportSelectedTemplateCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TextTemplateView textTemplateView =
			(TextTemplateView) activePage.findView(TextTemplateView.ID);
		
		TextTemplate textTemplate = getSelectedTextTemplate(event);
		if (textTemplate == null) {
			logger.warn("No TextTemplate selected - skipping template import");
			return null;
		}
		
		ITextPlugin plugin = textTemplateView.getActiveTextPlugin();
		if (plugin == null) {
			logger.warn("No TextPlugin found - skipping text template import");
			return null;
		}
		
		try {
			String mimeType = plugin.getMimeType();
			FileDialog fdl = new FileDialog(UiDesk.getTopShell());
			fdl.setFilterExtensions(new String[] {
				MimeTypeUtil.getExtensions(mimeType)
			});
			fdl.setFilterNames(new String[] {
				MimeTypeUtil.getPrettyPrintName(mimeType)
			});
			
			String filename = fdl.open();
			if (filename != null) {
				File file = new File(filename);
				if (file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					byte[] contentToStore = new byte[(int) file.length()];
					fis.read(contentToStore);
					fis.close();
					
					Brief bTemplate =
						new Brief(textTemplate.getName(), null, CoreHub.actUser, null, null,
							Brief.TEMPLATE);
					if (textTemplate.isSystemTemplate()) {
						bTemplate.set(Brief.FLD_KONSULTATION_ID, Brief.SYS_TEMPLATE);
						textTemplate.addSystemTemplateReference(bTemplate);
					} else {
						textTemplate.addFormTemplateReference(bTemplate);
					}
					bTemplate.save(contentToStore, plugin.getMimeType());
					textTemplateView.update(textTemplate);
				}
			}
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
		return null;
	}
	
	private TextTemplate getSelectedTextTemplate(ExecutionEvent event){
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object firstElement = strucSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				return textTemplate;
			}
		}
		return null;
	}
}
