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
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;
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
		if (document != null) {
			try {
				if (document instanceof IDocumentLetter) {
					IDocumentLetter documentLetter = (IDocumentLetter) document;
					if (ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false)) {
						String path = getOperatingSystemSpecificExternalStoragePath();
						if (path != null) {
							if (documentLetter.getPatient() != null) {
								return getDocumentLetterFilePath(path, documentLetter);
							} else if (documentLetter.isTemplate()) {
								IDocumentTemplate documentTemplate = CoreModelServiceHolder.get()
										.load(documentLetter.getId(), IDocumentTemplate.class).orElse(null);
								if (documentTemplate != null) {
									// make sure properties are correct if not yet saved to db
									documentTemplate.setTitle(documentLetter.getTitle());
									documentTemplate.setMimeType(documentLetter.getMimeType());
									return getDocumentTemplateFilePath(path, documentTemplate);
								}
							} else {
								logger.warn("No patient for [{}]", documentLetter.getId());
							}

						} else {
							logger.warn("Brief external storage activate with null path");
						}
					}
				} else if (document instanceof IDocumentTemplate) {
					if (ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false)) {
						String path = getOperatingSystemSpecificExternalStoragePath();
						if (path != null) {
							return getDocumentTemplateFilePath(path, (IDocumentTemplate) document);
						}
					}
				}
			} catch (IOException e) {
				logger.warn("Error loading letter [{}]", document.getId(), e);
			}
		}
		return null;
	}

	private static IVirtualFilesystemHandle getDocumentLetterFilePath(String path, IDocumentLetter documentLetter)
			throws IOException {
		IVirtualFilesystemHandle basePath = VirtualFilesystemServiceHolder.get().of(path);
		if (basePath.exists() && basePath.canRead() && basePath.canWrite()) {
			IVirtualFilesystemHandle patientSubDir = basePath.subDir(documentLetter.getPatient().getPatientNr());
			patientSubDir = patientSubDir.mkdir(); // assert existence
			IVirtualFilesystemHandle filePath = patientSubDir
					.subFile(documentLetter.getId() + "." + evaluateFileExtension(documentLetter.getMimeType()));
			return filePath;
		} else {
			logger.warn("Base external storage path [{}] does not exist or is not read/writable", basePath);
		}
		return null;
	}

	private static IVirtualFilesystemHandle getDocumentTemplateFilePath(String externalStoragePath,
			IDocumentTemplate documentTemplate) throws IOException {
		IVirtualFilesystemHandle basePath = VirtualFilesystemServiceHolder.get().of(externalStoragePath);
		if (basePath.exists() && basePath.canRead() && basePath.canWrite()) {
			IVirtualFilesystemHandle templatesSubDir = basePath.subDir("templates");
			templatesSubDir = templatesSubDir.mkdir(); // assert existence
			IVirtualFilesystemHandle typedTemplatesSubDir = null;
			if (BriefConstants.SYS_TEMPLATE.equals(documentTemplate.getTemplateTyp())) {
				typedTemplatesSubDir = templatesSubDir.subDir("system");
				typedTemplatesSubDir = typedTemplatesSubDir.mkdir(); // assert existence
			} else {
				typedTemplatesSubDir = templatesSubDir.subDir("custom");
				typedTemplatesSubDir = typedTemplatesSubDir.mkdir(); // assert existence
			}
			IVirtualFilesystemHandle templatesFileSubDir = typedTemplatesSubDir;
			if (documentTemplate.getMandator() != null) {
				templatesFileSubDir = typedTemplatesSubDir.subDir(documentTemplate.getMandator().getLabel());
				templatesFileSubDir = templatesFileSubDir.mkdir(); // assert existence
			}
			IVirtualFilesystemHandle filePath = templatesFileSubDir.subFile(documentTemplate.getId() + "_"
					+ documentTemplate.getTitle() + "." + evaluateFileExtension(documentTemplate.getMimeType()));

			return filePath;
		} else {
			logger.warn("Base external storage path [{}] does not exist or is not read/writable", basePath);
		}
		return null;
	}

	public static String getOperatingSystemSpecificExternalStoragePath() {
		OS operatingSystem = CoreUtil.getOperatingSystemType();
		String setting;
		switch (operatingSystem) {
			case WINDOWS :
				setting = Preferences.P_TEXT_EXTERN_FILE_PATH_WINDOWS;
				break;
			case MAC :
				setting = Preferences.P_TEXT_EXTERN_FILE_PATH_MAC;
				break;
			case LINUX :
				setting = Preferences.P_TEXT_EXTERN_FILE_PATH_LINUX;
				break;
			default :
				setting = Preferences.P_TEXT_EXTERN_FILE_PATH;
				break;
		}
		String path = ConfigServiceHolder.getGlobal(setting, null);
		if (path == null) {
			LoggerFactory.getLogger(DocumentLetterUtil.class)
					.warn("No OS specific path set, reverting to generic setting");
			path = ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		}
		return path;
	}

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
