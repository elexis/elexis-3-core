package ch.elexis.core.findings.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;

import ca.uhn.fhir.rest.param.DateRangeParam;

public class DateRangeParamUtil {

	/**
	 * Test if the period is within the bounds of the dates. If no dates specified
	 * returns always true, if no period specified always false.
	 *
	 * <br />
	 * Period start has to be before upper bound. Period end has to be after lower
	 * bound.
	 *
	 * @param period
	 * @param dates
	 * @return
	 */
	public static boolean isPeriodInRange(Period period, DateRangeParam dates) {
		if (dates == null) {
			return true;
		}
		if (period == null) {
			return false;
		}

		Date lower = dates.getLowerBoundAsInstant();
		Date upper = dates.getUpperBoundAsInstant();

		Date start = period.getStart();
		Date end = period.getEnd();

		if (start != null) {
			if (upper != null) {
				if (start.after(upper)) {
					return false;
				}
			}
		}

		if (end != null) {
			if (lower != null) {
				if (end.before(lower)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Test if the dateTime is within the bounds of the dates. If no dates specified
	 * returns always true, if no dateTime specified always false.
	 *
	 * @param dateTime
	 * @param dates
	 * @return
	 */
	public static boolean isDateInRange(DateTimeType dateTime, DateRangeParam dates) {
		if (dates == null) {
			return true;
		}
		if (dateTime == null) {
			return false;
		}
		LocalDate lower = LocalDate.parse(dates.getLowerBound().getValueAsString());
		LocalDate upper = LocalDate.parse(dates.getUpperBound().getValueAsString());

		LocalDateTime date = LocalDateTime.parse(dateTime.asStringValue(), DateTimeFormatter.ISO_DATE_TIME);

		if (upper != null) {
			if (date.isAfter(upper.atTime(23, 59, 59))) {
				return false;
			}
		}

		if (lower != null) {
			if (date.isBefore(lower.atStartOfDay())) {
				return false;
			}
		}

		return true;
	}

}
