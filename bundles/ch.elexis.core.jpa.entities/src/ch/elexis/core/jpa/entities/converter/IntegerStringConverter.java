package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntegerStringConverter implements AttributeConverter<Integer, String> {

	private Logger log = LoggerFactory.getLogger(IntegerStringConverter.class);

	@Override
	public String convertToDatabaseColumn(Integer objectValue) {
		return Integer.toString((int) objectValue);
	}

	@Override
	public Integer convertToEntityAttribute(String dataValue) {
		if (StringUtils.isEmpty(dataValue)) {
			return 0;
		}
		try {
			return Integer.parseInt(((String) dataValue).trim());
		} catch (NumberFormatException ex) {
			log.warn("Number format exception " + dataValue, ex);
			return 0;
		}
	}
}
