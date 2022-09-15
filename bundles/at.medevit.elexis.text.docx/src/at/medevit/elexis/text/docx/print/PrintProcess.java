package at.medevit.elexis.text.docx.print;

import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.medevit.elexis.text.docx.DocxTextPlugin;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.utils.CoreUtil;

public class PrintProcess {

	private static Logger logger = LoggerFactory.getLogger(PrintProcess.class);

	private static final String PATTERN_STRING = "\\[.*?\\]";

	private String command;

	private Pattern pattern;

	private HashMap<String, String> contextVariables;

	private static String[] variables = new String[] { "printer", "filename", "scriptdirectory", "tray" };

	public PrintProcess(String command) {
		this.command = command;
		this.contextVariables = new HashMap<>();

		this.pattern = Pattern.compile(PATTERN_STRING);

		contextVariables.put(variables[2],
				ScriptInitializer.getOrCreateScriptFolder().getAbsolutePath() + File.separator);
	}

	public boolean execute() {
		try {
			String preparedCommand = getPreparedCommand(command);
			if (CoreUtil.isWindows()) {
				preparedCommand = "cmd /C start /min " + preparedCommand;
			}
			logger.info("Executing print command [" + preparedCommand + "]");
			Process process = Runtime.getRuntime().exec(preparedCommand);

			if (process.waitFor(CoreHub.localCfg.get(DocxTextPlugin.PRINT_PROCESS_TIMEOUT, 30), TimeUnit.SECONDS)) {
				return process.exitValue() == 0;
			} else {
				logger.error("Error executing print command [" + preparedCommand + "] process terminated.");
				process.destroy();
				return false;
			}
		} catch (IOException | InterruptedException e) {
			logger.error("Error executing print command", e);
			return false;
		}
	}

	/**
	 * Replace all variables in the command String with values from the
	 * contextValiables map.
	 *
	 * @param command
	 * @return
	 */
	private String getPreparedCommand(String command) {
		String preparedCommand = command;
		Matcher matcher = pattern.matcher(command);
		while (matcher.find()) {
			String replacement = contextVariables
					.get(preparedCommand.substring(matcher.start() + 1, matcher.end() - 1));
			if (replacement != null) {
				// escape win characters in replacement
				if (replacement.contains("\\")) {
					replacement = replacement.replaceAll("\\\\", "\\\\\\\\");
				}
				preparedCommand = matcher.replaceFirst((String) replacement);
			} else {
				preparedCommand = matcher.replaceFirst(StringUtils.EMPTY);
			}
			matcher = pattern.matcher(preparedCommand);
		}
		return preparedCommand;
	}

	public void setPrinter(String printer) {
		contextVariables.put(variables[0], printer);
	}

	public void setTray(String tray) {
		contextVariables.put(variables[3], tray);
	}

	public void setFilename(String filename) {
		contextVariables.put(variables[1], filename);
	}

	public static String getVariablesAsString() {
		StringJoiner sj = new StringJoiner(", ");
		for (String variable : variables) {
			sj.add(variable);
		}
		return sj.toString();
	}

}
