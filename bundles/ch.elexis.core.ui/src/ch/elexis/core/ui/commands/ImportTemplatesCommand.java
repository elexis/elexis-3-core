package ch.elexis.core.ui.commands;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.text.MimeTypeUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.TextTemplateImportConflictDialog;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;
import ch.elexis.core.ui.views.textsystem.model.TextTemplate;
import ch.elexis.data.Brief;
import ch.rgw.tools.ExHandler;

public class ImportTemplatesCommand extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(ImportTemplatesCommand.class);
	private String mandantId = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IMandator mandant = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if (mandant == null) {
			return null;
		}
		mandantId = mandant.getId();
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TextTemplateView ttView = (TextTemplateView) activePage.findView(TextTemplateView.ID);

		ITextPlugin plugin = ttView.getActiveTextPlugin();
		if (plugin == null) {
			logger.warn("No TextPlugin found - skipping text template import"); //$NON-NLS-1$
			return null;
		}

		try {
			String mimeType = plugin.getMimeType();
			FileDialog fdl = new FileDialog(UiDesk.getTopShell(), SWT.MULTI);
			fdl.setFilterExtensions(new String[] { MimeTypeUtil.getExtensions(mimeType) });
			fdl.setFilterNames(new String[] { MimeTypeUtil.getPrettyPrintName(mimeType) });

			fdl.open();
			String[] fileNames = fdl.getFileNames();
			String filterPath = fdl.getFilterPath() + File.separator;

			if (fileNames.length > 0) {
				for (String filename : fileNames) {
					File file = new File(filterPath + filename);
					if (file.exists()) {
						String name = filename.substring(0, filename.lastIndexOf('.'));
						TextTemplate sysTemplate = matchesRequiredSystemTemplate(ttView.getRequiredTextTemplates(),
								name, mimeType);

						// check existence of same template
						List<Brief> equivalentTemplates = TextTemplate.findExistingTemplates(sysTemplate != null, name,
								mimeType, mandantId);
						boolean replaceExisting = false;
						if (equivalentTemplates != null && !equivalentTemplates.isEmpty()) {
							TextTemplateImportConflictDialog ttiConflictDialog = new TextTemplateImportConflictDialog(
									UiDesk.getTopShell(), name);
							ttiConflictDialog.open();

							if (ttiConflictDialog.doSkipTemplate()) {
								continue;
							} else if (ttiConflictDialog.doChangeTemplateName()) {
								name = ttiConflictDialog.getNewName();
							} else {
								replaceExisting = true;
							}
						}

						// perform the actual import
						FileInputStream fis = new FileInputStream(file);
						byte[] contentToStore = new byte[(int) file.length()];
						fis.read(contentToStore);
						fis.close();

						if (replaceExisting) {
							// only switch template content
							if (equivalentTemplates instanceof List) {
								equivalentTemplates.get(0).save(contentToStore, mimeType);
							} else {
								logger.error("Invalid state: equivalentTemplates is " + equivalentTemplates); //$NON-NLS-1$
							}
						} else {
							Brief template = new Brief(name, null, CoreHub.getLoggedInContact(), null, null,
									Brief.TEMPLATE);
							// add general form tempalte
							if (sysTemplate == null) {
								template.setAdressat(mandant.getId());
								TextTemplate tt = new TextTemplate(name, StringUtils.EMPTY, mimeType);
								tt.addFormTemplateReference(template);
								ttView.update(tt);
							} else {
								// add system template
								template.set(Brief.FLD_KONSULTATION_ID, Brief.SYS_TEMPLATE);
								sysTemplate.addSystemTemplateReference(template);
								ttView.update(sysTemplate);
							}
							template.save(contentToStore, mimeType);
						}
					}
				}
			}
		} catch (Throwable ex) {
			ExHandler.handle(ex);
			logger.error("Error during import of text templates", ex); //$NON-NLS-1$
		}
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IDocumentLetter.class);
		return null;
	}

	private TextTemplate matchesRequiredSystemTemplate(List<TextTemplate> reqTextTemplates, String name,
			String mimeType) {
		for (TextTemplate sysTemplate : reqTextTemplates) {
			// same name and mimetype as required text template
			if (sysTemplate.getName().equals(name) && sysTemplate.getMimeType().equals(mimeType)) {
				if (MessageDialog.openQuestion(UiDesk.getTopShell(), "Systemvorlage",
						"Soll " + name + " als Systemvorlage importiert werden?")) {
					return sysTemplate;
				}
				return null;
			}
		}
		return null;
	}
}
