package ch.elexis.core.jpa.entities.converter.auto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		if (dateValue == null || dateValue.isEmpty()) {
			return null;
		}

		try {
			return LocalDate.parse(dateValue, yyyyMMdd);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing [{}]", dateValue, e);
		}
		return null;
	}
}
