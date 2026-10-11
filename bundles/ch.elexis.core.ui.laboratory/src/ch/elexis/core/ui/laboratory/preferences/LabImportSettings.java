package ch.elexis.core.ui.laboratory.preferences;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;

/**
 * Operating system specific access to the HL7 import directory of the
 * laboratory settings. The value is read from the scope selected with
 * {@link #CFG_PATHS_GLOBAL}, falling back to the key without operating system
 * suffix, so existing installations keep working.
 */
public class LabImportSettings {

	public static final String IMPORT_DIR = "CFG_HL7_IMPORT_DIR"; //$NON-NLS-1$

	public static final String CFG_PATHS_GLOBAL = "CFG_HL7_IMPORT_DIR_GLOBAL"; //$NON-NLS-1$

	private LabImportSettings() {
	}

	public static boolean isStoreGlobal() {
		return isStoreGlobal(ConfigServiceHolder.get());
	}

	public static boolean isStoreGlobal(IConfigService configService) {
		return configService.get(CFG_PATHS_GLOBAL, false);
	}

	public static String get(String preference) {
		IConfigService configService = ConfigServiceHolder.get();
		if (isStoreGlobal(configService)) {
			return PreferencesUtil.getOsSpecificGlobalPreference(preference, configService);
		}
		IContact userContact = ContextServiceHolder.get().getActiveUserContact().orElse(null);
		return PreferencesUtil.getOsSpecificContactPreference(preference, userContact, configService);
	}

	/**
	 * @return the configured import directory as local path, empty String if there
	 *         is none
	 */
	public static String getImportDirectory() {
		return resolveLocalFile(get(IMPORT_DIR)).map(File::getAbsolutePath).orElse(StringUtils.EMPTY);
	}

	/**
	 * Resolve a stored value to a local {@link File}. Handles plain paths, file
	 * URIs and relative paths.
	 *
	 * @param pathOrUri
	 * @return
	 */
	public static Optional<File> resolveLocalFile(String pathOrUri) {
		if (StringUtils.isBlank(pathOrUri)) {
			return Optional.empty();
		}
		IVirtualFilesystemService vfsService = VirtualFilesystemServiceHolder.get();
		if (vfsService == null) {
			return Optional.of(new File(pathOrUri));
		}
		try {
			Optional<File> resolved = vfsService.of(pathOrUri).toFile();
			if (resolved.isPresent()) {
				return resolved;
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(LabImportSettings.class).debug(
					"Location [{}] is not a valid URI, falling back to plain file resolution", //$NON-NLS-1$
					IVirtualFilesystemService.hidePasswordInUrlString(pathOrUri));
		}
		return Optional.of(new File(pathOrUri));
	}
}
