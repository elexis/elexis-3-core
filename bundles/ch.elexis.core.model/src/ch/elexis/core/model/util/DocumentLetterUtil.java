package ch.elexis.core.model.util;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.rgw.tools.MimeTool;

public class DocumentLetterUtil {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentLetterUtil.class);
	
	/**
	 * If the installation is configured to use external storage for Briefe, and the path is valid,
	 * return the virtual handle. This is no guarantee that the referenced element really exists
	 * 
	 * @param documentLetter
	 * @return
	 */
	public static @Nullable IVirtualFilesystemHandle getExternalHandleIfApplicable(
		IDocumentLetter documentLetter){
		
		if (ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false)) {
			String path = ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
			if (path != null) {
				IPatient patient = documentLetter.getPatient();
				if (patient != null) {
					try {
						IVirtualFilesystemHandle basePath =
							VirtualFilesystemServiceHolder.get().of(path);
						if (basePath.exists() && basePath.canRead() && basePath.canWrite()) {
							IVirtualFilesystemHandle patientSubDir =
								basePath.subDir(patient.getPatientNr());
							patientSubDir = patientSubDir.mkdir(); // assert existence
							IVirtualFilesystemHandle filePath =
								patientSubDir.subFile(documentLetter.getId() + "."
									+ evaluateFileExtension(documentLetter.getMimeType()));
							return filePath;
						} else {
							logger.warn(
								"Base external storage path [{}] does not exist or is not read/writable",
								basePath);
						}
					} catch (IOException e) {
						logger.warn("Error loading letter [{}]", documentLetter.getId(), e);
					}
					
				} else {
					logger.warn("No patient for [{}]", documentLetter.getId());
				}
				
			} else {
				logger.warn("Brief external storage activate with null path");
			}
		}
		return null;
	}
	
	/**
	 * Get the file extension part of the input String.
	 * 
	 * @param input
	 * @return
	 */
	public static String evaluateFileExtension(String input){
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
