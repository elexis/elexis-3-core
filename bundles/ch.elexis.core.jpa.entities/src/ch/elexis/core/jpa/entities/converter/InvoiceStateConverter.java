package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.InvoiceState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class InvoiceStateConverter implements AttributeConverter<InvoiceState, String> {

	private Logger log = LoggerFactory.getLogger(InvoiceStateConverter.class);

	@Override
	public String convertToDatabaseColumn(InvoiceState attribute) {
		if (attribute == null) {
			return Integer.toString(InvoiceState.UNKNOWN.numericValue());
		}
		return Integer.toString(attribute.getState());
	}

	@Override
	public InvoiceState convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return InvoiceState.UNKNOWN;
		}
		try {
			int value = Integer.parseInt(dbData.trim());
			return InvoiceState.fromState(value);
		} catch (NumberFormatException ex) {
			log.warn("Number format exception " + dbData, ex);
			return InvoiceState.UNKNOWN;
		}
	}

}
