package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.LabItemTyp;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LabItemTypConverter implements AttributeConverter<LabItemTyp, String> {

	@Override
	public String convertToDatabaseColumn(LabItemTyp attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.getType());
		}
		return null;
	}

	@Override
	public LabItemTyp convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return LabItemTyp.TEXT;
		}
		return LabItemTyp.fromType(dbData);
	}
}
