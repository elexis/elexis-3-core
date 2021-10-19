package ch.elexis.core.time;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;

public class DateConverter {

	private DatatypeFactory dtf;

	public DateConverter() {
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	public XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDate date) {
		GregorianCalendar gc = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
		return dtf.newXMLGregorianCalendar(gc);
	}

	public XMLGregorianCalendar convertToXMLGregorianCalendar(BigInteger timestamp) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return dtf.newXMLGregorianCalendar(gc);
	}

	public XMLGregorianCalendar convertToXMLGregorianCalendarDateOnly(LocalDate date) {
		XMLGregorianCalendar ret = dtf.newXMLGregorianCalendar();
		ret.setDay(date.getDayOfMonth());
		ret.setMonth(date.getMonthValue());
		ret.setYear(date.getYear());
		return ret;
	}

	public XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDateTime beginTime) {
		GregorianCalendar gc = GregorianCalendar.from(beginTime.atZone(ZoneId.systemDefault()));
		return dtf.newXMLGregorianCalendar(gc);
	}

	/**
	 * 
	 * @param date
	 *            if <code>null</code> assumes now
	 * @return
	 */
	public XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
		if (date == null) {
			date = new Date();
		}
		return convertToXMLGregorianCalendar(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	public LocalDate convertToLocalDate(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar().toZonedDateTime().toLocalDate();
	}

	public LocalDateTime convertToLocalDateTime(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}

	public LocalDateTime convertToLocalDateTime(BigInteger timestamp) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.toZonedDateTime().toLocalDateTime();
	}

	public LocalDate convertToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public LocalDate convertToLocalDate(BigInteger timestamp) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.toZonedDateTime().toLocalDate();
	}

	public Date convertToDate(BigInteger timestamp) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.getTime();
	}

	public Date convertToDate(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar().getTime();
	}

	public Duration determineAbsoluteDurationBetweenXMLGregorianCalender(XMLGregorianCalendar ts1,
			XMLGregorianCalendar ts2) {
		long ts1T = ts1.toGregorianCalendar().getTimeInMillis();
		long ts2T = ts2.toGregorianCalendar().getTimeInMillis();
		long absoluteDuration = Math.abs(ts1T - ts2T);

		return dtf.newDuration(absoluteDuration);
	}

	/**
	 * Fail-Save conversion, reverting to now in case of an error.
	 * 
	 * @param localDate
	 * @param log
	 * @return
	 */
	public XMLGregorianCalendar convertToXMLGregorianCalendarFailSafe(LocalDate localDate, Logger log) {
		try {
			if (localDate == null) {
				localDate = LocalDate.now();
			}
			return convertToXMLGregorianCalendar(localDate);
		} catch (Exception e) {
			if (log != null) {
				log.error("Error converting date {}, reverting to now.", localDate);
			}
		}
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		return dtf.newXMLGregorianCalendar(gregorianCalendar);
	}

	public XMLGregorianCalendar convertToXMLGregorianCalendarFailSafe(LocalDateTime localDateTime, Logger log) {
		try {
			if (localDateTime == null) {
				localDateTime = LocalDateTime.now();
			}
			return convertToXMLGregorianCalendar(localDateTime);
		} catch (Exception e) {
			if (log != null) {
				log.error("Error converting date {}, reverting to now.", localDateTime);
			}
		}
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		return dtf.newXMLGregorianCalendar(gregorianCalendar);
	}

}
