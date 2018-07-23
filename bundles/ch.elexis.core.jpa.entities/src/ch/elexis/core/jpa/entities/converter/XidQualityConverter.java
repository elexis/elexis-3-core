package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.XidQuality;

@Converter
public class XidQualityConverter implements AttributeConverter<XidQuality, String> {

	@Override
	public String convertToDatabaseColumn(XidQuality attribute){
		if (attribute != null) {
			return Integer.toString(attribute.getValue());
		}
		return null;
	}

	@Override
	public XidQuality convertToEntityAttribute(String dbData){
		if (StringUtils.isEmpty(dbData)) {
			return XidQuality.ASSIGNMENT_LOCAL;
		}
		return XidQuality.ofValue(Integer.parseInt(dbData));
	}
}
