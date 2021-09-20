package ch.elexis.core.time;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;

import ch.elexis.core.jdt.Nullable;

public class TimeUtil {
	
	static {
		try {
			TimeUtil.dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static DatatypeFactory dtf;
	
	/**
	 * dd.MM.yyyy, HH:mm:ss
	 */
	public static final DateTimeFormatter FULL_GER =
		DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
	/**
	 * dd.MM.yyyy
	 */
	public static DateTimeFormatter DATE_GER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	/**
	 * dd.MM.yy
	 */
	public static DateTimeFormatter DATE_GER_SHORT = DateTimeFormatter.ofPattern("dd.MM.yy");
	
	public static String formatSafe(LocalDateTime localDateTime){
		if (localDateTime != null) {
			return FULL_GER.format(localDateTime);
		}
		return "";
	}
	
	/**
	 * Format with default format {@link #DATE_GER}
	 */
	public static String formatSafe(LocalDate date){
		if (date != null) {
			return date.format(DATE_GER);
		}
		return "";
	}
	
	public static String formatSafe(LocalDate date, DateTimeFormatter formatter){
		if (date != null) {
			return formatter.format(date);
		}
		return "";
	}
	
	public static String formatSafe(Long lastUpdate){
		if (lastUpdate == null) {
			return "";
		}
		return formatSafe(toLocalDateTime(new Date(lastUpdate)));
	}
	
	public static @Nullable LocalDate toLocalDate(@Nullable Date date){
		if (date == null) {
			return null;
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());
		return gc.toZonedDateTime().toLocalDate();
	}
	
	public static @Nullable LocalDateTime toLocalDateTime(@Nullable Date date){
		if (date == null) {
			return null;
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());
		return gc.toZonedDateTime().toLocalDateTime();
	}
	
	public static Date toDate(LocalDateTime localDateTime){
		if (localDateTime == null) {
			return null;
		}
		return java.util.Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static Date toDate(LocalDate localDate){
		if (localDate == null) {
			return null;
		}
		return java.util.Date
			.from(localDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static boolean isToday(LocalDate date){
		return LocalDate.now().isEqual(date);
	}
	
	public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate date){
		GregorianCalendar gc = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
		return dtf.newXMLGregorianCalendar(gc);
	}
	
	public static XMLGregorianCalendar toXMLGregorianCalendar(BigInteger timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return dtf.newXMLGregorianCalendar(gc);
	}
	
	public static XMLGregorianCalendar toXMLGregorianCalendarDateOnly(LocalDate date){
		XMLGregorianCalendar ret = dtf.newXMLGregorianCalendar();
		ret.setDay(date.getDayOfMonth());
		ret.setMonth(date.getMonthValue());
		ret.setYear(date.getYear());
		return ret;
	}
	
	public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime beginTime){
		GregorianCalendar gc = GregorianCalendar.from(beginTime.atZone(ZoneId.systemDefault()));
		return dtf.newXMLGregorianCalendar(gc);
	}
	
	/**
	 * 
	 * @param date
	 *            if <code>null</code> assumes now
	 * @return
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date){
		if (date == null) {
			date = new Date();
		}
		return toXMLGregorianCalendar(
			date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}
	
	public static LocalDate toLocalDate(XMLGregorianCalendar xcal){
		return xcal.toGregorianCalendar().toZonedDateTime().toLocalDate();
	}
	
	public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xcal){
		return xcal.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}
	
	public static LocalDateTime toLocalDateTime(BigInteger timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.toZonedDateTime().toLocalDateTime();
	}
	
	public static LocalDateTime toLocalDateTime(Long timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);
		return gc.toZonedDateTime().toLocalDateTime();
	}

	public static LocalDate toLocalDate(BigInteger timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.toZonedDateTime().toLocalDate();
	}
	
	public static Date toDate(BigInteger timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp.longValue());
		return gc.getTime();
	}
	
	public static Date toDate(Long timestamp){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);
		return gc.getTime();
	}
	
	public static Date toDate(XMLGregorianCalendar xcal){
		return xcal.toGregorianCalendar().getTime();
	}
	
	public static Duration determineAbsoluteDurationBetweenXMLGregorianCalender(
		XMLGregorianCalendar ts1, XMLGregorianCalendar ts2){
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
	public static XMLGregorianCalendar toXMLGregorianCalendarFailSafe(LocalDate localDate,
		Logger log){
		try {
			if (localDate == null) {
				localDate = LocalDate.now();
			}
			return toXMLGregorianCalendar(localDate);
		} catch (Exception e) {
			if (log != null) {
				log.error("Error converting date {}, reverting to now.", localDate);
			}
		}
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		return dtf.newXMLGregorianCalendar(gregorianCalendar);
	}
	
	public static XMLGregorianCalendar toXMLGregorianCalendarFailSafe(LocalDateTime localDateTime,
		Logger log){
		try {
			if (localDateTime == null) {
				localDateTime = LocalDateTime.now();
			}
			return toXMLGregorianCalendar(localDateTime);
		} catch (Exception e) {
			if (log != null) {
				log.error("Error converting date {}, reverting to now.", localDateTime);
			}
		}
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		return dtf.newXMLGregorianCalendar(gregorianCalendar);
	}
	
}
