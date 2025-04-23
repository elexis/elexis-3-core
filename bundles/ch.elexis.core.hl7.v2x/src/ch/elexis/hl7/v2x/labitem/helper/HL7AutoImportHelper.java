package ch.elexis.hl7.v2x.labitem.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.v2x.labitem.HL7ImportLabItemReader;

public class HL7AutoImportHelper {

	private static final Logger logger = LoggerFactory.getLogger(HL7AutoImportHelper.class);

	public static List<HL7Reader> parseImportReaders(IVirtualFilesystemHandle fileHandle) {
		List<HL7Reader> result = new ArrayList<>();
		try {
			byte[] bytes = fileHandle.readAllBytes();
			String encoding = detectEncodingFromMSH(bytes);
			String content = new String(bytes, encoding);
			String[] rawMessages = content.split("(?<=\\r)MSH\\|");
			for (int i = 0; i < rawMessages.length; i++) {
				String raw = (i == 0) ? rawMessages[i] : "MSH|" + rawMessages[i];
				raw = HL7ReaderFactory.INSTANCE.getAssureSaveMessage(raw);
				PipeParser parser = new PipeParser();
				parser.setValidationContext(new NoValidation());
				Message message = parser.parse(raw);
				result.add(new HL7ImportLabItemReader(message));
			}
		} catch (Exception e) {
			logger.error("Fehler beim Parsen der HL7-Datei", e);
		}
		return result;
	}

	private static String detectEncodingFromMSH(byte[] bytes) {
		try {
			String preview = new String(bytes, "ISO-8859-1");
			String mshLine = preview.lines().filter(l -> l.startsWith("MSH|")).findFirst().orElse(null);
			if (mshLine != null) {
				String[] parts = mshLine.split("\\|", -1);
				if (parts.length >= 18 && !parts[17].isEmpty()) {
					return parts[17];
				}
			}
		} catch (Exception e) {
			logger.warn("Fehler bei Encoding-Erkennung, fallback auf UTF-8", e);
		}
		return "UTF-8";
	}
}
