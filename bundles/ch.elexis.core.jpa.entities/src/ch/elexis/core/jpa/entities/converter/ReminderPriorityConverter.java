package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.Priority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReminderPriorityConverter implements AttributeConverter<Priority, String> {

	@Override
	public String convertToDatabaseColumn(Priority attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public Priority convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return Priority.LOW;
		}
		return Priority.byNumericSafe(dbData);
	}
}
