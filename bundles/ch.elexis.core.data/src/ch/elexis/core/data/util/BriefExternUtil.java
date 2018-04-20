package ch.elexis.core.data.util;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.interfaces.events.MessageEvent.MessageType;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.MimeTool;

/**
 * Utility class for handling extern storage of {@link Brief}.
 * 
 * @author thomas
 *
 */
public class BriefExternUtil {
	
	/**
	 * Test if configuration of {@link Brief} as extern file is set and valid.
	 * 
	 * @return
	 */
	public static boolean isExternFile(){
		if (CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE, false)) {
			boolean ret = isValidExternPath(
				CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null), true);
			if (!ret) {
				ElexisEventDispatcher.getInstance()
					.fireMessageEvent(new MessageEvent(MessageType.WARN, "Brief Extern",
						"Briefe extern speichern aktiviert, aber Pfad nicht erreichbar."));
			}
			return ret;
		}
		return false;
	}
	
	/**
	 * Get an existing extern {@link File} for the {@link Brief}.
	 * 
	 * @param brief
	 * @return Brief or empty if no such file is found
	 */
	public static Optional<File> getExternFile(Brief brief){
		String path = CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (isValidExternPath(path, true)) {
			File dir = new File(path);
			StringBuilder sb = new StringBuilder();
			Person patient = brief.getPatient();
			if (patient != null) {
				sb.append(patient.get(Patient.FLD_PATID)).append(File.separator)
					.append(brief.getId()).append("." + evaluateExtension(brief.getMimeType()));
				File ret = new File(dir, sb.toString());
				if (ret.exists() && ret.isFile()) {
					return Optional.of(ret);
				} else {
					LoggerFactory.getLogger(BriefExternUtil.class)
						.warn("File [" + ret.getAbsolutePath() + "] not valid e=" + ret.exists()
							+ " f=" + ret.isFile());
				}
			} else {
				LoggerFactory.getLogger(BriefExternUtil.class)
					.warn("No patient for [" + brief.getId() + "]");
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Create a new extern file for the {@link Brief}.
	 * 
	 * @param brief
	 * @return
	 */
	public static Optional<File> createExternFile(Brief brief){
		String path = CoreHub.globalCfg.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (isValidExternPath(path, true)) {
			File dir = new File(path);
			Person patient = brief.getPatient();
			if (patient != null) {
				File patPath = new File(dir, patient.get(Patient.FLD_PATID));
				if (!patPath.exists()) {
					patPath.mkdirs();
				}
				File ret =
					new File(patPath, brief.getId() + "." + evaluateExtension(brief.getMimeType()));
				if (!ret.exists()) {
					try {
						ret.createNewFile();
					} catch (IOException e) {
						LoggerFactory.getLogger(BriefExternUtil.class).error("Error creating file",
							e);
						return Optional.empty();
					}
				}
				return Optional.of(ret);
			} else {
				LoggerFactory.getLogger(BriefExternUtil.class)
					.warn("No patient for [" + brief.getId() + "]");
			}
		}
		return Optional.empty();
	}
	
	private static String evaluateExtension(String input){
		String ext = MimeTool.getExtension(input);
		if (StringUtils.isEmpty(ext)) {
			ext = FilenameUtils.getExtension(input);
			if (StringUtils.isEmpty(ext)) {
				ext = input;
			}
		}
		return ext;
	}
	
	/**
	 * Test if the configured Path is available.
	 * 
	 * @param string
	 * 
	 * @return
	 */
	public static boolean isValidExternPath(String path, boolean log){
		if (path != null) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
				return true;
			} else {
				if (log) {
					LoggerFactory.getLogger(BriefExternUtil.class)
						.warn("Configured path [" + path + "] not valid e=" + dir.exists() + " d="
							+ dir.isDirectory() + " w=" + dir.canWrite());
				}
			}
		} else if (log) {
			LoggerFactory.getLogger(BriefExternUtil.class).warn("No path configured");
		}
		return false;
	}
	
	/**
	 * Try to save the {@link Brief} extern. Extern file configuration has to be valid.
	 * 
	 * @param brief
	 * @return
	 */
	public static boolean exportToExtern(Brief brief){
		if (brief != null && brief.getPatient() != null && isExternFile()) {
			Optional<File> existing = getExternFile(brief);
			if (existing.isPresent()) {
				return true;
			} else {
				byte[] content = brief.loadBinary();
				brief.removeContent();
				brief.save(content, brief.getMimeType());
				return true;
			}
		}
		return false;
	}
}
