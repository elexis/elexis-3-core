package ch.elexis.core.importer.div.importers.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.Message;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.util.HL7Helper;
import ch.elexis.hl7.v2x.kikons.HL7ImporterKIKonsReader;

public class HL7KiKonsImportHandler {
	private static final Logger logger = LoggerFactory.getLogger(HL7KiKonsImportHandler.class);

	public static boolean handleKiKonsImport(String filePath) {
		try {
			String hl7Content = Files.readString(Path.of(filePath.replace("file:///", "")));
			Message msg = HL7Helper.parseMessage(hl7Content);
			HL7ImporterKIKonsReader reader = new HL7ImporterKIKonsReader(msg);
			var obs = reader.readObservation(null, true);

			if (obs == null || obs.getOrderNumber() == null) {
				logger.warn("KI-Kons HL7 ohne OrderNumber: {}", filePath);
				return false;
			}

			String konsId = obs.getOrderNumber().trim();
			var encounterOpt = CoreModelServiceHolder.get().load(konsId, IEncounter.class);
			if (encounterOpt.isEmpty()) {
				logger.warn("Konsultation mit ID {} nicht gefunden", konsId);
				return false;
			}

			IEncounter kons = encounterOpt.get();
			StringBuilder sb = new StringBuilder();
			obs.getObservations().stream().filter(TextData.class::isInstance).map(v -> ((TextData) v).getText())
					.filter(StringUtils::isNotBlank).forEach(t -> sb.append(t).append("\n"));

			if (sb.length() == 0) {
				logger.warn("Keine TextData im KI-HL7: {}", filePath);
				return false;
			}

			var vr = kons.getVersionedEntry();
			Samdas samdas = (vr != null && vr.getHead() != null) ? new Samdas(vr.getHead()) : new Samdas();

			var record = samdas.getRecord();
			String oldText = StringUtils.defaultString(record.getText());
			String newBlock = "\n\n=== HL7 KI Import "
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===\n"
					+ sb.toString().trim();
			record.setText(oldText + newBlock);

			EncounterServiceHolder.get().updateVersionedEntry(kons, samdas.toString(), "HL7 KI Import");

			logger.info("Text erfolgreich in Kons-ID {} importiert", konsId);
			return true;
		} catch (Exception e) {
			logger.error("Fehler beim KI-Kons Import: {}", filePath, e);
			return false;
		}
	}
}
