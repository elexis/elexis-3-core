package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.Type;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReminderTypeConverter implements AttributeConverter<Type, String> {

	@Override
	public String convertToDatabaseColumn(Type attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public Type convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return Type.COMMON;
		}
		return Type.byNumericSafe(dbData);
	}
}
