package at.medevit.elexis.text.docx.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;

public class ScriptInitializer {

	private String scriptFileRscPath;

	private String filename;

	private long filesize = -1;

	public ScriptInitializer(String scriptFileRscPath) {
		this.scriptFileRscPath = scriptFileRscPath;
		int lastSlash = scriptFileRscPath.lastIndexOf('/');
		if (lastSlash > 0 && lastSlash < scriptFileRscPath.length()) {
			this.filename = scriptFileRscPath.substring(lastSlash + 1);
		}
	}

	/**
	 * Initialize the script by writing the file to the script folder.
	 *
	 */
	public void init() {
		if (filename != null && scriptFileRscPath != null) {
			File folder = getOrCreateScriptFolder();
			if (folder != null && folder.exists()) {
				File outFile = new File(folder, filename);
				try (FileOutputStream fout = new FileOutputStream(outFile);
						InputStream fin = ScriptInitializer.class.getResourceAsStream(scriptFileRscPath)) {
					IOUtils.copy(fin, fout);
				} catch (IOException e) {
					throw new IllegalStateException("Could not initialize script [" + scriptFileRscPath + "]", e);
				}
			}
		} else {
			throw new IllegalStateException("Could not initialize script [" + scriptFileRscPath + "]");
		}
	}

	/**
	 * Test if the script exists in the script folder.
	 *
	 * @return
	 */
	public boolean existsInScriptFolder() {
		if (filename != null && scriptFileRscPath != null) {
			File folder = getOrCreateScriptFolder();
			if (folder != null && folder.exists()) {
				File outFile = new File(folder, filename);
				return outFile.exists();
			}
		} else {
			throw new IllegalStateException("Could test script [" + scriptFileRscPath + "]");
		}
		return false;
	}

	/**
	 * Get the filename of the script.
	 *
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Get the local script folder as {@link File}.
	 *
	 * @return
	 */
	public static File getOrCreateScriptFolder() {
		File writeableDir = CoreHub.getWritableUserDir();
		if (writeableDir != null && writeableDir.exists()) {
			File scriptFolder = new File(writeableDir, "docxScript");
			if (!scriptFolder.exists()) {
				scriptFolder.mkdir();
			}
			return scriptFolder;
		}
		return null;
	}

	public static Properties getPrintCommands(String propertiesRscPath) {
		Properties ret = new Properties();
		try {
			ret.load(ScriptInitializer.class.getResourceAsStream(propertiesRscPath));
		} catch (IOException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Fehler",
					"Fehler beim Einlesen der Script Befehle.");
			LoggerFactory.getLogger(ScriptInitializer.class).error("Error reading print commands properties", e);
		}
		return ret;
	}

	public boolean matchingFileSize() {
		if (filename != null && scriptFileRscPath != null) {
			File folder = getOrCreateScriptFolder();
			if (folder != null && folder.exists()) {
				File existingFile = new File(folder, filename);
				if (existingFile.exists()) {
					long existingFileSize = existingFile.length();
					try (InputStream fin = ScriptInitializer.class.getResourceAsStream(scriptFileRscPath)) {
						if (fin != null) {
							return fin.available() == existingFileSize;
						}
					} catch (IOException e) {
						return false;
					}
				}
			}
		}
		return false;
	}
}
