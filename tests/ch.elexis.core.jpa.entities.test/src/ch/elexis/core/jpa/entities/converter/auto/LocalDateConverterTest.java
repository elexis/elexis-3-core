package ch.elexis.core.jpa.entities.converter.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;

import org.junit.Test;

public class LocalDateConverterTest {
	LocalDateConverter ldc = new LocalDateConverter();

	String DATE_STRING = "20150512";
	LocalDate instant = LocalDate.of(2015, 5, 12);
	
	@Test
	public void testConvertToDatabaseColumn() {
		assertEquals(DATE_STRING, ldc.convertToDatabaseColumn(instant));
	}

	@Test
	public void testConvertToEntityAttribute() {
		LocalDate value = ldc.convertToEntityAttribute(DATE_STRING);
		assertEquals(value.toString(), instant.toString());
		
		assertNull(ldc.convertToEntityAttribute("dalkfjasldkfjasdkjf"));
		assertNull(ldc.convertToEntityAttribute(""));
	}
}
