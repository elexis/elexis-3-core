package ch.elexis.core.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

import ch.elexis.core.jdt.Nullable;

public class TimeUtil {
	
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
	
	public static @Nullable LocalDate toLocalDate(Date date){
		if (date == null) {
			return null;
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());
		return gc.toZonedDateTime().toLocalDate();
	}
	
	public static LocalDateTime toLocalDateTime(Date date){
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
	
}
