package ch.elexis.core.findings.util;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.Optional;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;

public class FindingSortOrder {

	public static final String EXTENSION_SORT_ORDER_URL = "www.elexis.info/extensions/finding/sortorder";

	public static final Comparator<ICondition> BY_DATE_RECORDED_DESC = (left, right) -> {
		LocalDate lRecorded = left.getDateRecorded().orElse(LocalDate.of(1970, Month.JANUARY, 1));
		LocalDate rRecorded = right.getDateRecorded().orElse(LocalDate.of(1970, Month.JANUARY, 1));
		int byRecorded = rRecorded.compareTo(lRecorded);
		if (byRecorded != 0) {
			return byRecorded;
		}
		Long lUpdated = left.getLastupdate() != null ? left.getLastupdate() : Long.valueOf(0);
		Long rUpdated = right.getLastupdate() != null ? right.getLastupdate() : Long.valueOf(0);
		return rUpdated.compareTo(lUpdated);
	};

	public static Optional<Integer> getSortOrder(IFinding finding) {
		return Optional.ofNullable(finding.getStringExtensions().get(EXTENSION_SORT_ORDER_URL))
				.map(Integer::valueOf);
	}

	public static void setSortOrder(IFinding finding, int sortOrder) {
		finding.addStringExtension(EXTENSION_SORT_ORDER_URL, Integer.toString(sortOrder));
	}

	public static <T extends IFinding> Comparator<T> comparator(Comparator<T> fallback) {
		return Comparator.<T, Integer>comparing(finding -> getSortOrder(finding).orElse(null),
				Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(fallback);
	}
}
