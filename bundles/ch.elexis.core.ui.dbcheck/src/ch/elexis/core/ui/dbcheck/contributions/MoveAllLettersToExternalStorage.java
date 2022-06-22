package ch.elexis.core.ui.dbcheck.contributions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.util.DocumentLetterUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class MoveAllLettersToExternalStorage extends ExternalMaintenance {

	private int whichLetters;

	public MoveAllLettersToExternalStorage() {
	}

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		Display.getDefault().syncExec(() -> {
			whichLetters = MessageDialog.open(MessageDialog.QUESTION, Display.getDefault().getActiveShell(),
					"Export letters", "Select which letters to export.", SWT.SHEET, "All letters", "Only templates",
					"Fix templates");
		});
		boolean onlyTemplates = (whichLetters == 1 || whichLetters == 2);

		StringBuilder result = new StringBuilder();
		int okCount = 0;
		int failCount = 0;
		IQuery<IDocumentLetter> query = CoreModelServiceHolder.get().getQuery(IDocumentLetter.class);
		if (onlyTemplates) {
			query.and("typ", COMPARATOR.EQUALS, BriefConstants.TEMPLATE);
		}
		try (IQueryCursor<IDocumentLetter> letters = query.executeAsCursor()) {
			pm.beginTask(Messages.Texterstellung_save_all_letters_externally, letters.size());
			while (letters.hasNext()) {
				IDocumentLetter letter = letters.next();
				try {
					if (onlyTemplates && !letter.isTemplate()) {
						// skip all non template letters
						continue;
					}
					boolean ok = false;
					if (whichLetters < 2) {
						ok = exportToExtern(letter, result);
					} else if (whichLetters == 2) {
						ok = fixDocumentTemplate(
								CoreModelServiceHolder.get().load(letter.getId(), IDocumentTemplate.class).orElse(null),
								result);
					}
					if (ok) {
						okCount++;
					} else {
						failCount++;
						result.append("[" + letter.getId() + "] could not export: see log\n");
					}
					pm.worked(1);
				} catch (IOException e) {
					failCount++;
					result.append("[" + letter.getId() + "] could not export: " + e.getMessage() + StringUtils.LF);
				}
			}
		}
		pm.done();
		return okCount + " OK / " + failCount + " FAIL\n" + result.toString();
	}

	private boolean fixDocumentTemplate(IDocumentTemplate template, StringBuilder result) throws IOException {
		boolean ret = true;
		if (template != null) {
			IVirtualFilesystemHandle vfsHandle = DocumentLetterUtil.getExternalHandleIfApplicable(template);
			Optional<File> file = vfsHandle.toFile();
			if (file.isPresent() && !file.get().exists()) {
				ret = false;
				String fixPath = vfsHandle.getURI().toString();
				if (fixPath.contains("custom")) {
					fixPath = fixPath.replaceFirst("custom", "system");
				} else if (fixPath.contains("system")) {
					fixPath = fixPath.replaceFirst("system", "custom");
				}
				IVirtualFilesystemHandle vfsFixHandle = VirtualFilesystemServiceHolder.get().of(fixPath);
				if (vfsFixHandle.exists()) {
					vfsFixHandle.moveTo(vfsHandle);
					ret = true;
				}
			}
		}
		return ret;
	}

	@Override
	public String getMaintenanceDescription() {
		return "[EXPLETT] Export all letters from DB storage to external storage";
	}

	/**
	 * Try to save the {@link IDocumentLetter} on external storage. Extern file
	 * configuration has to be valid.
	 *
	 * @param result
	 *
	 * @param brief
	 * @return
	 * @throws IOException
	 */
	public boolean exportToExtern(IDocumentLetter letter, StringBuilder result) throws IOException {
		IVirtualFilesystemHandle externalHandle = DocumentLetterUtil.getExternalHandleIfApplicable(letter);
		if (externalHandle != null && externalHandle.exists() && externalHandle.canRead()) {
			return true;
		} else {
			byte[] content = null;
			try (InputStream letterInputStream = letter.getContent()) {
				if (letterInputStream != null) {
					content = IOUtils.toByteArray(letterInputStream);
				} else {
					result.append("[" + letter.getId() + "] content is null\n");
				}
			}
			if (content != null) {
				try (InputStream is = new ByteArrayInputStream(content)) {
					letter.setContent(is);
				}
			}

			return true;
		}
	}

}
