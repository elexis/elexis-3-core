package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class DoubleStringConverter implements AttributeConverter<Double, String> {

	private Logger log = LoggerFactory.getLogger(DoubleStringConverter.class);
	
	@Override
	public String convertToDatabaseColumn(Double objectValue){
		return Double.toString((double) objectValue);
	}

	@Override
	public Double convertToEntityAttribute(String dataValue){
		if (StringUtils.isEmpty(dataValue)) {
			return 0.0;
		}
		try {
			return Double.parseDouble(((String) dataValue).trim());
		} catch (NumberFormatException ex) {
			log.warn("Number format exception "+dataValue, ex);
			return 0.0;
		}
	}
}
