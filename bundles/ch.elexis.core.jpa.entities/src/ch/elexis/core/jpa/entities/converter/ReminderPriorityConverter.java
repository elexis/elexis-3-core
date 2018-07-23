package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.Priority;

@Converter
public class ReminderPriorityConverter implements AttributeConverter<Priority, String> {

	@Override
	public String convertToDatabaseColumn(Priority attribute){
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public Priority convertToEntityAttribute(String dbData){
		if (StringUtils.isEmpty(dbData)) {
			return Priority.LOW;
		}
		return Priority.byNumericSafe(dbData);
	}
}
