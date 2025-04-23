package ch.elexis.core.ui.laboratory.controls.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.service.holder.LabImportUtilHolder;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.v2x.labitem.helper.HL7AutoImportHelper;


public class HL7AutoGroupImporter {

	private static final Logger logger = LoggerFactory.getLogger(HL7AutoGroupImporter.class);
	private static final Map<String, Pattern> PROFILE_REGEX_MAP = Map.of("Analytica", //$NON-NLS-1$
			Pattern.compile("^\\d{2}TEST(\\d{2})(.+)$") //$NON-NLS-1$
	// Weitere Labore hier hinzufügen...
	);

	private static ILaboratory myLab;
	private String profile;
	private IProgressMonitor monitor;



	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public int importDirectory(String dirPath) throws IOException {
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			logger.error("Kein Verzeichnis: " + dirPath); //$NON-NLS-1$
		}
		File[] files = dir.listFiles((f, name) -> name.toLowerCase().endsWith(".hl7") && !name.startsWith("._")); //$NON-NLS-1$ //$NON-NLS-2$
		if (files == null || files.length == 0) {
			logger.info("Keine .hl7-Dateien gefunden in {}", dirPath); //$NON-NLS-1$
			return 0;
		}

		if (monitor != null) {
			monitor.beginTask("Importiere HL7-Dateien...", files.length); //$NON-NLS-1$
		}

		int count = 0;
		for (File file : files) {
			if (monitor != null && monitor.isCanceled())
				break;

			if (monitor != null) {
				monitor.subTask("Verarbeite Datei: " + file.getName()); //$NON-NLS-1$
			}
			importFile(file.getAbsolutePath());
			if (monitor != null)
				monitor.worked(1);
			count++;
		}

		if (monitor != null) {
			monitor.done();
		}

		return count;
	}

	public void importFile(String filePath) throws IOException {
		profile = ConfigServiceHolder.getUser("CFG_HL7_IMPORT_PROFILE", "Analytica"); //$NON-NLS-1$ //$NON-NLS-2$
		File originalFile = new File(filePath);
		if (!originalFile.canRead()) {
			logger.error("File not readable: " + filePath); //$NON-NLS-1$
			return;
		}

		File fixedFile = fixHL7File(originalFile);
		myLab = LabImportUtilHolder.get().getOrCreateLabor(profile);

		IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(fixedFile);
		List<HL7Reader> readerList = HL7AutoImportHelper.parseImportReaders(fileHandle);

		if (readerList.isEmpty()) {
			logger.info("No HL7 messages found in {}", filePath); //$NON-NLS-1$
			return;
		}

		String filename = originalFile.getName();
		GroupAndPriority groupInfo = extractGroupAndPriorityFromFilename(filename);

		for (HL7Reader reader : readerList) {
			processReader(reader, groupInfo);
		}
	}


	private File fixHL7File(File originalFile) throws IOException {
		List<String> lines = Files.readAllLines(originalFile.toPath(), StandardCharsets.ISO_8859_1);
		List<String> fixedLines = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("OBX|")) { //$NON-NLS-1$
				String[] parts = line.split("\\|", -1); //$NON-NLS-1$
				if (parts.length > 2 && (parts[2] == null || parts[2].isBlank())) {
					parts[2] = "TX"; //$NON-NLS-1$
					line = String.join("|", parts); //$NON-NLS-1$
				}
			}
			fixedLines.add(line);
		}

		File fixedFile = File.createTempFile("hl7fixed_", ".hl7"); //$NON-NLS-1$ //$NON-NLS-2$
		Files.write(fixedFile.toPath(), fixedLines, StandardCharsets.ISO_8859_1);
		fixedFile.deleteOnExit();
		return fixedFile;
	}

	private void processReader(HL7Reader reader, GroupAndPriority groupInfo) {
		try {
			ObservationMessage obsMsg = reader.readObservation(null, true);
			if (obsMsg == null) {
				logger.warn("No ObservationMessage in message"); //$NON-NLS-1$
				return;
			}
			List<IValueType> observations = obsMsg.getObservations();
			if (observations == null || observations.isEmpty()) {
				logger.info("No observations available"); //$NON-NLS-1$
				return;
			}

			int priorityCounter = 1;
			for (IValueType iValueType : observations) {
				if (iValueType instanceof LabResultData lab) {
					processLabResult(lab, groupInfo, priorityCounter++);
				}
			}
		} catch (Exception e) {
			logger.error("Error parsing the message: {}", e.getMessage(), e); //$NON-NLS-1$
		}
	}

	private void processLabResult(LabResultData lab, GroupAndPriority groupInfo, int priorityCounter) {
		try {
			String group = safeGroupName(groupInfo.priority, groupInfo.groupName);
			String prio = String.valueOf(priorityCounter);
			ILabItem existing = LabImportUtilHolder.get().getLabItem(lab.getCode(), myLab);
			if (monitor != null) {
				monitor.subTask(lab.getCode() + " – " + lab.getName()); //$NON-NLS-1$
			}
			boolean sameGroup = existing != null && existing.getGroup() != null
					&& existing.getGroup().contains(groupInfo.groupName);

			if (existing == null || !sameGroup) {
				LabImportUtilHolder.get().createLabItem(lab.getCode(), lab.getName(), myLab, lab.getRange(),
						lab.getRange(), lab.getUnit(), lab.isNumeric() ? LabItemTyp.NUMERIC : LabItemTyp.TEXT, group,
						prio);
			}
		} catch (Exception ex) {
			logger.error("Error when creating LabItem [{}]: {}", lab.getCode(), ex.getMessage(), ex); //$NON-NLS-1$
		}
	}

	private GroupAndPriority extractGroupAndPriorityFromFilename(String filename) {
		if (filename.endsWith(".hl7")) { //$NON-NLS-1$
			filename = filename.substring(0, filename.length() - 4);
		}
		Pattern pattern = PROFILE_REGEX_MAP.get(profile);
		if (pattern == null) {
			logger.warn("Kein Regex-Muster für Profil '{}'", profile); //$NON-NLS-1$
			return new GroupAndPriority("Unbekannt", "99"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		Matcher matcher = pattern.matcher(filename);
		if (matcher.find()) {
			return new GroupAndPriority(matcher.group(2), matcher.group(1));
		} else {
			logger.info("Keine Gruppe/Priorität extrahiert aus Dateiname: {}", filename); //$NON-NLS-1$
			return new GroupAndPriority("Unbekannt", "99"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private String safeGroupName(String priority, String groupName) {
		String group = priority + StringUtils.SPACE + groupName; // $NON-NLS-1$
		final int maxLength = 25;
		if (group.length() > maxLength) {
			logger.info("Gruppenname gekürzt auf {} Zeichen: {}", maxLength, group); //$NON-NLS-1$
			return group.substring(0, maxLength);
		}
		return group;
	}

	private static class GroupAndPriority {
		final String groupName;
		final String priority;

		GroupAndPriority(String groupName, String priority) {
			this.groupName = groupName;
			this.priority = priority;
		}
	}
}
