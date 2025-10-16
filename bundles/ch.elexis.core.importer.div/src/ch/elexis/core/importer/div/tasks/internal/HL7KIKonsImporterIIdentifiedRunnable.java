package ch.elexis.core.importer.div.tasks.internal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ca.uhn.hl7v2.model.Message;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.tasks.model.TaskTriggerTypeParameter;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.elexis.hl7.util.HL7Helper;
import ch.elexis.hl7.v2x.kikons.HL7ImporterKIKonsReader;
import ch.rgw.tools.VersionedResource;

public class HL7KIKonsImporterIIdentifiedRunnable implements IIdentifiedRunnable {

	public static final String RUNNABLE_ID = "hl7kikonsimporter"; //$NON-NLS-1$
	public static final String DESCRIPTION = "Automatically imports AI consultation HL7 files from a directory"; //$NON-NLS-1$

	public static final String RCP_STRING_IMPORT_FOLDER = "importFolder"; //$NON-NLS-1$
	public static final String RCP_BOOLEAN_MOVE_AFTER_IMPORT = "moveAfterImport"; //$NON-NLS-1$
	public static final String USR_HL7_KIKONS_IMPORT_PATH = "usr/hl7kikons/importpath"; //$NON-NLS-1$

	private final IModelService modelService;
	private static final Set<String> processingFiles = Collections.synchronizedSet(new HashSet<>());

	public HL7KIKonsImporterIIdentifiedRunnable(IModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public String getId() {
		return RUNNABLE_ID;
	}

	@Override
	public String getLocalizedDescription() {
		return DESCRIPTION;
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext() {
		Map<String, Serializable> ctx = new HashMap<>();
		ctx.put(RCP_STRING_IMPORT_FOLDER, StringUtils.EMPTY);
		ctx.put(RCP_BOOLEAN_MOVE_AFTER_IMPORT, Boolean.TRUE);
		return ctx;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context, IProgressMonitor monitor,
			org.slf4j.Logger logger) throws TaskException {

		String folder = null;
		Object ctxUrl = context.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL);
		if (ctxUrl != null) {
			folder = ctxUrl.toString();
		}

		if (StringUtils.isBlank(folder)) {
			throw new TaskException(TaskException.PARAMETERS_MISSING, "Missing 'url' context parameter."); //$NON-NLS-1$
		}
		File importDir;
		try {
			var vfsHandle = VirtualFilesystemServiceHolder.get().of(folder);
			File resolved = vfsHandle.toFile().orElse(null);

			if (resolved == null) {
				throw new IOException("Cannot resolve VFS handle for " + folder); //$NON-NLS-1$
			}

			if (resolved.isFile()) {
				importDir = resolved.getParentFile();
			} else {
				importDir = resolved;
			}

		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR,
					"Failed to resolve virtual filesystem path: " + folder, e); //$NON-NLS-1$
		}

		if (importDir == null || !importDir.exists() || !importDir.isDirectory()) {
			throw new TaskException(TaskException.EXECUTION_ERROR,
					"Import directory not found or invalid: " + importDir); //$NON-NLS-1$
		}

		String dirName = importDir.getName();
		if ("Importiert".equalsIgnoreCase(dirName) || "Error".equalsIgnoreCase(dirName)) { //$NON-NLS-1$ //$NON-NLS-2$
			logger.info("Skipping folder: " + dirName); //$NON-NLS-1$
			return Map.of("result", "Skipped " + dirName + " folder."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}


		boolean moveAfterImport = Boolean.TRUE.equals(context.get(RCP_BOOLEAN_MOVE_AFTER_IMPORT));
		int importedCount = 0;
		File[] hl7Files = importDir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".hl7")); //$NON-NLS-1$
		if (hl7Files == null || hl7Files.length == 0) {
			return Map.of("result", "No new files found."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		for (File file : hl7Files) {
			if ("Importiert".equalsIgnoreCase(file.getParentFile().getName()) //$NON-NLS-1$
					|| !processingFiles.add(file.getAbsolutePath()) || !file.exists()) {
				continue;
			}
			try {
				Message msg = HL7Helper.parseMessage(Files.readString(file.toPath()));
				HL7ImporterKIKonsReader reader = new HL7ImporterKIKonsReader(msg);
				var obs = reader.readObservation(null, true);
				if (obs == null || obs.getObservations().isEmpty()) {
					throw new IllegalStateException("No valid observations in file"); //$NON-NLS-1$
				}
				String konsId = obs.getOrderNumber();
				if (StringUtils.isBlank(konsId)) {
					throw new IllegalArgumentException("Missing Kons-ID in HL7 file"); //$NON-NLS-1$
				}
				var encounterOpt = modelService.load(konsId, IEncounter.class);
				if (encounterOpt.isEmpty()) {
					throw new IllegalStateException("Encounter not found for Kons-ID: " + konsId); //$NON-NLS-1$
				}
				IEncounter kons = encounterOpt.get();
				StringBuilder sb = new StringBuilder();
				obs.getObservations().stream().filter(ch.elexis.hl7.model.TextData.class::isInstance)
						.map(v -> ((ch.elexis.hl7.model.TextData) v).getText()).filter(StringUtils::isNotBlank)
						.forEach(t -> sb.append(t).append('\n'));
				if (sb.length() == 0) {
					throw new IllegalStateException("No text data found in HL7 file"); //$NON-NLS-1$
				}
				VersionedResource vr = kons.getVersionedEntry();
				Samdas samdas = (vr != null && vr.getHead() != null) ? new Samdas(vr.getHead()) : new Samdas();
				Record record = samdas.getRecord();
				String oldText = StringUtils.defaultString(record.getText());
				String newBlock = "\n\n=== HL7 AI Import " //$NON-NLS-1$
						+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ sb.toString().trim();
				record.setText(oldText + newBlock);
				EncounterServiceHolder.get().updateVersionedEntry(kons, samdas.toString(), "HL7 KI Import"); //$NON-NLS-1$
				importedCount++;
				if (moveAfterImport) {
					Path targetDir = file.toPath().getParent().resolve("Importiert"); //$NON-NLS-1$
					Files.createDirectories(targetDir);
					Files.move(file.toPath(), targetDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
				}

			} catch (Exception e) {
				logger.error("Error processing HL7 file: {}", file.getName(), e); //$NON-NLS-1$

				try {
					Path errorDir = file.toPath().getParent().resolve("Error"); //$NON-NLS-1$
					Files.createDirectories(errorDir);
					String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")); //$NON-NLS-1$
					String errorName = file.getName().replace(".hl7", "_ERROR_" + timestamp + ".hl7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Path targetPath = errorDir.resolve(errorName);
					Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
					logger.warn("Moved failed HL7 file to Error folder: {}", targetPath); //$NON-NLS-1$
				} catch (IOException moveError) {
					logger.error("Failed to move HL7 file to Error folder: {}", file.getAbsolutePath(), moveError); //$NON-NLS-1$
				}

			} finally {
				processingFiles.remove(file.getAbsolutePath());
			}
		}
		return Map.of("importedCount", importedCount, "result", "Done"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
