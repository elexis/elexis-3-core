package ch.elexis.core.jpa.entities.converter.auto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;
import ch.rgw.tools.TimeTool.TimeFormatException;

@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

	private Logger log = LoggerFactory.getLogger(LocalDateTimeConverter.class);

	private final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public String convertToDatabaseColumn(LocalDateTime date) {
		if (date == null) {
			return null;
		}

		return date.format(yyyyMMddHHmmss);
	}

	@Override
	public LocalDateTime convertToEntityAttribute(String dateValue) {
		if (dateValue == null || dateValue.length() == 0) {
			return null;
		}

		try {
			switch (dateValue.length()) {
			case 14:
				return LocalDateTime.parse(dateValue, yyyyMMddHHmmss);
			case 8:
				return LocalDate.parse(dateValue, yyyyMMdd).atStartOfDay();
			default:
				log.warn("Using TimeTool to parse [{}]", dateValue);
				return new TimeTool(dateValue, true).toLocalDateTime();
			}
		} catch (DateTimeParseException | TimeFormatException e) {
			log.warn("Error parsing [{}], returning null.", dateValue, e);
			return null;
		}
	}

}
