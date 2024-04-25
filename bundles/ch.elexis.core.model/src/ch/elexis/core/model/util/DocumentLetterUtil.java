package ch.elexis.core.model.util;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.rgw.tools.MimeTool;

public class DocumentLetterUtil {

	private static Logger logger = LoggerFactory.getLogger(DocumentLetterUtil.class);

	/**
	 * If the installation is configured to use external storage for Briefe (
	 * {@link IDocumentLetter} ), and the path is valid, return the virtual handle.
	 * This is no guarantee that the referenced element really exists.
	 *
	 * @param document
	 * @return
	 */
	public static @Nullable IVirtualFilesystemHandle getExternalHandleIfApplicable(IDocument document) {
		if (document == null) {
			return null;
		}

		if (!ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false)) {
			return null;
		}

		String path = PreferencesUtil.getOsSpecificPreference(Preferences.P_TEXT_EXTERN_FILE_PATH,
				ConfigServiceHolder.get());
		if (path == null) {
			logger.error("External storage path is [null]");
			return null;
		}

		IVirtualFilesystemHandle externalStoragePath;
		try {
			externalStoragePath = VirtualFilesystemServiceHolder.get().of(path);
		} catch (IOException e) {
			logger.error("Invalid external storage path [{}]", path, e.getMessage());
			return null;
		}

		try {
			// optimistic - we assume the external storage path exists and validate on the
			// target of the resulting doc path only
			if (document instanceof IDocumentLetter documentLetter) {
				if (documentLetter.getPatient() != null) {
					return getDocumentLetterFilePath(externalStoragePath, documentLetter);
				}
				if (documentLetter.isTemplate()) {
					IDocumentTemplate documentTemplate = CoreModelServiceHolder.get()
							.load(documentLetter.getId(), IDocumentTemplate.class).orElseThrow();
					// make sure properties are correct if not yet saved to db
					documentTemplate.setTitle(documentLetter.getTitle());
					documentTemplate.setMimeType(documentLetter.getMimeType());
					return getDocumentTemplateFilePath(externalStoragePath, documentTemplate);
				}
				logger.warn("No patient set in IDocumentLetter and is no template [{}]", documentLetter.getId());
			} else if (document instanceof IDocumentTemplate) {
				return getDocumentTemplateFilePath(externalStoragePath, (IDocumentTemplate) document);
			}
		} catch (IOException e) {
			logger.warn("Error loading letter [{}]", document.getId(), e);
		}
		return null;
	}

	private static IVirtualFilesystemHandle getDocumentLetterFilePath(IVirtualFilesystemHandle externalStoragePath,
			IDocumentLetter documentLetter) throws IOException {

		IVirtualFilesystemHandle patientSubDir = externalStoragePath.subDir(documentLetter.getPatient().getPatientNr());
		if (!patientSubDir.exists()) {
			if (!(externalStoragePath.canRead() && externalStoragePath.canWrite())) {
				logger.error("External storage path [{}] does not exist or is not read/writable", externalStoragePath);
				return null;
			}
			logger.info("mkdir [{}]",
					IVirtualFilesystemService.hidePasswordInUrlString(patientSubDir.toURL().toString()));
			patientSubDir.mkdir();
		}

		IVirtualFilesystemHandle filePath = patientSubDir
				.subFile(documentLetter.getId() + "." + evaluateFileExtension(documentLetter.getMimeType()));
		return filePath;
	}

	private static IVirtualFilesystemHandle getDocumentTemplateFilePath(IVirtualFilesystemHandle externalStoragePath,
			IDocumentTemplate documentTemplate) throws IOException {

		IVirtualFilesystemHandle templatesSubDir = externalStoragePath.subDir("templates");
		String _templatesSubDir = BriefConstants.SYS_TEMPLATE.equals(documentTemplate.getTemplateTyp()) ? "system"
				: "custom";
		IVirtualFilesystemHandle typedTemplatesSubDir = templatesSubDir.subDir(_templatesSubDir);
		IVirtualFilesystemHandle mandatorTypedTemplatesSubDir = null;
		if (documentTemplate.getMandator() != null) {
			mandatorTypedTemplatesSubDir = typedTemplatesSubDir.subDir(documentTemplate.getMandator().getLabel());
		}

		IVirtualFilesystemHandle targetDirectory = mandatorTypedTemplatesSubDir != null ? mandatorTypedTemplatesSubDir
				: typedTemplatesSubDir;
		if (!targetDirectory.canRead()) {
			if (!(externalStoragePath.canRead() && externalStoragePath.canWrite())) {
				logger.error("External storage path [{}] does not exist or is not read/writable", externalStoragePath);
				return null;
			}
			if (!templatesSubDir.exists()) {
				logger.info("mkdir [{}]",
						IVirtualFilesystemService.hidePasswordInUrlString(templatesSubDir.toURL().toString()));
				templatesSubDir.mkdir();
			}
			if (!typedTemplatesSubDir.exists()) {
				logger.info("mkdir [{}]",
						IVirtualFilesystemService.hidePasswordInUrlString(typedTemplatesSubDir.toURL().toString()));
				typedTemplatesSubDir.mkdir();
			}
			if (mandatorTypedTemplatesSubDir != null && !mandatorTypedTemplatesSubDir.exists()) {
				logger.info("mkdir [{}]", IVirtualFilesystemService
						.hidePasswordInUrlString(mandatorTypedTemplatesSubDir.toURL().toString()));
				mandatorTypedTemplatesSubDir.mkdir();
			}
		}

		IVirtualFilesystemHandle targetFile = targetDirectory.subFile(documentTemplate.getId() + "_"
				+ documentTemplate.getTitle() + "." + evaluateFileExtension(documentTemplate.getMimeType()));
		return targetFile;
	}

//	public static String getOperatingSystemSpecificExternalStoragePath() {
//		OS operatingSystem = CoreUtil.getOperatingSystemType();
//		String setting = switch (operatingSystem) {
//		case WINDOWS -> Preferences.P_TEXT_EXTERN_FILE_PATH_WINDOWS;
//		case MAC -> Preferences.P_TEXT_EXTERN_FILE_PATH_MAC;
//		case LINUX -> Preferences.P_TEXT_EXTERN_FILE_PATH_LINUX;
//		default -> Preferences.P_TEXT_EXTERN_FILE_PATH;
//		};
//		String path = ConfigServiceHolder.getGlobal(setting, null);
//		if (path == null) {
//			LoggerFactory.getLogger(DocumentLetterUtil.class)
//					.warn("No OS specific path set, reverting to generic setting");
//			path = ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
//		}
//		return path;
//	}

	/**
	 * Get the file extension part of the input String.
	 *
	 * @param input
	 * @return
	 */
	public static String evaluateFileExtension(String input) {
		String ext = MimeTool.getExtension(input);
		if (StringUtils.isEmpty(ext)) {
			ext = FilenameUtils.getExtension(input);
			if (StringUtils.isEmpty(ext)) {
				ext = input;
			}
		}
		return ext;
	}

}
