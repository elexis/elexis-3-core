package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.issue.ProcessStatus;

@Converter
public class ReminderProcessStatusConverter implements AttributeConverter<ProcessStatus, String> {

	@Override
	public String convertToDatabaseColumn(ProcessStatus attribute){
		if (attribute != null) {
			return Integer.toString(attribute.numericValue());
		}
		return null;
	}

	@Override
	public ProcessStatus convertToEntityAttribute(String dbData){
		if (StringUtils.isEmpty(dbData)) {
			return ProcessStatus.OPEN;
		}
		return ProcessStatus.byNumericSafe(dbData);
	}
}
