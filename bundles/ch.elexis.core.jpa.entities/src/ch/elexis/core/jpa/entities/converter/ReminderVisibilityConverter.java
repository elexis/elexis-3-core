package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.Visibility;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
