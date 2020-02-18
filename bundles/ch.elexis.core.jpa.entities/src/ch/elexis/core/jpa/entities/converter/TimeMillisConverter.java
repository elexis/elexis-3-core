package ch.elexis.core.jpa.entities.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeMillisConverter {
	
	public static LocalDateTime convertOptionalMillisToLocalDateTime(Long currentTimeMillis){
		if (currentTimeMillis == null) {
			return null;
		}
		return Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault())
			.toLocalDateTime();
	}
	
}
