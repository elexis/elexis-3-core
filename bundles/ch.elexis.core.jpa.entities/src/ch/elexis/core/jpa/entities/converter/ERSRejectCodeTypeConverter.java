package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.model.esr.ESRRejectCode;

@Converter
public class ERSRejectCodeTypeConverter implements AttributeConverter<ESRRejectCode, String> {

	@Override
	public String convertToDatabaseColumn(ESRRejectCode attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public ESRRejectCode convertToEntityAttribute(String dbData) {
		String esrCodeString = dbData;
		int typeNum = -1;
		if (esrCodeString != null && !esrCodeString.isEmpty()) {
			try {
				typeNum = Integer.parseInt(esrCodeString);
			} catch (NumberFormatException e) {
				// ignore and return -1
			}
		}

		return ESRRejectCode.byNumeric(typeNum);
	}
}
