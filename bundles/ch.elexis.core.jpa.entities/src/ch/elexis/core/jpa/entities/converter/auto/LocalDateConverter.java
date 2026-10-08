package ch.elexis.core.jpa.entities.converter.auto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

	private Logger log = LoggerFactory.getLogger(LocalDateConverter.class);

	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public String convertToDatabaseColumn(LocalDate date) {
		if (date == null) {
			return null;
		} else if (LocalDate.MIN.equals(date)) {
			return "";
		}

		return date.format(yyyyMMdd);
	}

	@Override
	public LocalDate convertToEntityAttribute(String dateValue) {
		if (StringUtils.isBlank(dateValue)) {
			return null;
		}

		dateValue = dateValue.trim();

		try {
			return LocalDate.parse(dateValue, yyyyMMdd);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing [{}]", dateValue, e);
		}
		return null;
	}
}
