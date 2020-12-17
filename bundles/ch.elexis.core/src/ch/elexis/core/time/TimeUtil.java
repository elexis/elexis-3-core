package ch.elexis.core.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

import ch.elexis.core.jdt.Nullable;

public class TimeUtil {
	
	public static final DateTimeFormatter FULL_GER =
		DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
	public static DateTimeFormatter DATE_GER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	public static String formatSafe(LocalDateTime localDateTime){
		if (localDateTime != null) {
			return FULL_GER.format(localDateTime);
		}
		return "";
	}
	
	public static String formatSafe(LocalDate date){
		if (date != null) {
			return date.format(DATE_GER);
		}
		return "";
	}
	
	public static @Nullable LocalDate toLocalDate(Date date){
		if (date == null) {
			return null;
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());
		return gc.toZonedDateTime().toLocalDate();
	}
	
}
