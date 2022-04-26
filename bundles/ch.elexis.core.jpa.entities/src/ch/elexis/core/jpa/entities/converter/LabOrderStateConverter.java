package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.LabOrderState;

@Converter
public class LabOrderStateConverter implements AttributeConverter<LabOrderState, String> {

	@Override
	public String convertToDatabaseColumn(LabOrderState attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.getValue());
		}
		return null;
	}

	@Override
	public LabOrderState convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return LabOrderState.ORDERED;
		}
		return LabOrderState.ofValue(Integer.parseInt(dbData));
	}
}
