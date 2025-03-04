package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.StringConstants;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanCharacterConverterSafe implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean objectValue) {
		return (objectValue != null && objectValue) ? StringConstants.ONE : StringConstants.ZERO;
	}

	@Override
	public Boolean convertToEntityAttribute(String dataValue) {
		if (StringUtils.isEmpty(dataValue)) {
			return false;
		}
		return (dataValue.equals(StringConstants.ONE)) ? true : false;
	}
}
