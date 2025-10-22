package ch.elexis.hl7.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.text.model.Samdas;

public class KiConsultationHelper {

	private static final Logger logger = LoggerFactory.getLogger(KiConsultationHelper.class);

	/**
	 * Schreibt KI-Konsultations-Text in die angegebene Konsultation, falls
	 * vorhanden.
	 */
	public static boolean attachToEncounter(String orderNumber, String text) {
		try {
			if (StringUtils.isBlank(orderNumber)) {
				logger.warn("KI Consultation: Keine OrderNumber vorhanden");
				return false;
			}

			var encounterOpt = CoreModelServiceHolder.get().load(orderNumber.trim(), IEncounter.class);
			if (encounterOpt.isEmpty()) {
				logger.warn("KI Consultation: Konsultation mit ID {} nicht gefunden", orderNumber);
				return false;
			}

			IEncounter kons = encounterOpt.get();
			var vr = kons.getVersionedEntry();
			Samdas samdas = (vr != null && vr.getHead() != null) ? new Samdas(vr.getHead()) : new Samdas();

			var record = samdas.getRecord();
			String oldText = StringUtils.defaultString(record.getText());
			String newBlock = "\n\n=== HL7 KI Import "
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " ===\n"
					+ text.trim();
			record.setText(oldText + newBlock);

			EncounterServiceHolder.get().updateVersionedEntry(kons, samdas.toString(), "HL7 KI Import");

			logger.info("KI Consultation in Kons-ID {} importiert ({} Zeichen)", orderNumber, text.length());
			return true;
		} catch (Exception e) {
			logger.error("Fehler beim Speichern der KI-Konsultation", e);
			return false;
		}
	}
}
