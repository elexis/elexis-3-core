package ch.elexis.core.jpa.entities.converter.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

public class LocalDateTimeConverterTest {
	
	LocalDateTimeConverter ldtc = new LocalDateTimeConverter();

	String DATETIME_STRING = "20150512142200";
	LocalDateTime instant = LocalDateTime.of(2015, 5, 12, 14, 22, 00);
	
	@Test
	public void testConvertToDatabaseColumn() {
		assertEquals(DATETIME_STRING, ldtc.convertToDatabaseColumn(instant));
	}

	@Test
	public void testConvertToEntityAttribute() {
		LocalDateTime value = ldtc.convertToEntityAttribute(DATETIME_STRING);
		assertEquals(value.toString(), instant.toString());
		
		instant = LocalDate.of(2015, 05, 12).atStartOfDay();
		value = ldtc.convertToEntityAttribute("20150512");
		assertEquals(value.toString(), instant.toString());
		
		value = ldtc.convertToEntityAttribute("12.05.2015");
		assertEquals(value.toString(), instant.toString());
		
		assertNull(ldtc.convertToEntityAttribute("dalkfjasldkfjasdkjf"));
		assertNull(ldtc.convertToEntityAttribute(""));
	}

}
