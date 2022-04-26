package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.Visibility;

@Converter
public class ReminderVisibilityConverter implements AttributeConverter<Visibility, String> {

	@Override
	public String convertToDatabaseColumn(Visibility attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public Visibility convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return Visibility.ALWAYS;
		}
		return Visibility.byNumericSafe(dbData);
	}
}
