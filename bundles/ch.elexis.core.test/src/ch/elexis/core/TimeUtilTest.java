package ch.elexis.core;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.time.TimeUtil;

public class TimeUtilTest {

	private static DatatypeFactory dtf;

	@BeforeClass
	public static void beforeClass() throws DatatypeConfigurationException {
		dtf = DatatypeFactory.newInstance();
	}

	@Test
	public void testToLocalDateXMLGregorianCalendar() {
		XMLGregorianCalendar calendar = dtf.newXMLGregorianCalendar("2021-10-13T00:00:00.0Z");
		LocalDate result = TimeUtil.toLocalDate(calendar);
		LocalDate reference = LocalDate.of(2021, Month.OCTOBER, 13);
		assertEquals(reference, result);
	}

	@Test
	public void testToLocalDateTimeXMLGregorianCalendar() {
		XMLGregorianCalendar calendar = dtf.newXMLGregorianCalendar("2021-10-13T05:49:44.303Z");
		LocalDateTime result = TimeUtil.toLocalDateTime(calendar);
		LocalDateTime reference = LocalDateTime.of(2021, Month.OCTOBER, 13, 7, 49, 44, 303000000);
		assertEquals(reference, result);
	}

	@Test
	public void testToDateXMLGregorianCalendar() {
		XMLGregorianCalendar calendar = dtf.newXMLGregorianCalendar("2021-10-13T05:49:44.303Z");
		Date result = TimeUtil.toDate(calendar);
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.set(2021, 9, 13, 7, 49, 44);
		Date reference = gregorianCalendar.getTime();
		assertEquals(reference, result);
	}

}
