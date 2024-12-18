package ch.elexis.core.jpa.entities.converter;

import ch.elexis.core.model.esr.ESRCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ERSCodeTypeConverter implements AttributeConverter<ESRCode, String> {

	@Override
	public String convertToDatabaseColumn(ESRCode attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public ESRCode convertToEntityAttribute(String dbData) {
		String esrCodeString = dbData;
		int typeNum = -1;
		if (esrCodeString != null && !esrCodeString.isEmpty()) {
			try {
				typeNum = Integer.parseInt(esrCodeString);
			} catch (NumberFormatException e) {
				// ignore and return -1
			}
		}

		return ESRCode.byNumeric(typeNum);
	}
}
