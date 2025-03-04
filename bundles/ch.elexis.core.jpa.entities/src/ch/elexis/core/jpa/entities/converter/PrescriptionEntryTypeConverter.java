package ch.elexis.core.jpa.entities.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.prescription.EntryType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PrescriptionEntryTypeConverter implements AttributeConverter<EntryType, String> {

	private static final Logger logger = LoggerFactory.getLogger(PrescriptionEntryTypeConverter.class);

	@Override
	public String convertToDatabaseColumn(EntryType attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public EntryType convertToEntityAttribute(String dbData) {
		String prescTypeString = dbData;
		int typeNum = -1;
		if (prescTypeString != null && !prescTypeString.isEmpty()) {
			try {
				typeNum = Integer.parseInt(prescTypeString);
			} catch (NumberFormatException e) {
				// ignore and return -1
			}
		}

		if (typeNum != -1) {
			return EntryType.byNumeric(typeNum);
		}
		logger.warn("Unknown entry type [" + dbData + "] using UNKNOWN");
		return EntryType.UNKNOWN;
	}
}
