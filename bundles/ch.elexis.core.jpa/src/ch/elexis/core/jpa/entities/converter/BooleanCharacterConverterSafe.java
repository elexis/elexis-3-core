package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.StringConstants;

@Converter
public class BooleanCharacterConverterSafe implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean objectValue){
		return (Boolean.valueOf(objectValue.toString()) == true) ? StringConstants.ONE
				: StringConstants.ZERO;
	}
	
	@Override
	public Boolean convertToEntityAttribute(String dataValue){
		if (StringUtils.isEmpty(dataValue)) {
			return false;
		}
		return (dataValue.equals(StringConstants.ONE)) ? true : false;
	}
}
