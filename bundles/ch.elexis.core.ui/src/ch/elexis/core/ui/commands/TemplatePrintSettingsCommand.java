package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.TextTemplatePrintSettingsDialog;
import ch.elexis.core.ui.views.textsystem.TextTemplatePrintSettings;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;

public class TemplatePrintSettingsCommand extends AbstractHandler {
	public static String ID = "ch.elexis.core.ui.command.templatePrintSettings";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		TextTemplate template = getSelectedTextTemplate(event);
		if (template == null) {
			return null;
		}
		String cfgTemplate = template.getCfgTemplateBase();
		String cfgPrinter =
			CoreHub.localCfg.get(cfgTemplate
				+ TextTemplatePrintSettings.TXT_TEMPLATE_PRINTER_SUFFIX, null);
		String cfgTray =
			CoreHub.localCfg.get(cfgTemplate + TextTemplatePrintSettings.TXT_TEMPLATE_TRAY_SUFFIX,
				null);
		
		TextTemplatePrintSettingsDialog ttPrintSettingsDialog =
			new TextTemplatePrintSettingsDialog(UiDesk.getTopShell(), cfgPrinter, cfgTray);
		if (ttPrintSettingsDialog.open() == IDialogConstants.OK_ID) {
			template.setPrinter(ttPrintSettingsDialog.getPrinter());
			template.setTray(ttPrintSettingsDialog.getMediaTray());
		}
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TextTemplateView textTemplateView =
			(TextTemplateView) activePage.findView(TextTemplateView.ID);
		textTemplateView.update(template);
		return null;
	}
	
	private TextTemplate getSelectedTextTemplate(ExecutionEvent event){
		IStructuredSelection selection =
			(IStructuredSelection) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
				.getSelection();
		if (selection != null && !selection.isEmpty()) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof TextTemplate) {
				TextTemplate textTemplate = (TextTemplate) firstElement;
				return textTemplate;
			}
		}
		return null;
	}
}
